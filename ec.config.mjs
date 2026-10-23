// @ts-check
import { defineEcConfig } from 'astro-expressive-code';

/** Configuração do Expressive Code (blocos de código), separada para o componente <Code> funcionar. */
export default defineEcConfig({
  themes: ['github-dark-default', 'github-light-default'],
  themeCssSelector: (theme) => `[data-theme='${theme.type}']`,
  useDarkModeMediaQuery: false,
  styleOverrides: {
    borderRadius: '12px',
    codeFontFamily: "'JetBrains Mono Variable', ui-monospace, monospace",
    codeFontSize: '0.875rem',
    uiFontFamily: "'Inter Variable', system-ui, sans-serif",
    frames: { shadowColor: 'transparent' },
  },
  defaultProps: { wrap: false },
  plugins: [
    {
      // Blocos sem quebra de linha rolam na horizontal: com tabindex, dá para rolar pelo teclado.
      name: 'pre-focavel',
      hooks: {
        postprocessRenderedBlock: ({ renderData }) => {
          /** @param {{ type: string, tagName?: string, properties?: object, children?: any[] }} no */
          const visitar = (no) => {
            if (no.type === 'element' && no.tagName === 'pre') no.properties = { ...no.properties, tabIndex: 0 };
            for (const filho of no.children ?? []) visitar(filho);
          };
          visitar(renderData.blockAst);
        },
      },
    },
  ],
});
