#!/usr/bin/env python3.14

from __future__ import annotations

import concurrent.futures
import json
import re
import subprocess
import sys
import tomllib
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Callable

ROOT = Path(__file__).resolve().parent
VERSION_CATALOG = ROOT / "gradle" / "libs.versions.toml"
USER_AGENT = "carpet-lms-addition-version-checker/1.0"
MAX_ATTEMPTS = 3


@dataclass(frozen=True, slots=True)
class CheckSpec:
    key: str
    source: str
    fetch_latest: Callable[..., str] | None = None
    fetch_args: tuple[object, ...] = ()
    skip_reason: str | None = None


@dataclass(frozen=True, slots=True)
class CheckRow:
    key: str
    current: str
    latest: str
    is_latest: str
    source: str


def fetch_text(url: str) -> str:
    request = urllib.request.Request(
        url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"}
    )
    with urllib.request.urlopen(request, timeout=20) as response:
        charset = response.headers.get_content_charset() or "utf-8"
        return response.read().decode(charset)


def fetch_json(url: str) -> object:
    return json.loads(fetch_text(url))


def normalize_github_tag(tag: str) -> str:
    return tag.removeprefix("v")


def npm_latest(package_name: str) -> str:
    encoded_name = urllib.parse.quote(package_name, safe="@/")
    data = fetch_json(f"https://registry.npmjs.org/{encoded_name}/latest")
    if not isinstance(data, dict) or not isinstance(
        version := data.get("version"), str
    ):
        raise ValueError(f"npm did not return a version for {package_name}")
    return version


def github_latest_release(owner: str, repo: str) -> str:
    data = fetch_json(f"https://api.github.com/repos/{owner}/{repo}/releases/latest")
    if not isinstance(data, dict) or not isinstance(
        tag_name := data.get("tag_name"), str
    ):
        raise ValueError(f"GitHub did not return a release tag for {owner}/{repo}")
    return normalize_github_tag(tag_name)


def eclipse_update_site_latest() -> str:
    try:
        root_completed = subprocess.run(
            ["curl", "-Ls", "https://download.eclipse.org/eclipse/updates/"],
            check=True,
            capture_output=True,
            text=True,
        )
    except subprocess.CalledProcessError as exc:
        raise ValueError("Failed to query Eclipse update site") from exc

    release_line_match = re.search(r"url=([0-9]+\.[0-9]+)", root_completed.stdout)
    if not release_line_match:
        raise ValueError("Eclipse update site did not expose the current release line")
    release_line = release_line_match.group(1)

    try:
        release_completed = subprocess.run(
            [
                "curl",
                "-Ls",
                f"https://download.eclipse.org/eclipse/updates/{release_line}/",
            ],
            check=True,
            capture_output=True,
            text=True,
        )
    except subprocess.CalledProcessError as exc:
        raise ValueError(
            f"Failed to query Eclipse update site release line {release_line}"
        ) from exc

    if match := re.search(
        rf"R-{re.escape(release_line)}-\d{{12}}", release_completed.stdout
    ):
        return match.group(0)
    raise ValueError(
        f"Eclipse update site did not expose an R-{release_line}-* directory"
    )


def node_latest(major: int | None = None) -> str:
    data = fetch_json("https://nodejs.org/dist/index.json")
    if not isinstance(data, list):
        raise ValueError("Node index returned an unexpected payload")

    for entry in data:
        if not isinstance(entry, dict):
            continue
        if not isinstance(
            version := entry.get("version"), str
        ) or not version.startswith("v"):
            continue
        normalized = version.removeprefix("v")
        if major is not None and not normalized.startswith(f"{major}."):
            continue
        return normalized

    raise ValueError("Node index did not return a version")


def load_versions() -> dict[str, str]:
    with VERSION_CATALOG.open("rb") as f:
        data = tomllib.load(f)

    raw_versions = data.get("versions")
    if not isinstance(raw_versions, dict):
        raise ValueError(f"{VERSION_CATALOG} does not contain a [versions] table")

    versions = {
        key: value
        for key, value in raw_versions.items()
        if isinstance(key, str) and isinstance(value, str)
    }

    if not versions:
        raise ValueError(
            f"{VERSION_CATALOG} does not contain any string entries in [versions]"
        )
    return versions


def check_skip(key: str, source: str, reason: str) -> CheckSpec:
    return CheckSpec(key, source, skip_reason=reason)


def check_npm(key: str, package_name: str) -> CheckSpec:
    return CheckSpec(key, "npm", npm_latest, (package_name,))


def check_gradle_plugin(key: str, plugin_id: str) -> CheckSpec:
    return check_custom_maven(
        key,
        "gradleplugin",
        "https://plugins.gradle.org/m2",
        plugin_id,
        f"{plugin_id}.gradle.plugin",
    )


def check_custom_maven(
    key: str, source: str, base_url: str, group: str, artifact: str
) -> CheckSpec:
    return CheckSpec(key, source, maven_latest, (base_url, group, artifact))


def check_maven_central(key: str, group: str, artifact: str) -> CheckSpec:
    return check_custom_maven(
        key,
        "mavencentral",
        "https://repo1.maven.org/maven2",
        group,
        artifact,
    )


def check_node(key: str, major: int | None = None) -> CheckSpec:
    return CheckSpec(key, "nodejs", node_latest, (major,))


def check_github_release(key: str, owner: str, repo: str) -> CheckSpec:
    return CheckSpec(key, "github", github_latest_release, (owner, repo))


def check_source(
    key: str, source: str, fetch_latest: Callable[..., str], *fetch_args: object
) -> CheckSpec:
    return CheckSpec(key, source, fetch_latest, fetch_args)


def maven_latest(base_url: str, group: str, artifact: str) -> str:
    group_path = group.replace(".", "/")
    metadata_url = f"{base_url.rstrip('/')}/{group_path}/{artifact}/maven-metadata.xml"
    xml_text = fetch_text(metadata_url)
    latest_match = re.search(r"<latest>([^<]+)</latest>", xml_text)
    if latest_match:
        return latest_match.group(1).strip()

    release_match = re.search(r"<release>([^<]+)</release>", xml_text)
    if release_match:
        return release_match.group(1).strip()

    versions = re.findall(r"<version>([^<]+)</version>", xml_text)
    if versions:
        return versions[-1].strip()

    raise ValueError(f"No version found in {metadata_url}")


def build_specs() -> list[CheckSpec]:
    return [
        check_custom_maven(
            "fabricLoom",
            "fabricmaven",
            "https://maven.fabricmc.net",
            "net.fabricmc",
            "fabric-loom",
        ),
        check_custom_maven(
            "loader",
            "fabricmaven",
            "https://maven.fabricmc.net",
            "net.fabricmc",
            "fabric-loader",
        ),
        check_custom_maven(
            "conditionalMixin",
            "fallenmaven",
            "https://maven.fallenbreath.me/releases",
            "me.fallenbreath",
            "conditional-mixin-fabric",
        ),
        check_maven_central("jbcrypt", "org.mindrot", "jbcrypt"),
        check_maven_central("jspecify", "org.jspecify", "jspecify"),
        check_skip("preprocess", "jitpack-commit", "special"),
        check_gradle_plugin("yamlang", "me.fallenbreath.yamlang"),
        check_gradle_plugin("spotless", "com.diffplug.spotless"),
        check_maven_central(
            "mavenPublish",
            "com.vanniktech.maven.publish",
            "com.vanniktech.maven.publish.gradle.plugin",
        ),
        check_gradle_plugin("nodeGradle", "com.github.node-gradle.node"),
        check_node("nodeRuntime", 24),
        check_npm("pnpm", "pnpm"),
        check_npm("prettier", "prettier"),
        check_npm("prettierPlugin-toml", "prettier-plugin-toml"),
        check_npm("prettierPlugin-xml", "@prettier/plugin-xml"),
        check_npm("prettierPlugin-astro", "prettier-plugin-astro"),
        check_source("eclipse", "eclipse-updates", eclipse_update_site_latest),
        check_github_release("ktlint", "ktlint", "ktlint"),
    ]


def print_progress(current: int, total: int, key: str) -> None:
    bar_width = 24
    filled = 0 if total == 0 else current * bar_width // total
    bar = "#" * filled + "-" * (bar_width - filled)
    print(
        f"\rProgress [{bar}] {current}/{total} {key}",
        end="",
        file=sys.stderr,
        flush=True,
    )
    if current == total:
        print(file=sys.stderr, flush=True)


def fetch_with_retries(
    fetcher: Callable[..., str], *fetch_args: object, attempts: int = MAX_ATTEMPTS
) -> str:
    last_error: Exception | None = None
    for _ in range(attempts):
        try:
            return fetcher(*fetch_args)
        except (
            TimeoutError,
            urllib.error.URLError,
            urllib.error.HTTPError,
            ValueError,
        ) as exc:
            last_error = exc
    if last_error is not None:
        raise last_error
    raise ValueError("Fetcher did not run")


def resolve_row(spec: CheckSpec, versions: dict[str, str]) -> CheckRow:
    current = versions.get(spec.key, "not configured")
    if spec.skip_reason is not None:
        return CheckRow(
            spec.key,
            current,
            spec.skip_reason,
            spec.skip_reason,
            spec.source,
        )
    if spec.fetch_latest is None:
        raise ValueError(f"{spec.key} is missing a fetcher")

    try:
        latest = fetch_with_retries(spec.fetch_latest, *spec.fetch_args)
        is_latest = "yes" if latest == current else "no"
    except (TimeoutError, urllib.error.URLError, urllib.error.HTTPError, ValueError):
        latest = "not found"
        is_latest = "not found"
    return CheckRow(spec.key, current, latest, is_latest, spec.source)


def build_rows() -> list[CheckRow]:
    versions = load_versions()
    specs = build_specs()
    rows_by_key: dict[str, CheckRow] = {}

    with concurrent.futures.ThreadPoolExecutor(
        max_workers=min(8, len(specs))
    ) as executor:
        future_to_spec = {
            executor.submit(resolve_row, spec, versions): spec for spec in specs
        }
        for index, future in enumerate(
            concurrent.futures.as_completed(future_to_spec), start=1
        ):
            spec = future_to_spec[future]
            rows_by_key[spec.key] = future.result()
            print_progress(index, len(specs), spec.key)

    return [rows_by_key[spec.key] for spec in specs]


def print_table(rows: list[CheckRow]) -> None:
    headers = ("key", "current", "latest", "up_to_date", "source")
    key_width = max(len(headers[0]), *(len(row.key) for row in rows))
    current_width = max(len(headers[1]), *(len(row.current) for row in rows))
    latest_width = max(len(headers[2]), *(len(row.latest) for row in rows))
    status_width = max(len(headers[3]), *(len(row.is_latest) for row in rows))
    source_width = max(len(headers[4]), *(len(row.source) for row in rows))

    print(
        f"{headers[0]:<{key_width}}  {headers[1]:<{current_width}}  {headers[2]:<{latest_width}}  "
        f"{headers[3]:<{status_width}}  {headers[4]:<{source_width}}",
    )
    print(
        f"{'-' * key_width}  {'-' * current_width}  {'-' * latest_width}  {'-' * status_width}  {'-' * source_width}",
    )
    for row in rows:
        print(
            f"{row.key:<{key_width}}  {row.current:<{current_width}}  {row.latest:<{latest_width}}  "
            f"{row.is_latest:<{status_width}}  {row.source:<{source_width}}",
        )


def print_grouped_tables(rows: list[CheckRow]) -> None:
    groups = [
        ("Not Found", [row for row in rows if row.is_latest == "not found"]),
        ("Special", [row for row in rows if row.is_latest == "special"]),
        ("Up To Date", [row for row in rows if row.is_latest == "yes"]),
        ("Outdated", [row for row in rows if row.is_latest == "no"]),
    ]

    first_group = True
    for title, group_rows in groups:
        if not group_rows:
            continue
        if not first_group:
            print()
        print(title)
        print_table(group_rows)
        first_group = False


def main() -> int:
    rows = build_rows()
    print_grouped_tables(rows)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
