// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import sitemap from "@astrojs/sitemap";
import { SITE } from "./src/config/site.ts";
import path from "path";

// https://astro.build/config
export default defineConfig({
  site: SITE.url,

  integrations: [
    sitemap(),
    starlight({
      title: "Carpet LMS Addition",
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/Citrus-Union/Carpet-LMS-Addition",
        },
      ],
      sidebar: [
        {
          label: "Intro",
          link: "/docs/",
        },
        {
          label: "Rules",
          link: "/docs/rules/",
        },
      ],
      components: {
        Head: "./src/components/Head.astro",
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
