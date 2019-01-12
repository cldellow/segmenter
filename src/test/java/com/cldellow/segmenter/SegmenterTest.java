package com.cldellow.segmenter;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.Assert.*;


public class SegmenterTest {
    @Test
    public void testKnownWords() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("the" , 0.1);
        probs.put("dog" , 0.1);
        probs.put("jumped" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("thedogjumped", 5, 5, 0);
        assertEquals(1, rv.getHits());
        assertEquals("the dog jumped", rv.getPhrase(0));
    }

    @Test
    public void testLongerThan32() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("sixteenletters00" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("sixteenletters00sixteenletters00sixteenletters00", 10, 5, 0);
        assertEquals(1, rv.getHits());
        assertEquals("sixteenletters00 sixteenletters00 sixteenletters00", rv.getPhrase(0));
    }

    @Test
    public void test63() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("tententen0" , 0.1);
        probs.put("333" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("tententen0tententen0tententen0tententen0tententen0tententen0333", 10, 5, 0);
        assertEquals(1, rv.getHits());
        assertEquals("tententen0 tententen0 tententen0 tententen0 tententen0 tententen0 333", rv.getPhrase(0));
    }

    @Test
    public void testRepeatedWords() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("tenten" , 0.1);
        probs.put("ten" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("tenten", 2, 5, 0);
        assertEquals(2, rv.getHits());
        assertEquals("tenten", rv.getPhrase(0));
        assertEquals("ten ten", rv.getPhrase(1));
    }

    @Test
    public void testEmptyString() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("ten" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("", 2, 5, 0);
        assertEquals(0, rv.getHits());
    }

    @Test
    public void testTooManyWords() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("tenten" , 0.1);
        probs.put("ten" , 0.1);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("tenten", 1, 5, 0);
        assertEquals(1, rv.getHits());
        assertEquals("tenten", rv.getPhrase(0));
    }


    @Test
    public void testLongerThan63() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("sixteenletters00" , 0.1);

        Segmenter segmenter = new Segmenter(probs);

        try {
            Result rv = segmenter.segment("sixteenletters00sixteenletters00sixteenletters00sixteenletters00", 10, 5, 0);
            assertTrue("64 character or longer should throw", false);
        } catch(Exception e) { }
    }

    @Test
    public void testMultipleValid() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("the" , 0.5);
        probs.put("dog" , 0.2);
        probs.put("ate" , 0.2);
        probs.put("do", 0.05);
        probs.put("gate", 0.05);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("thedogate", 5, 5, 0);
        assertEquals(2, rv.getHits());
        assertEquals("the dog ate", rv.getPhrase(0));
        assertEquals("the do gate", rv.getPhrase(1));
    }

    @Test
    public void testUnknown() {
        HashMap<String, Double> probs = new HashMap<String, Double>();
        probs.put("the" , 0.5);
        probs.put("ate" , 0.5);

        Segmenter segmenter = new Segmenter(probs);
        Result rv = segmenter.segment("thedogate", 5, 15, 0.001);

        assertEquals("the dog ate", rv.getPhrase(0));
    }
}
