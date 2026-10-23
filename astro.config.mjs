// @ts-check
import { defineConfig } from 'astro/config';
import mdx from '@astrojs/mdx';
import expressiveCode from 'astro-expressive-code';
import { satteri } from '@astrojs/markdown-satteri';
import { markdownAcessivel } from './src/lib/markdown-acessivel.mjs';

// Em GitHub Pages o site fica em /<repo>/. Defina BASE_PATH no CI (ex.: "/trilha-java-backend").
const base = process.env.BASE_PATH ?? '/';
const site = process.env.SITE_URL ?? 'https://example.com';

export default defineConfig({
  site,
  base,
  trailingSlash: 'always',
  integrations: [
    expressiveCode(), // opções em ec.config.mjs
    mdx(),
  ],
  markdown: {
    smartypants: false,
    processor: satteri({ hastPlugins: [markdownAcessivel] }),
  },
});
