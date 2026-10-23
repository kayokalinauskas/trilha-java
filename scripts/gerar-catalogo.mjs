// Gera DOCUMENTACAO.md a partir do conteúdo real (src/content), para que a documentação
// nunca fique dessincronizada das aulas e exercícios publicados.
// Uso: npm run docs:catalogo
import { readFileSync, readdirSync, writeFileSync, statSync } from 'node:fs';
import { join, relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { load as carregarYaml } from 'js-yaml';

const raiz = fileURLToPath(new URL('..', import.meta.url));
const conteudo = join(raiz, 'src', 'content');

function arquivos(dir, ext) {
  return readdirSync(dir).flatMap((nome) => {
    const caminho = join(dir, nome);
    return statSync(caminho).isDirectory() ? arquivos(caminho, ext) : caminho.endsWith(ext) ? [caminho] : [];
  });
}

function frontmatter(caminho) {
  const texto = readFileSync(caminho, 'utf8');
  const m = texto.match(/^---\r?\n([\s\S]*?)\r?\n---/);
  return m ? carregarYaml(m[1]) : {};
}

const idDe = (base, caminho, ext) => relative(base, caminho).split(sep).join('/').replace(ext, '');
const pad = (n) => String(n).padStart(2, '0');
const NIVEL = { base: 'Base', jr: 'Júnior', 'jr-pleno': 'Jr → Pleno', pleno: 'Pleno', todos: 'Todos' };
const DIF = { facil: '🟢 Fácil', medio: '🟡 Médio', dificil: '🔴 Difícil' };

const modulos = JSON.parse(readFileSync(join(conteudo, 'modulos.json'), 'utf8')).sort((a, b) => a.numero - b.numero);
const aulas = arquivos(join(conteudo, 'aulas'), '.mdx').map((c) => ({ id: idDe(join(conteudo, 'aulas'), c, '.mdx'), ...frontmatter(c) }));
const exercicios = arquivos(join(conteudo, 'exercicios'), '.mdx').map((c) => ({ id: idDe(join(conteudo, 'exercicios'), c, '.mdx'), ...frontmatter(c) }));
const perguntas = arquivos(join(conteudo, 'entrevista'), '.md').map((c) => frontmatter(c));
let projetos = [];
try {
  projetos = arquivos(join(conteudo, 'projetos'), '.mdx').map((c) => ({ id: idDe(join(conteudo, 'projetos'), c, '.mdx'), ...frontmatter(c) }));
} catch {
  /* pasta ainda não existe */
}
const NIVEL_PROJETO = { junior: 'Júnior', pleno: 'Pleno', senior: 'Sênior' };
const ORDEM_NIVEL = { junior: 0, pleno: 1, senior: 2 };
projetos.sort((a, b) => ORDEM_NIVEL[a.nivel] - ORDEM_NIVEL[b.nivel] || a.ordem - b.ordem);

const minutos = aulas.reduce((s, a) => s + (a.duracao ?? 0), 0);
const cartoes = aulas.reduce((s, a) => s + (a.flashcards?.length ?? 0), 0);
const disponiveis = modulos.filter((m) => m.status === 'disponivel');

let md = `# Documentação da Trilha Java Backend

> Arquivo **gerado automaticamente** por \`npm run docs:catalogo\` a partir de \`src/content/\`. Não edite à mão.
> Para a visão geral do projeto, veja o [README](README.md). Para escrever conteúdo novo, veja [docs/guia-de-autoria.md](docs/guia-de-autoria.md).

## Visão geral

| Item | Quantidade |
|---|---|
| Módulos no currículo | ${modulos.length} (${disponiveis.length} publicados) |
| Aulas publicadas | ${aulas.length} (~${Math.round(minutos / 60)}h de leitura e prática guiada) |
| Exercícios com solução | ${exercicios.length} (${exercicios.filter((e) => e.java?.teste).length} com testes para o aluno, ${exercicios.filter((e) => e.java && !e.java.teste).length} com starter e solução em Java sem testes prontos, ${exercicios.filter((e) => !e.java).length} escritos) |
| Projetos de portfólio | ${projetos.length} (${Object.keys(NIVEL_PROJETO).map((n) => `${projetos.filter((p) => p.nivel === n).length} ${NIVEL_PROJETO[n]}`).join(', ')}) |
| Flashcards | ${cartoes} |
| Perguntas do simulador de entrevista | ${perguntas.length} (${perguntas.filter((p) => p.idioma === 'en').length} em inglês) |

## Currículo

| # | Módulo | Nível | Horas | Status | Pré-requisitos |
|---|---|---|---|---|---|
${modulos
  .map(
    (m) =>
      `| ${pad(m.numero)} | **${m.titulo}** | ${NIVEL[m.nivel]} | ~${m.horas}h | ${m.status === 'disponivel' ? '✅ publicado' : '🕓 em breve'} | ${m.requisitos.map((r) => r.slice(0, 2)).join(', ') || '—'} |`,
  )
  .join('\n')}

`;

for (const m of modulos) {
  const aulasM = aulas.filter((a) => a.modulo === m.id).sort((a, b) => a.ordem - b.ordem);
  const exM = exercicios.filter((e) => e.modulo === m.id).sort((a, b) => a.codigo.localeCompare(b.codigo));
  md += `### ${pad(m.numero)} · ${m.titulo}${m.status === 'em-breve' ? ' (em breve)' : ''}\n\n${m.resumo}\n\n`;
  md += `**Objetivos:**\n\n${m.objetivos.map((o) => `- ${o}`).join('\n')}\n\n`;
  if (m.marcoProjeto) md += `**Projeto integrador:** ${m.marcoProjeto}\n\n`;
  if (aulasM.length) {
    md += `**Aulas:**\n\n| # | Aula | Duração | Flashcards |\n|---|---|---|---|\n`;
    md += aulasM.map((a) => `| ${a.ordem} | ${a.titulo} | ${a.duracao} min | ${a.flashcards?.length ?? 0} |`).join('\n') + '\n\n';
  } else {
    md += `**Programa:** ${m.topicos.join(' · ')}\n\n`;
  }
  if (exM.length) {
    md += `**Exercícios:**\n\n| Código | Exercício | Dificuldade | Tempo | Testes |\n|---|---|---|---|---|\n`;
    md += exM
      .map((e) => `| ${e.codigo} | ${e.titulo} | ${DIF[e.dificuldade]} | ${e.tempo} | ${e.java ? (e.java.teste ? `\`java/${e.java.modulo}\` · testes: \`${e.java.teste}\`` : `\`java/${e.java.modulo}\` · starter + solução`) : '—'} |`)
      .join('\n');
    md += '\n\n';
  }
}

if (projetos.length) {
  md += `## Projetos de portfólio

Especificações completas para construir do zero e mostrar em entrevistas (sem código de referência).

| Nível | Projeto | Domínio | Horas | Marcos | Stack |
|---|---|---|---|---|---|
`;
  md += projetos
    .map((p) => `| ${NIVEL_PROJETO[p.nivel]} | **${p.titulo}** | ${p.dominio} | ~${p.horas}h | ${p.marcos.length} | ${p.stack.join(', ')} |`)
    .join('\n');
  md += '\n\n';
}

const temas = [...new Set(perguntas.map((p) => p.tema))].sort();
md += `## Simulador de entrevista\n\n| Tema | Perguntas |\n|---|---|\n${temas
  .map((t) => `| ${t} | ${perguntas.filter((p) => p.tema === t).length} |`)
  .join('\n')}\n`;

writeFileSync(join(raiz, 'DOCUMENTACAO.md'), md);
console.log(`DOCUMENTACAO.md gerado: ${modulos.length} módulos, ${aulas.length} aulas, ${exercicios.length} exercícios, ${projetos.length} projetos, ${perguntas.length} perguntas.`);
