---
pergunta: "Como você implementaria um lock distribuído com Redis? Ele é seguro?"
tema: "System design"
nivel: pleno
idioma: pt
modulo: 15-system-design
intencao: "Separar quem copia a receita do SET NX de quem entende os limites de um lock baseado em tempo."
pontosChave:
  - "SET chave valor-aleatório NX PX: adquire e define a expiração num comando atômico"
  - "Liberar com um script Lua que só apaga se o valor ainda for o seu"
  - "O lock expira, e uma pausa de GC ou de rede faz dois processos se acharem donos"
  - "Para correção, o recurso precisa checar um fencing token crescente, ou uma versão ou constraint"
  - "Redlock: a crítica de Kleppmann (tempo e pausas) e a resposta de Sanfilippo; lock para eficiência é ok"
armadilha: "Dizer que o Redlock (ou o SET NX) garante exclusão mútua em qualquer situação, ou liberar o lock com um DEL simples."
---

"A versão básica é um comando só: `SET lock:fechamento <uuid> NX PX 30000`. O `NX` só grava se a chave não existir, e o `PX` define a expiração no mesmo comando, então não sobra lock eterno se o processo morrer. O valor é um UUID que identifica quem travou. Para liberar, eu uso um **script Lua** que compara o valor e só então apaga: se eu fizesse um `DEL` direto e o meu lock já tivesse expirado, eu apagaria o lock de outro processo.

Se ele é seguro depende do que ele protege. O problema de fundo é que o lock **expira por tempo**, e o processo não sabe que foi pausado. Uma pausa de GC, uma VM congelada ou um pacote atrasado, e o lock vence enquanto o dono ainda acha que o tem. Outro processo adquire, e os dois escrevem.

Martin Kleppmann mostrou isso na crítica ao **Redlock**, o algoritmo que adquire o lock na maioria de vários Redis independentes: mais nós não resolvem pausas nem relógios imprecisos. A proposta dele é o **fencing token**, um número que cresce a cada aquisição e que o recurso protegido verifica, recusando escritas com token menor que o maior já visto. O Redis não gera esse número com a garantia necessária, então ele vem de algo com ordem, como uma sequência do banco. O Salvatore Sanfilippo respondeu defendendo o Redlock sob certas premissas, e vale ler os dois lados.

Na prática, eu separo: lock para **eficiência** (evitar que dois nós rodem o mesmo job e gastem processamento à toa), o `SET NX PX` resolve. Lock para **correção** (duas escritas quebram o dado), o próprio recurso tem de se defender: `UPDATE ... WHERE versao = ?`, constraint única, ou fencing token. O lock vira otimização, não garantia."
