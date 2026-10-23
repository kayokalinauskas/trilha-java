---
pergunta: "Explique o SOLID com um exemplo de cada princípio."
tema: "Arquitetura"
nivel: pleno
idioma: pt
modulo: 13-arquitetura
intencao: "Se você aplica os princípios de verdade ou só recita as siglas."
pontosChave:
  - "S: uma razão para mudar"
  - "O: estender sem modificar"
  - "L: subclasses substituíveis"
  - "I: interfaces pequenas e específicas"
  - "D: depender de abstrações (injeção de dependência)"
armadilha: "Recitar as definições sem nenhum exemplo do próprio código."
---

"**S — Responsabilidade única**: uma classe tem um motivo para mudar. Um `PedidoService` que calcula frete, envia e-mail e gera nota fiscal tem três; separo em componentes.

**O — Aberto/Fechado**: estendo sem modificar. Um meio de pagamento novo é uma classe nova que implementa `MeioDePagamento`, sem mexer no checkout.

**L — Substituição de Liskov**: uma subclasse precisa funcionar onde a superclasse funciona. `Quadrado extends Retangulo` é o contraexemplo clássico.

**I — Segregação de interfaces**: prefiro várias interfaces pequenas a uma gigante. Um `RelatorioExportavel` não deveria obrigar a implementar `enviarPorEmail`.

**D — Inversão de dependência**: dependo de abstrações. O serviço depende de `PedidoRepository`, uma interface, e não de uma classe JDBC concreta. É o que o Spring faz com injeção de dependência, e é o que torna o código testável com mocks.

Uso SOLID como heurística, não como dogma: aplicar tudo em um CRUD simples gera abstração demais."
