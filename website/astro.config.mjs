// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import sitemap from "@astrojs/sitemap";
import icon from "astro-icon";
import { SITE } from "./src/config/site.ts";
import path from "path";

const branch = process.env.GIT_BRANCH ?? process.env.CF_PAGES_BRANCH ?? "main";

// https://astro.build/config
export default defineConfig({
  site: SITE.url,
  markdown: {
    shikiConfig: {
      langAlias: {
        mcfunction: "shellscript",
        snbt: "hjson",
      },
    },
  },

  integrations: [
    icon(),
    sitemap(),
    starlight({
      title: "Carpet LMS Addition",
      lastUpdated: true,
      editLink: {
        baseUrl: `https://github.com/Citrus-Union/Carpet-LMS-Addition/blob/${branch}/website`,
      },
      locales: {
        root: {
          label: "English",
          lang: "en",
        },
        "zh-cn": {
          label: "简体中文",
          lang: "zh-CN",
        },
      },
      sidebar: [
        "docs",
        "docs/rules",
        {
          label: "Storage",
          translations: {
            "zh-CN": "仓储",
          },
          items: [
            "docs/storage",
            "docs/storage/tutor",
            "docs/storage/config",
            {
              label: "Development",
              translations: {
                "zh-CN": "开发",
              },
              items: [
                "docs/storage/development/web-api",
                "docs/storage/development/command-api",
              ],
            },
          ],
        },
      ],
      components: {
        Head: "./src/components/Head.astro",
        SocialIcons: "./src/components/SocialIcons.astro",
      },
    }),
  ],

  vite: {
    assetsInclude: ["**/*.yml"],
    server: {
      fs: {
        allow: [path.resolve("."), path.resolve("..", "src")],
      },
    },
  },
});
