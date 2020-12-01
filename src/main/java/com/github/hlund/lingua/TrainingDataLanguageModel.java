package com.github.hlund.lingua;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * No training here simple load the data files.
 *
 * The model keeps data in two structures - a Map and a simple array used to
 * store scores for ascii a-z ngrams - which is by far the most common to
 * look up in predominant latin scripted text.
 *
 * The array consumes quite a lot of memory but is about 10x faster to lookup
 * than the map (0.35720126 my seconds vs 3.550569858 my seconds) measured.
 *
 * currently 1-4 grams uses this optimization for languages using Latin
 * script - reducing to 3 will reduce memory needs, but still represent a
 * substantial performance gain.
 *
 * Optimized after profiling showed 56% cpu time spend on map.get exactly here.
 */
public class TrainingDataLanguageModel {

    static int[] lowOffsets = new int[] {
        getAsciiInt('a'), getAsciiInt('a','a'), getAsciiInt('a','a','a'),
        getAsciiInt('a','a','a','a')};
    static int[] upperBound = new int[] {getAsciiInt('z'),getAsciiInt('z','z'),
        getAsciiInt('z','z','z'),getAsciiInt('z','z','z','z')};

    Language language;
    Map<Ngram, Double> jsonRelativeFrequencies;
    double[] optimized;
    ObjectMapper mapper = new ObjectMapper();
    boolean isLatin;



    private String getResourceName(Language language, int ngram) {
        return "language-models/" + language.getISOCode() + "/"
            + getFileName(ngram);
    }

    static TrainingDataLanguageModel create(Language language, int ngram)
        throws IOException{
        return new TrainingDataLanguageModel().load(language,ngram);
    }

    TrainingDataLanguageModel load(Language language, int ngram)
        throws IOException {
        this.language = language;
        isLatin = language.script.length == 1 &&
            language.script[0].script == Character.UnicodeScript.LATIN;
        InputStream in = ClassLoader
            .getSystemResourceAsStream(getResourceName(language, ngram));
        JsonN j = mapper.readValue(in, JsonN.class);
        StringBuilder builder = new StringBuilder();
        jsonRelativeFrequencies = new HashMap<>();
        if (ngram <= 3 && isLatin) optimized =
            new double[upperBound[ngram-1] - lowOffsets[ngram-1]+1];
        for (Map.Entry<String, String> en : j.ngrams.entrySet()) {
            int divider = en.getKey().indexOf('/');
            int fre = Integer.parseInt(en.getKey().substring(0, divider));
            int total = Integer.parseInt(en.getKey().substring(divider + 1));
            for (char c : en.getValue().toCharArray()) {
                if (' ' == c) addNgram(ngram, builder, fre, total);
                else builder.append(c);
            }
            addNgram(ngram, builder, fre, total);
        }
        return this;
    }

    public double getWeight(Ngram n) {
        if (n.isAscii && isLatin)
            return optimized[getAsciiInt(n.chars) -lowOffsets[n.len -1]];
        Double d = jsonRelativeFrequencies.get(n);
        return d == null ? 0 : d;
    }

    private void addNgram(int ngram, StringBuilder builder, int numerator,
                          int denominator)
        throws IOException {
        if (builder.length() != 0 && builder.length() != ngram)
            throw new IOException("Ngram model corrupted");
        Ngram ngr = new Ngram(builder.toString());
        double w = (double) numerator / denominator;
        if (ngr.isAscii && isLatin)
            optimized[getAsciiInt(ngr.chars) - lowOffsets[ngram-1]]
                = Math.log(w);
        else
            jsonRelativeFrequencies.put(ngr, Math.log(w));
        builder.setLength(0);
    }

    public static class JsonN {
        public String language;
        public Map<String, String> ngrams;
    }

    private String getFileName(int ngram) {
        if (ngram == 1) return "unigrams.json";
        if (ngram == 2) return "bigrams.json";
        if (ngram == 3) return "trigrams.json";
        if (ngram == 4) return "quadrigrams.json";
        if (ngram == 5) return "fivegrams.json";
        return null;
    }

    //lowercase ascii can be mapped to 5 bits -> 20 bits for all ngram up to
    // length 4 - using floats each language ngrams can be stored in just
    // 32Mb (float is reasonable as the ln function is done before removing
    // the bytes.
    //(this encoding rely on a-z being in sequence in the character mapping.]
    private static int getAsciiInt(char... s) {
        int a = 0;
        for (char c : s) {
            int k = c - 'a'; // offset
            a = a << 5 | k;
        }
        return a;
    }
}
