package com.cldellow.segmenter;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie.Hit;

import java.util.HashMap;
import java.util.List;

public class Segmenter {
    private AhoCorasickDoubleArrayTrie<Double> trie;

    public Segmenter(HashMap<String, Double> probs) {
        this(mkTrie(probs));
    }

    public Segmenter(AhoCorasickDoubleArrayTrie<Double> trie) {
        this.trie = trie;
    }

    private static AhoCorasickDoubleArrayTrie<Double> mkTrie(HashMap<String, Double> probs) {
        AhoCorasickDoubleArrayTrie<Double> rv = new AhoCorasickDoubleArrayTrie<Double>();
        rv.build(probs);
        return rv;
    }

    protected static int offsetLengthToIndex(int phraseLength, int wordOffset, int wordLength) {
        // Return the index into the table of probabilities for word lengths at given offsets.
        // eg for "abcd" there are 10 entries in the table, corresponding to the probabilities for:
        // abcd
        // abc
        // ab
        // a
        // bcd
        // bc
        // b
        //  cd
        //  c
        //   d

        // Skip all the entries for words prior to you. eg for "abcd":
        // index 0: skip 0 entries
        // index 1: skip 4 entries (abcd, abc, ab, a)
        // index 2: skip 7 entries (4 from index 1 and 3 more: bcd, bc, b)
        // index 3: skip 9 entries (7 from the previous two indexes and 2 more "cd", "c")
        //
        // Intuitively, this is an arithmetic sequence where the first term is the size of
        // the phrase, and subsequent terms decrease by 1 to capture that there is one
        // less character available.
        //
        // Per https://proofwiki.org/wiki/Sum_of_Arithmetic_Progression, the closed form of an arithmetic series
        // to term k is: n(a + l)/2, where l is the last term of ak, n is the # of terms, a is the base term.
        // So for the example above:
        // index 0 = 0(4 + ?) / 2 = 0
        // index 1 = 1(4 + 4) / 2 = 4
        // index 2 = 2(4 + 3) / 2 = 7
        // index 3 = 3(4 + 2) / 2 = 9

        return wordOffset * (phraseLength + (phraseLength - (wordOffset - 1))) / 2 + wordLength - 1;
    }

    public Result segment(String text, int maxWords, int hits, double unknownProbability) {
        int phraseLength = text.length();
        double[] probs = new double[phraseLength * (phraseLength + 1) / 2];

        List<Hit<Double>> tokens = trie.parseText(text);
        for(Hit<Double> hit : tokens) {
            int tableIndex = offsetLengthToIndex(phraseLength, hit.begin, hit.end - hit.begin);
            //System.out.println("word " + text.substring(hit.begin, hit.end) + " " + hit + ", tableIndex=" + tableIndex);
            probs[tableIndex] = hit.value;
        }

        Result holder = new Result(text, probs, maxWords, hits, unknownProbability);
        return holder;
    }
}
