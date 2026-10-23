// Plugin do Sätteri (processador de Markdown do Astro) com ajustes de acessibilidade:
// - listas de verificação (`- [ ] item`) geram checkboxes sempre desabilitadas e sem rótulo;
//   elas viram decoração (o texto do item já diz tudo ao leitor de tela);
// - tabelas comparativas com o primeiro cabeçalho vazio (`| | A | B |`) ganham um
//   rótulo só para leitores de tela.
export function markdownAcessivel() {
  return {
    name: 'markdown-acessivel',
    element: {
      filter: ['input', 'th'],
      visit(node, ctx) {
        if (node.tagName === 'input') {
          if (node.properties?.type === 'checkbox') ctx.setProperty(node, 'aria-hidden', 'true');
          return;
        }
        if (!ctx.textContent(node).trim()) {
          ctx.appendChild(node, {
            type: 'element',
            tagName: 'span',
            properties: { className: ['sr-only'] },
            children: [{ type: 'text', value: 'Aspecto' }],
          });
        }
      },
    },
  };
}
