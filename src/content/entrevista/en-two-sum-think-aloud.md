---
pergunta: "Given an array of integers and a target, return the indices of the two numbers that add up to the target. Talk me through your approach before you code."
tema: "Algorithms (EN)"
nivel: jr
idioma: en
modulo: 05-algoritmos
intencao: "Thinking out loud in English: clarifying, starting with brute force, improving it and stating the complexity before coding."
pontosChave:
  - "Clarifying questions: duplicates, negative numbers, always one answer?"
  - "Brute force first, with its complexity: O(n²)"
  - "Optimization with a HashMap: O(n) time, O(n) space"
  - "Walk through an example by hand"
  - "Edge cases and tests at the end"
armadilha: "Coding in silence for ten minutes, or jumping to the optimal solution without explaining how you got there."
---

"Let me **clarify a few things first**. Can the array contain negative numbers or duplicates? Is there always exactly one answer? And can I use the same element twice? ... OK, so exactly one answer, and no reusing the same index.

The **brute-force approach** is to check every pair with two nested loops. That's **O(n²)** time and O(1) space. It works, but we can do better.

For each number, what I really need to know is whether its **complement**, target minus the number, has already appeared. A **HashMap** from value to index answers that in constant time. So I iterate once: for each element, I check if the complement is in the map. If it is, I return both indices. If not, I put the current number in the map.

Let me **walk through an example**: `[2, 7, 11, 15]`, target 9. At index 0, the complement 7 isn't in the map, so I store 2. At index 1, the complement 2 is in the map at index 0, so I return `[0, 1]`.

That's **O(n) time and O(n) space**. It's a classic trade-off: extra memory for speed. Should I go ahead and code it?"

```java
int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        Integer j = seen.get(target - nums[i]);
        if (j != null) return new int[] {j, i};
        seen.put(nums[i], i);
    }
    throw new IllegalArgumentException("no solution");
}
```

After coding, **test it out loud**: the example, a case with negative numbers, and a case with duplicates like `[3, 3]` with target 6. Checking the map *before* inserting is what makes the duplicate case work.
