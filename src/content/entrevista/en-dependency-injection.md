---
pergunta: "What is dependency injection, and why does Spring use it?"
tema: "Spring (EN)"
nivel: jr
idioma: en
modulo: 08-spring-boot
intencao: "Whether you understand the concept behind the framework (inversion of control) and its practical benefits."
pontosChave:
  - "Objects receive their dependencies instead of creating them"
  - "Inversion of control: the container creates and wires the beans"
  - "Constructor injection: explicit, immutable, easy to test"
  - "Benefits: loose coupling, swapping implementations, testing with fakes"
armadilha: "Describing only annotations ('you put @Autowired on the field') without explaining the concept or why field injection is discouraged."
---

"**Dependency injection** means a class **receives the objects it depends on** instead of creating them itself. A `TransferService` doesn't call `new JdbcAccountRepository()`. It declares that it needs an `AccountRepository`, and someone else provides it.

In Spring, that someone is the **IoC container**. At startup, it creates the beans, figures out the dependencies between them and wires everything together. That's **inversion of control**: the framework manages the object graph, not my code.

I prefer **constructor injection**. Dependencies are explicit, the fields can be `final`, and with a single constructor I don't even need `@Autowired`. It also makes the class easy to use without Spring: in a unit test, I just call the constructor with a fake repository.

The benefits are **loose coupling** and **testability**. I can swap an implementation, for example a different payment provider, through configuration, and I can test business logic without starting the whole application."
