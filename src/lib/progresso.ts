/**
 * Progresso do aluno, salvo apenas no navegador (localStorage).
 * Nada sai da máquina: exportar/importar é a forma de levar para outro computador.
 */

export const STORAGE_KEY = 'trilha-java:v2';
const EVENTO = 'progresso:mudou';

export type StatusExercicio = 'fazendo' | 'concluido';

export interface CartaoSRS {
  /** fator de facilidade (SM-2) */
  ef: number;
  /** intervalo em dias */
  intervalo: number;
  repeticoes: number;
  /** data (AAAA-MM-DD) da próxima revisão */
  proxima: string;
}

export interface Progresso {
  versao: 2;
  aulas: Record<string, { concluida: string }>;
  exercicios: Record<string, { status: StatusExercicio; viuSolucao?: boolean; dicas?: number; atualizado: string }>;
  quizzes: Record<string, { acertos: number; total: number; em: string }>;
  flashcards: Record<string, CartaoSRS>;
  entrevista: Record<string, { nota: number; em: string }>;
  /** projetos de portfólio: ids dos marcos concluídos */
  projetos: Record<string, { marcos: string[]; atualizado: string }>;
  atividade: Record<string, number>;
}

function vazio(): Progresso {
  return { versao: 2, aulas: {}, exercicios: {}, quizzes: {}, flashcards: {}, entrevista: {}, projetos: {}, atividade: {} };
}

export function hoje(d = new Date()): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function valido(obj: unknown): obj is Progresso {
  return !!obj && typeof obj === 'object' && (obj as Progresso).versao === 2;
}

export function ler(): Progresso {
  try {
    const bruto = localStorage.getItem(STORAGE_KEY);
    if (!bruto) return vazio();
    const dados = JSON.parse(bruto);
    return valido(dados) ? { ...vazio(), ...dados } : vazio();
  } catch {
    return vazio();
  }
}

function salvar(p: Progresso) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(p));
  } catch {
    /* modo privado ou armazenamento bloqueado: segue sem persistir */
  }
  window.dispatchEvent(new CustomEvent(EVENTO));
}

export function atualizar(fn: (p: Progresso) => void, contarAtividade = true) {
  const p = ler();
  fn(p);
  if (contarAtividade) {
    const d = hoje();
    p.atividade[d] = (p.atividade[d] ?? 0) + 1;
  }
  salvar(p);
}

export function aoMudar(cb: () => void) {
  window.addEventListener(EVENTO, cb);
  window.addEventListener('storage', (e) => e.key === STORAGE_KEY && cb());
}

/* ---------------- aulas ---------------- */
export function aulaConcluida(id: string): boolean {
  return !!ler().aulas[id];
}
export function marcarAula(id: string, concluida: boolean) {
  atualizar((p) => {
    if (concluida) p.aulas[id] = { concluida: new Date().toISOString() };
    else delete p.aulas[id];
  }, concluida);
}

/* ---------------- exercícios ---------------- */
export function statusExercicio(id: string) {
  return ler().exercicios[id];
}
export function marcarExercicio(id: string, status: StatusExercicio | null) {
  atualizar((p) => {
    if (!status) {
      delete p.exercicios[id];
      return;
    }
    const atual = p.exercicios[id];
    p.exercicios[id] = { ...atual, status, atualizado: new Date().toISOString() };
  }, status === 'concluido');
}
export function registrarSolucaoVista(id: string) {
  atualizar((p) => {
    const atual = p.exercicios[id] ?? { status: 'fazendo' as const, atualizado: '' };
    p.exercicios[id] = { ...atual, viuSolucao: true, atualizado: new Date().toISOString() };
  }, false);
}
export function registrarDica(id: string, nivel: number) {
  atualizar((p) => {
    const atual = p.exercicios[id] ?? { status: 'fazendo' as const, atualizado: '' };
    p.exercicios[id] = { ...atual, dicas: Math.max(atual.dicas ?? 0, nivel), atualizado: new Date().toISOString() };
  }, false);
}

/* ---------------- quizzes ---------------- */
export function registrarQuiz(id: string, acertos: number, total: number) {
  atualizar((p) => {
    p.quizzes[id] = { acertos, total, em: new Date().toISOString() };
  });
}

/* ---------------- flashcards (SM-2) ---------------- */
export type Qualidade = 0 | 3 | 4 | 5;

/**
 * SM-2 simplificado. qualidade: 0 = errei, 3 = difícil, 4 = bom, 5 = fácil.
 * Devolve o novo estado do cartão (sem a data), para ser usado tanto na revisão quanto na prévia dos botões.
 */
export function proximoEstado(atual: CartaoSRS | undefined, qualidade: Qualidade): Omit<CartaoSRS, 'proxima'> {
  const c = { ef: atual?.ef ?? 2.5, intervalo: atual?.intervalo ?? 0, repeticoes: atual?.repeticoes ?? 0 };
  if (qualidade < 3) {
    return { ef: Math.max(1.3, c.ef - 0.2), intervalo: 1, repeticoes: 0 };
  }
  const base = c.repeticoes === 0 ? 1 : c.repeticoes === 1 ? 3 : c.intervalo * c.ef;
  const fator = qualidade === 3 ? 0.6 : qualidade === 5 ? 1.5 : 1;
  const intervalo = Math.max(1, Math.round(base * fator + (qualidade === 5 && c.repeticoes === 0 ? 2 : 0)));
  const ef = Math.max(1.3, c.ef + (0.1 - (5 - qualidade) * (0.08 + (5 - qualidade) * 0.02)));
  return { ef, intervalo, repeticoes: c.repeticoes + 1 };
}

export function revisarCartao(id: string, qualidade: Qualidade) {
  atualizar((p) => {
    const novo = proximoEstado(p.flashcards[id], qualidade);
    const prox = new Date();
    prox.setDate(prox.getDate() + novo.intervalo);
    p.flashcards[id] = { ...novo, proxima: hoje(prox) };
  });
}

/* ---------------- entrevista ---------------- */
export function avaliarResposta(id: string, nota: number) {
  atualizar((p) => {
    p.entrevista[id] = { nota, em: new Date().toISOString() };
  });
}

/* ---------------- projetos de portfólio ---------------- */
export function marcosConcluidos(projeto: string): string[] {
  return ler().projetos[projeto]?.marcos ?? [];
}
export function marcarMarco(projeto: string, marco: string, concluido: boolean) {
  atualizar((p) => {
    const atuais = new Set(p.projetos[projeto]?.marcos ?? []);
    if (concluido) atuais.add(marco);
    else atuais.delete(marco);
    if (atuais.size === 0) delete p.projetos[projeto];
    else p.projetos[projeto] = { marcos: [...atuais], atualizado: new Date().toISOString() };
  }, concluido);
}

/* ---------------- sequência de estudo ---------------- */
export function sequenciaDias(p = ler()): number {
  let dias = 0;
  const d = new Date();
  if (!p.atividade[hoje(d)]) d.setDate(d.getDate() - 1); // hoje ainda não conta como quebra
  while (p.atividade[hoje(d)]) {
    dias++;
    d.setDate(d.getDate() - 1);
  }
  return dias;
}

/* ---------------- backup ---------------- */
export function exportar() {
  const blob = new Blob([JSON.stringify(ler(), null, 2)], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `trilha-java-progresso-${hoje()}.json`;
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}

export async function importar(arquivo: File): Promise<boolean> {
  try {
    const dados = JSON.parse(await arquivo.text());
    if (!valido(dados)) return false;
    salvar({ ...vazio(), ...dados });
    return true;
  } catch {
    return false;
  }
}

export function zerar() {
  salvar(vazio());
}

/* ---------------- feedback ---------------- */
export function toast(mensagem: string) {
  let regiao = document.querySelector<HTMLElement>('.toast-region');
  if (!regiao) {
    regiao = document.createElement('div');
    regiao.className = 'toast-region';
    regiao.setAttribute('role', 'status');
    regiao.setAttribute('aria-live', 'polite');
    document.body.append(regiao);
  }
  const t = document.createElement('div');
  t.className = 'toast';
  t.textContent = mensagem;
  regiao.append(t);
  setTimeout(() => t.remove(), 3200);
}
