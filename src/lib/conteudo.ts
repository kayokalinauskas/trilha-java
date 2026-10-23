import { getCollection, type CollectionEntry } from 'astro:content';

export type Modulo = CollectionEntry<'modulos'>;
export type Aula = CollectionEntry<'aulas'>;
export type Exercicio = CollectionEntry<'exercicios'>;
export type Projeto = CollectionEntry<'projetos'>;

export const NIVEL_LABEL: Record<Modulo['data']['nivel'], string> = {
  base: 'Base',
  jr: 'Júnior',
  'jr-pleno': 'Jr → Pleno',
  pleno: 'Pleno',
  todos: 'Todos',
};

export const DIFICULDADE_LABEL: Record<Exercicio['data']['dificuldade'], string> = {
  facil: 'Fácil',
  medio: 'Médio',
  dificil: 'Difícil',
};

export async function getModulos(): Promise<Modulo[]> {
  const modulos = await getCollection('modulos');
  return modulos.sort((a, b) => a.data.numero - b.data.numero);
}

export async function getAulas(moduloId?: string): Promise<Aula[]> {
  const aulas = await getCollection('aulas', (a) => !moduloId || a.data.modulo.id === moduloId);
  const modulos = await getModulos();
  const ordemModulo = new Map(modulos.map((m) => [m.id, m.data.numero]));
  return aulas.sort(
    (a, b) =>
      (ordemModulo.get(a.data.modulo.id) ?? 0) - (ordemModulo.get(b.data.modulo.id) ?? 0) ||
      a.data.ordem - b.data.ordem,
  );
}

export async function getExercicios(moduloId?: string): Promise<Exercicio[]> {
  const exercicios = await getCollection('exercicios', (e) => !moduloId || e.data.modulo.id === moduloId);
  return exercicios.sort((a, b) => a.data.codigo.localeCompare(b.data.codigo));
}

export const NIVEL_PROJETO: Record<Projeto['data']['nivel'], string> = {
  junior: 'Júnior',
  pleno: 'Pleno',
  senior: 'Sênior',
};

export async function getProjetos(): Promise<Projeto[]> {
  const projetos = await getCollection('projetos');
  const ordem = { junior: 0, pleno: 1, senior: 2 };
  return projetos.sort((a, b) => ordem[a.data.nivel] - ordem[b.data.nivel] || a.data.ordem - b.data.ordem);
}

/** Profundidade de cada módulo no grafo de pré-requisitos (usada para desenhar o mapa). */
export function camadas(modulos: Modulo[]): Modulo[][] {
  const porId = new Map(modulos.map((m) => [m.id, m]));
  const profundidade = new Map<string, number>();
  const calcular = (id: string): number => {
    if (profundidade.has(id)) return profundidade.get(id)!;
    const m = porId.get(id);
    const d = !m || m.data.requisitos.length === 0 ? 0 : 1 + Math.max(...m.data.requisitos.map(calcular));
    profundidade.set(id, d);
    return d;
  };
  const resultado: Modulo[][] = [];
  for (const m of modulos) {
    const d = calcular(m.id);
    (resultado[d] ??= []).push(m);
  }
  return resultado.filter(Boolean);
}

export function formatarDuracao(min: number): string {
  if (min < 60) return `${min} min`;
  const h = Math.floor(min / 60);
  const r = min % 60;
  return r ? `${h}h${String(r).padStart(2, '0')}` : `${h}h`;
}

export function pad2(n: number): string {
  return String(n).padStart(2, '0');
}
