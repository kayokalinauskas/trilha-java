---
pergunta: "Walk me through what happens when an HTTP request hits a Spring Boot application."
tema: "Spring (EN)"
nivel: pleno
idioma: en
modulo: 08-spring-boot
intencao: "Big-picture understanding of the framework, not just annotations."
pontosChave:
  - "Embedded server (Tomcat) accepts the connection; a thread (or virtual thread) handles it"
  - "Servlet filter chain, including Spring Security"
  - "DispatcherServlet finds the handler; argument resolution, JSON deserialization, validation"
  - "Controller → service → repository (transactions via proxies)"
  - "Response serialized; exceptions handled by @RestControllerAdvice"
---

"First, the **embedded server** — Tomcat by default — accepts the connection and assigns a thread to the request; with virtual threads enabled, that's a virtual thread. The request then goes through the **servlet filter chain**. That's where cross-cutting concerns live, including the **Spring Security** filter chain, which authenticates the request, for example by validating a JWT.

Next, the **DispatcherServlet** finds the matching controller method via the handler mappings. Argument resolvers bind path variables and query parameters, **Jackson** deserializes the JSON body, and **Bean Validation** runs if the parameter is annotated with `@Valid`.

The controller delegates to a **service**. If the service method is `@Transactional`, it's actually called through a **proxy** that opens and commits the transaction. The service uses a **repository**, where Spring Data and Hibernate turn method calls into SQL using a connection from the **HikariCP** pool.

On the way back, the return value is serialized to JSON. If an exception is thrown anywhere, a `@RestControllerAdvice` translates it into a **Problem Details** response with the right status code."
