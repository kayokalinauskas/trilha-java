---
pergunta: "How does a HashMap work internally?"
tema: "Data structures (EN)"
nivel: pleno
idioma: en
modulo: 03-colecoes-generics-excecoes
intencao: "Technical depth and the ability to explain it clearly in English."
pontosChave:
  - "Array of buckets indexed by the key's hashCode"
  - "equals() resolves collisions inside a bucket"
  - "Average O(1); resizes when load factor exceeds 0.75"
  - "Since Java 8, crowded buckets become balanced trees (O(log n) worst case)"
  - "Broken equals/hashCode contract or mutable keys break it"
---

"A HashMap is backed by an **array of buckets**. When I call `put`, it computes the key's `hashCode`, spreads the bits, and maps the result to a bucket index. If the bucket already has entries — a **collision** — it walks through them and uses `equals` to decide whether to replace an existing value or add a new entry. `get` follows the same path, so both are **O(1) on average**.

When the number of entries exceeds the **load factor**, 75% of the capacity by default, the map doubles the array and redistributes the entries — that's a **rehash**. Since Java 8, if a single bucket gets too many collisions, it turns into a balanced tree, so the worst case is O(log n) instead of O(n).

The practical implications: `equals` and `hashCode` must be consistent — equal objects must have the same hash code — and keys should be immutable. Also, HashMap is not thread-safe; for concurrent access I'd use `ConcurrentHashMap`."
