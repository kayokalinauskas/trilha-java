---
pergunta: "How does JWT authentication work, and what are its downsides?"
tema: "Security (EN)"
nivel: pleno
idioma: en
modulo: 11-seguranca
intencao: "Whether you understand what a JWT guarantees (integrity, not secrecy) and the trade-offs of stateless tokens."
pontosChave:
  - "Header, payload (claims) and signature; signed, not encrypted"
  - "The server validates signature, expiration, issuer and audience"
  - "Stateless: no session lookup, which scales easily"
  - "Downside: hard to revoke, so use short-lived access tokens and refresh tokens"
  - "No sensitive data in the payload; prefer an identity provider"
armadilha: "Saying the JWT is encrypted and putting sensitive data in it, or forgetting that a stolen token stays valid until it expires."
---

"A **JWT** has three parts: a header, a payload with **claims** like the user ID, roles and expiration, and a **signature**. The authorization server signs the token. Our API, acting as a **resource server**, validates the signature with the public key and checks the expiration, issuer and audience. If everything checks out, it trusts the claims **without calling a database or a session store**.

That's the main benefit: it's **stateless**, so any instance can validate a request, and it scales easily.

The downsides are real, though. A JWT is **signed, not encrypted**: anyone can decode the payload, so no sensitive data goes in it. And it's **hard to revoke**: a stolen token is valid until it expires. The usual answer is **short-lived access tokens**, a few minutes, plus **refresh tokens** that can be revoked on the server. For immediate revocation, you need some state again, like a denylist.

In practice, I wouldn't build token issuing myself. I'd use an identity provider with OAuth2 and OpenID Connect, and configure Spring Security as a resource server."
