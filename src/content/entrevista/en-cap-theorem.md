---
pergunta: "Can you explain the CAP theorem? How does it affect the way you design a system?"
tema: "System design (EN)"
nivel: pleno
idioma: en
modulo: 15-system-design
intencao: "Checking whether the candidate knows what CAP actually states — and its limits — instead of reciting 'pick 2 of 3'."
pontosChave:
  - "C means linearizability, A means every non-failing node answers; the trade-off only applies during a partition"
  - "Partitions are not optional, so 'CA' is not a real choice for a distributed system"
  - "PACELC: without a partition, the everyday trade-off is latency vs consistency"
  - "The choice is made per operation, not per system: balance strongly consistent, feed eventually consistent"
  - "Concrete examples: synchronous replicas, quorum settings, read-your-writes"
armadilha: "\"Pick any 2 of 3\" and labelling a single-node relational database as a 'CA system'."
---

"CAP says that in a distributed system, **when there is a network partition**, you have to choose between **consistency** and **availability**. Consistency here has a strict meaning: **linearizability**, every read sees the latest write, as if there were a single copy. Availability means every node that is still running answers the request, without an error.

The popular 'pick 2 of 3' framing is misleading. Networks do fail, so partition tolerance isn't something you opt out of. The real question is what each operation does *during* a partition: refuse or time out to stay consistent, or answer with possibly stale data to stay available. And CAP says nothing about the rest of the time.

That's why I find **PACELC** more useful day to day. If there's a Partition, choose between Availability and Consistency; Else, choose between **Latency** and **Consistency**. Synchronous replication gives you consistency, but every write waits for the replica, even on a perfectly healthy network. That's a cost you pay all the time, not only during failures.

In practice, I make the choice **per operation**, not per system. In a fintech, the account balance and 'don't spend the same money twice' need strong consistency: I'd rather return an error than accept a write I can't guarantee. The transaction feed, notification counters or search index can be eventually consistent. And I watch for the in-between guarantees users notice, like **read-your-writes**: if someone makes a transfer and refreshes, they should see it, so I read their own recent data from the leader instead of a lagging replica.

With tunable systems like Cassandra, the same idea shows up as quorum settings: with a replication factor of 3, writing and reading with QUORUM means the read and write sets overlap, giving up some availability and latency for fresher reads."
