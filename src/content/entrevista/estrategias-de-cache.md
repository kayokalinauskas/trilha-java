---
pergunta: "Quais estratégias de cache você conhece? Como você invalida o cache e evita derrubar o banco quando uma chave expira?"
tema: "System design"
nivel: pleno
idioma: pt
modulo: 15-system-design
intencao: "Ver se o candidato conhece os padrões além do nome, sabe onde cada um falha e já pensou em invalidação e stampede."
pontosChave:
  - "Cache-aside (o mais comum), read-through, write-through e write-behind, com o risco de cada um"
  - "Invalidar é apagar a chave depois do commit, não atualizar o valor (corrida entre escritas)"
  - "TTL sempre, com jitter: é a rede de segurança de qualquer invalidação"
  - "Stampede: single-flight na instância, lock no Redis entre instâncias, renovação antecipada"
  - "Cache negativo com TTL curto, e o Redis fora do ar tratado como lentidão, não como erro"
armadilha: "Responder só \"coloco um Redis com @Cacheable\" sem falar de invalidação, TTL e do que acontece quando a chave expira sob carga."
---

"Na maioria dos casos eu uso **cache-aside**: a aplicação lê do cache; no *miss*, lê do banco e grava no cache com TTL. O cache é descartável, e se ele some o sistema só fica mais lento. **Read-through** é a mesma ideia com o cache chamando o *loader*, como o `LoadingCache` do Caffeine. **Write-through** grava no cache e no banco na mesma operação: bom quando o dado é lido logo depois de escrito, com o custo de uma escrita mais lenta. **Write-behind** grava no cache e atualiza o banco depois, em lote: rápido, mas perde escritas se o cache cair. Uso para contadores e métricas, nunca para saldo.

A parte difícil é a **invalidação**. Na escrita, eu gravo no banco e **apago** a chave, depois do commit, em vez de gravar o valor novo no cache. Se duas atualizações concorrentes gravarem no cache, a ordem pode sair invertida em relação ao banco, e o valor velho fica lá. Apagar duas vezes dá no mesmo. Mesmo assim sobra uma janela rara (uma leitura lenta que recoloca o valor antigo), e por isso **toda chave tem TTL**, com um *jitter* aleatório para que chaves gravadas juntas não expirem juntas.

Para não derrubar o banco quando uma chave quente expira, o *cache stampede*, eu faço **single-flight**: só uma requisição recarrega e as outras esperam o mesmo resultado. Numa instância, o Caffeine faz isso, ou um `ConcurrentHashMap` de `CompletableFuture`; o `@Cacheable(sync = true)` também, mas só dentro da instância. Entre instâncias, um lock curto no Redis com `SET NX PX`, ou renovação antecipada da chave em segundo plano.

Dois detalhes que costumam faltar: **cache negativo**, gravar também o 'não existe' com TTL curto, para que ids inventados não martelem o banco; e tratar o **Redis fora do ar** como lentidão, com timeout curto no cliente e a leitura caindo direto no banco, em vez de devolver erro para o usuário."
