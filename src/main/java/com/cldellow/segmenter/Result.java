package com.cldellow.segmenter;

public class Result {
    private double[] probs;
    private String text;
    private int maxWords;
    private int maxHits;
    private int phraseLength;
    private long[] phrases;
    private double[] ranks;
    private int hits = 0;

    public Result(String text, double[] probs, int maxWords, int maxHits, double unknownProbability) {
        if(text.length() > 63)
            throw new RuntimeException("phrase must be 63 characters or less: " + text);
        this.probs = probs;
        this.text = text;
        this.phraseLength = text.length();
        this.maxWords = maxWords;

        this.ranks = new double[maxHits];
        this.phrases = new long[maxHits];
        this.maxHits = maxHits;
        evaluate(0, 0, 1);

        if(this.hits == 0 && unknownProbability > 0) {
            // Insert edges for all words. The probability should be
            // a function of unknownProbability that increases with
            // the length of the word.
            for(int wordOffset = 0; wordOffset < phraseLength; wordOffset++) {
                for(int wordLength = 0; wordLength < phraseLength - wordOffset; wordLength++) {
                    int tableIndex = Segmenter.offsetLengthToIndex(phraseLength, wordOffset, wordLength + 1);
                    if(probs[tableIndex] == 0) {
                        probs[tableIndex] = Math.pow(unknownProbability, wordLength + 1) / (wordLength + 2);
                    }
                }
            }
            evaluate(0, 0, 1);
        }
    }

    public int getHits() {
        return hits > maxHits ? maxHits : hits;
    }

    public double getProbability(int i) {
        return ranks[i];
    }

    public String getPhrase(int i) {
        String rv = "";
        long spaces = phrases[i];
        int offset = 0;

        while(spaces > 0) {
            spaces -= 1;
            int charsInWord = spaces == 0 ? phraseLength - offset : Long.numberOfTrailingZeros(spaces);
            if(rv.length() > 0)
                rv += " ";
            rv += text.substring(offset, offset + charsInWord);
            offset += charsInWord;

            spaces >>= charsInWord;
        }
        return rv;
    }

    private void evaluate(long spaces, int phraseOffset, double rank) {
        if(Long.bitCount(spaces) + 1 > maxWords)
            return;

        int tableOffset = Segmenter.offsetLengthToIndex(phraseLength, phraseOffset, 1);
        //System.out.println("spaces=" + spaces + ", phraseOffset=" + phraseOffset + ", tableOffset=" + tableOffset);
        int wordLengths = phraseLength - phraseOffset;

        while(wordLengths > 0) {
            // Is this a valid word?
            double prob = probs[tableOffset + wordLengths - 1];
            if(prob != 0d) {
              long newSpace = spaces | (1L << phraseOffset);
              if(phraseOffset + wordLengths >= phraseLength) {
                // consider if this is a new best result, update arrays
                  double newRank = rank * prob;
                  hits++;

                  int i = 0;
                  for(; i < ranks.length; i++) {
                      if(newRank > ranks[i])
                          break;
                  }

                  if(i < ranks.length) {
                      if(i + 1 < ranks.length) {
                          // Move the existing ranks down a notch
                          System.arraycopy(ranks, i, ranks, i + 1, ranks.length - (i + 1));
                          System.arraycopy(phrases, i, phrases, i + 1, phrases.length - (i + 1));
                      }
                      ranks[i] = newRank;
                      phrases[i] = newSpace;
                  }
                  //System.out.println("found phrase: " + newSpace + ", prob=" + rank);
              } else {
                  evaluate(newSpace, phraseOffset + wordLengths, rank * prob);
              }
            }

            wordLengths--;
        }
    }
}
