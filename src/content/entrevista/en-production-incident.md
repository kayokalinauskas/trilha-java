---
pergunta: "An endpoint suddenly starts returning errors in production. Walk me through how you would investigate."
tema: "DevOps (EN)"
nivel: pleno
idioma: en
modulo: 14-devops
intencao: "Structured troubleshooting under pressure: mitigate first, use data (metrics, logs, traces) and communicate."
pontosChave:
  - "Assess impact and communicate: who is affected, since when"
  - "Mitigate first: roll back or disable the feature flag if a deploy caused it"
  - "Metrics to scope it, traces to locate it, logs to explain it"
  - "Check recent changes: deploys, config, dependencies, traffic"
  - "Blameless postmortem with follow-up actions"
armadilha: "Starting to read code or SSH-ing into a server before checking dashboards, or trying to find the root cause while users are still affected."
---

"First, I'd **assess the impact**: the error rate, which endpoints, and when it started. I'd post an update in the incident channel so people know someone is on it.

Then I'd **look for what changed**. Most incidents follow a change: a deploy, a config update, a dependency, or a traffic spike. If a deploy happened right before the errors, **I'd roll it back first** and investigate after. Restoring service comes before finding the root cause.

To investigate, I'd go from wide to narrow. **Metrics** show the scope: is it every instance or one, every customer or one region? **Traces** show where the time or the error is: our code, the database, or a downstream API. **Logs** filtered by the trace ID explain why, for example a timeout from the payment provider.

If it's a downstream failure, I'd check that timeouts and the circuit breaker are behaving, so the problem doesn't spread. After it's resolved, I'd write a **blameless postmortem**: timeline, root cause, and actions like a missing alert or a test that would have caught it."
