# segmenter

Segment short strings into words.

## Usage

The easiest way to get started is to create a map of word
probabilities:

```
HashMap<String, Double> probabilities = new HashMap<String, Double>();
probabilities.put("eats", 0.2);
probabilities.put("at", 0.2);
probabilities.put("eat", 0.1);
probabilities.put("sat", 0.1);

Segmenter segmenter = new Segmenter(probabilities);
Result result = segmenter.segment("eatsat", 2, 2, 0);

result.getPhrase(0); // "eats at"
result.getPhrase(1); // "eat sat"
```

Under the covers, the `Segmenter` converts the map into a trie. The
construction step is slow, so you can also pass a constructed trie
(perhaps deserialized from a previous construction) to speed up
that step.

The `Segmenter` class is thread-safe.
