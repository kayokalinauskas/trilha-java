import { defineCollection, reference } from 'astro:content';
import { file, glob } from 'astro/loaders';
import { z } from 'astro/zod';

const nivel = z.enum(['base', 'jr', 'jr-pleno', 'pleno', 'todos']);

const modulos = defineCollection({
  loader: file('src/content/modulos.json'),
  schema: z.object({
    numero: z.number().int().min(0),
    titulo: z.string(),
    resumo: z.string(),
    nivel,
    status: z.enum(['disponivel', 'em-breve']),
    horas: z.number().positive(),
    requisitos: z.array(z.string()).default([]),
    objetivos: z.array(z.string()).min(1),
    topicos: z.array(z.string()).min(1),
    marcoProjeto: z.string().optional(),
  }),
});

const flashcard = z.object({
  frente: z.string(),
  verso: z.string(),
});

const aulas = defineCollection({
  loader: glob({ pattern: '**/*.mdx', base: './src/content/aulas' }),
  schema: z.object({
    titulo: z.string(),
    descricao: z.string(),
    modulo: reference('modulos'),
    ordem: z.number().int().positive(),
    duracao: z.number().int().positive().describe('minutos de leitura + prática'),
    objetivos: z.array(z.string()).min(1),
    tags: z.array(z.string()).default([]),
    flashcards: z.array(flashcard).default([]),
  }),
});

const exercicios = defineCollection({
  loader: glob({ pattern: '**/*.mdx', base: './src/content/exercicios' }),
  schema: z.object({
    titulo: z.string(),
    codigo: z.string().regex(/^\d{2}\.\d{2}$/),
    resumo: z.string(),
    modulo: reference('modulos'),
    ordem: z.number().int().positive(),
    dificuldade: z.enum(['facil', 'medio', 'dificil']),
    tempo: z.string(),
    conceitos: z.array(z.string()).min(1),
    aulas: z.array(reference('aulas')).default([]),
    /** marco do projeto integrador Fintech Core (1 a 6), quando o exercício for um marco */
    marco: z.number().int().min(1).max(6).optional(),
    java: z
      .object({
        modulo: z.string(),
        /** Formato antigo (módulos 00–09): classe de teste que confere o exercício. */
        teste: z.string().optional(),
        /** Formato novo (sem testes): arquivos do starter onde a pessoa implementa, relativos a java/<modulo>/starter. */
        arquivos: z.array(z.string().regex(/^src\/(main|test)\//, 'caminho relativo ao starter, ex.: src/main/java/...')).optional(),
      })
      .optional(),
  }),
});

const entrevista = defineCollection({
  loader: glob({ pattern: '**/*.md', base: './src/content/entrevista' }),
  schema: z.object({
    pergunta: z.string(),
    tema: z.string(),
    nivel: z.enum(['jr', 'pleno']),
    idioma: z.enum(['pt', 'en']).default('pt'),
    intencao: z.string(),
    pontosChave: z.array(z.string()).min(1),
    armadilha: z.string().optional(),
    modulo: reference('modulos').optional(),
  }),
});

const projetos = defineCollection({
  loader: glob({ pattern: '**/*.mdx', base: './src/content/projetos' }),
  schema: z.object({
    titulo: z.string(),
    /** uma frase: o que o sistema faz e para quem */
    resumo: z.string(),
    nivel: z.enum(['junior', 'pleno', 'senior']),
    ordem: z.number().int().positive(),
    /** domínio de negócio, ex.: "Saúde · agendamentos" */
    dominio: z.string(),
    /** estimativa total de horas de dedicação */
    horas: z.number().int().positive(),
    stack: z.array(z.string()).min(3),
    /** o que um recrutador ou gestor enxerga no projeto pronto */
    competencias: z.array(z.string()).min(3),
    /** módulos da trilha que o projeto exercita (e que convém ter feito antes) */
    modulos: z.array(reference('modulos')).min(1),
    /** marcos do plano de execução, na ordem; o id vira a âncora "#marco-<id>" e a chave do progresso */
    marcos: z
      .array(z.object({ id: z.string().regex(/^[a-z0-9-]+$/), titulo: z.string(), resumo: z.string() }))
      .min(4),
  }),
});

export const collections = { modulos, aulas, exercicios, entrevista, projetos };
