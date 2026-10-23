/** Monta um link interno respeitando o `base` configurado (ex.: GitHub Pages em /repo/). */
export function url(path = ''): string {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '');
  const clean = path.replace(/^\//, '');
  if (!clean) return `${base}/`;
  const withSlash = /[.#?]/.test(clean.split('/').pop() ?? '') ? clean : `${clean.replace(/\/$/, '')}/`;
  return `${base}/${withSlash}`;
}
