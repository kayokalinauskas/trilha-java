---
pergunta: "What is hexagonal architecture, and when would you not use it?"
tema: "Architecture (EN)"
nivel: pleno
idioma: en
modulo: 13-arquitetura
intencao: "Explaining ports and adapters and the dependency rule, and showing judgment about cost."
pontosChave:
  - "The core (domain + use cases) has no dependency on frameworks, databases or HTTP"
  - "Driving ports are use-case interfaces; driven ports are interfaces the core defines for what it needs"
  - "Adapters (REST controller, JPA repository, email) depend on the ports — dependencies point inward"
  - "Main benefit: the core is tested with fakes, fast, without Spring"
  - "Not worth it for simple CRUD services with little business logic"
armadilha: "Describing it as 'three layers with interfaces everywhere' without the dependency rule or the trade-off."
---

"Hexagonal architecture — ports and adapters — puts the business core in the middle and keeps it independent of technology. The core has the domain model and the use cases. It talks to the outside world only through **ports**, which are interfaces the core owns.

There are two kinds. **Driving ports** are the use cases, like `ReserveRoom`; a REST controller, a message consumer or a test calls them. **Driven ports** are what the core needs from the outside, like a `ReservationRepository` or a `Notifier`, written in the core's own terms. **Adapters** implement or call those ports: a JPA repository, an email sender, a controller.

The key rule is that dependencies point **inward**. Adapters know the core; the core knows no adapter and, ideally, not even Spring. I usually enforce that with ArchUnit, so a violation fails the build.

The payoff I've felt most is in tests: the core is tested with a `new` and a couple of fakes, in milliseconds. Swapping the database or adding a Kafka consumer means writing a new adapter.

When wouldn't I use it? For a simple CRUD service with almost no business rules, the extra interfaces and mappings are just ceremony. I'd use it where there's real logic to protect and more than one way in or out."
