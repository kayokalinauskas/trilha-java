---
pergunta: "How does Spring Security process an incoming request?"
tema: "Spring (EN)"
nivel: pleno
idioma: en
modulo: 11-seguranca
intencao: "Understanding the filter chain instead of copying configuration."
pontosChave:
  - "Servlet filters before the DispatcherServlet: FilterChainProxy picks a SecurityFilterChain"
  - "Authentication filters (e.g. BearerTokenAuthenticationFilter) populate the SecurityContext"
  - "ExceptionTranslationFilter turns security exceptions into 401/403"
  - "AuthorizationFilter applies the request rules; method security with @PreAuthorize"
  - "That's why @ControllerAdvice doesn't see 401/403: use an AuthenticationEntryPoint"
armadilha: "Describing it as annotations on controllers, with no mention of filters."
---

"Spring Security is a chain of servlet filters that runs **before** Spring MVC. The `FilterChainProxy` receives the request and picks the matching `SecurityFilterChain` — you can have several, for example one for `/api/**` with JWT and another for an admin UI with form login.

Inside the chain, each filter has one job. An authentication filter — say the `BearerTokenAuthenticationFilter` — reads the token, validates it through a `JwtDecoder` and stores an `Authentication` with the user's authorities in the `SecurityContext`. The `ExceptionTranslationFilter` turns security exceptions into a 401 or a 403. Then the `AuthorizationFilter` checks the rules I configured, like `hasRole("ADMIN")` on `/admin/**`. Only then does the request reach the `DispatcherServlet` and my controller. Method-level rules with `@PreAuthorize` run later, around the service call.

One practical consequence: a 401 or 403 never reaches my `@RestControllerAdvice`, because it happens in a filter. To return Problem Details for those, I plug in an `AuthenticationEntryPoint` and an `AccessDeniedHandler`."
