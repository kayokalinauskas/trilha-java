---
pergunta: "Walk me through the CI/CD pipeline you would set up for a Spring Boot service."
tema: "DevOps (EN)"
nivel: pleno
idioma: en
modulo: 14-devops
intencao: "Showing you own your code all the way to production: fast feedback, a single tested artifact, safe rollout and rollback."
pontosChave:
  - "CI on every pull request: build, unit and integration tests (Testcontainers), static checks"
  - "Build the artifact once; promote the same image through environments"
  - "Tag images with the commit SHA, never latest"
  - "Secrets from the platform (GitHub secrets, a vault), never in the repo"
  - "Progressive rollout with health checks, and a tested rollback path"
  - "Database migrations must be backward compatible with the previous version"
armadilha: "Describing only the YAML syntax, or rebuilding the image per environment, so the thing in production is not the thing that was tested."
---

"I split it into **CI** and **CD**.

**CI** runs on every pull request and on every push to main. It checks out the code, sets up Java 25 with the Maven cache, and runs `mvn -B verify`. That covers compilation, unit tests, integration tests with Testcontainers against a real PostgreSQL, and static checks like the architecture rules. Branch protection makes a green build a requirement for merging. The goal is feedback in a few minutes. If the pipeline gets slow, people start working around it.

**CD** starts after the merge. I **build the image once** from the jar that was just tested, and tag it with the commit SHA, never `latest`. I also scan it for vulnerabilities and push it to the registry. That **same image** is then promoted: first to staging, where smoke tests run against the health endpoints, then to production. Configuration and secrets come from the environment and a secret store, never from the image or the repo.

For the production rollout I use a **rolling or canary deployment**. Kubernetes only sends traffic to pods whose readiness probe passes, and I watch the error rate and latency dashboards during the rollout. **Rollback** means redeploying the previous SHA. That only works if database migrations are **backward compatible**: I add columns first, deploy, and drop the old ones in a later release. Finally, I keep the whole path automated but observable. Every deploy is tied to a commit, so when a metric moves, we can see exactly what changed."
