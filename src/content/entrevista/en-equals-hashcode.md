---
pergunta: "Why do you need to override hashCode when you override equals?"
tema: "Java (EN)"
nivel: jr
idioma: en
modulo: 03-colecoes-generics-excecoes
intencao: "A classic Java question: whether you know the contract and what breaks in hash-based collections."
pontosChave:
  - "Contract: equal objects must have the same hash code"
  - "HashMap and HashSet use hashCode to find the bucket, then equals"
  - "Breaking it: duplicates in a set, get() returning null"
  - "Use the same fields in both; prefer immutable fields"
  - "Records generate both automatically"
armadilha: "Saying the opposite, that objects with the same hash code must be equal. Collisions are allowed."
---

"Because of the **contract** between them: if two objects are **equal**, they **must return the same hash code**. The reverse isn't required. Different objects can share a hash code, which is just a collision.

Hash-based collections depend on this. A `HashMap` uses `hashCode()` to **choose the bucket**, and only then uses `equals()` to compare the keys in that bucket. If I override `equals` but not `hashCode`, two equal objects will probably land in different buckets. The result is **duplicates in a `HashSet`**, and `map.get(key)` returning `null` for a key that's actually there.

In practice, I use **the same fields in both methods**, and ideally immutable ones. If a field used in `hashCode` changes while the object is inside a `HashSet`, the object gets 'lost' in the wrong bucket.

For simple value objects, I use a **`record`**, which generates `equals`, `hashCode` and `toString` from its components."
