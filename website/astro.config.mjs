// @ts-check
import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import sitemap from "@astrojs/sitemap";
import path from "path";

// https://astro.build/config
export default defineConfig({
  site: "https://carpet.lms.jasonxue.top",

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
          link: "/intro/",
        },
        {
          label: "Rules",
          link: "/rules/",
        },
      ],
      components: {
        Footer: "./src/components/Analytics.astro",
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
