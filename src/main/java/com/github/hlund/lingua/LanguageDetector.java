package com.github.hlund.lingua;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Rewrite of language detector - avoid looping same data over and over and
 * collect multiple attributes in pass. Replace expensive sorts where cheaper
 * search will do. This implementation is 4-6 times faster than original.
 */
public class LanguageDetector {

   private final Map<Language, TrainingDataLanguageModel>
        unigramLanguageModels = new EnumMap<>(Language.class);
   private final Map<Language, TrainingDataLanguageModel>
        bigramLanguageModels = new EnumMap<>(Language.class);
   private final Map<Language, TrainingDataLanguageModel>
       trigramLanguageModels = new EnumMap<>(Language.class);
   private final Map<Language, TrainingDataLanguageModel>
       quadrigramLanguageModels = new EnumMap<>(Language.class);
   private final Map<Language, TrainingDataLanguageModel>
       fivegramLanguageModels = new EnumMap<>(Language.class);

    private List<Language> languagesWithUniqueCharacters;
    private Map<Alphabet, Language>
        alphabetsSupportingExactlyOneLanguage;

    private List<Language> languages;

    private Object lazyLanguageLoad = null;

    public LanguageDetector() {
        this(Language.allSpoken());
    }

    public LanguageDetector(List<Language> languages) {
        this(languages,false);
    }

    public LanguageDetector(List<Language> languages, boolean preLoad) {
        this.languages = languages;
        alphabetsSupportingExactlyOneLanguage = filterSingle();
        languagesWithUniqueCharacters = languages.stream()
            .filter(l -> l.unique != null && l.unique.length > 0)
            .collect(Collectors.toList());
        if (preLoad) {
            for (Language l : languages) {
                unigramLanguageModels.put(l, loadLanguageModels(l,1));
                bigramLanguageModels.put(l,loadLanguageModels(l,2));
                trigramLanguageModels.put(l,loadLanguageModels(l,3));
                quadrigramLanguageModels.put(l,loadLanguageModels(l, 4));
                fivegramLanguageModels.put(l, loadLanguageModels(l, 5));
            }
        } else lazyLanguageLoad = new Object();
    }

    private Map<Alphabet, Language> filterSingle() {
        Map<Alphabet, Language> l = new EnumMap<>(Alphabet.class);
        for (Map.Entry<Alphabet, Language> en :
            Alphabet.getOnlyOneLanguage().entrySet()) {
            if (languages.contains(en.getValue()))
                l.put(en.getKey(), en.getValue());
        }
        return l;
    }

    private TrainingDataLanguageModel loadLanguageModels(Language l,
                                                         int ngram) {
        try {
            return TrainingDataLanguageModel.create(l, ngram);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    //parser that in one single loop create all features needed to score.
    //no need for recursive ngram generation afterwards.
    static class Parser {
        List<Ngram> unigram, bigram, trigram, quadrigram, fivegram;
        List<String> tokens;
        String text;

        StringBuilder uni = new StringBuilder(), bi = new StringBuilder(),
            tri = new StringBuilder(), quad = new StringBuilder(),
            five = new StringBuilder(), tok = new StringBuilder();

        void process(String text) {
            this.text = text;
            unigram = new ArrayList<>();
            bigram = new ArrayList<>();
            trigram = new ArrayList<>();
            quadrigram = new ArrayList<>();
            fivegram = new ArrayList<>();
            tokens = new ArrayList<>();
            char[] chars = text.toCharArray();
            //with counter to make early termination later.
            for (int j  = 0; j < chars.length; j++) {
                char c = chars[j];
                if ((' ' == c || '/' == c) && tok.length() > 0) {
                    tokens.add(tok.toString());
                    resetBuilders();
                } else if (Character.isLetter(c)) {
                    c = Character.toLowerCase(c);
                    tok.append(c);
                    handleNgram(c, unigram, uni, 1);
                    handleNgram(c, bigram, bi, 2);
                    handleNgram(c, trigram, tri, 3);
                    handleNgram(c, quadrigram, quad, 4);
                    handleNgram(c, fivegram, five, 5);
                }
            }
            if (tok.length() > 0) {
                tokens.add(tok.toString());
            }

        }

        private void resetBuilders() {
            tok.setLength(0);
            uni.setLength(0);
            bi.setLength(0);
            tri.setLength(0);
            quad.setLength(0);
            five.setLength(0);
        }

        List<Ngram> getNgrams(int size) {
            switch (size) {
                case 1:
                    return unigram;
                case 2:
                    return bigram;
                case 3:
                    return trigram;
                case 4:
                    return quadrigram;
                case 5:
                    return fivegram;
                default:
                    return null;
            }
        }

        private void handleNgram(char c, List<Ngram> l,
                                 StringBuilder b, int g) {
            b.append(c);
            if (b.length() == g) {
                l.add(new Ngram(b.toString()));
                b.deleteCharAt(0);
            }
        }

    }

    SortedMap<Language, Double> computeLanguageConfidenceValues(String text) {
        TreeMap<Language, Double> values = new TreeMap<>();
        Parser p = new Parser();
        p.process(text);
        if (p.tokens.isEmpty()) return values;
        Language l = detectLanguageWithRules(p.tokens);
        if (l != Language.UNKNOWN) {
            values.put(l, 1.0D);
            return values;
        }

        List<Map<Language, Double>> allProbabilities = new ArrayList<>();
        //counters has to Mutable to perform
        Map<Language, AtomicInteger> unigramCountsOfInputText =
            new EnumMap<>(Language.class);
        List<Language> languagesSequence = filterLanguagesByRules(p.tokens);

        int i = 1;
        //obvious place to fork out for multi-threading
        do {
            if (p.text.length() < i) break;
            List<Ngram> data = p.getNgrams(i);
            allProbabilities.add(computeLanguageProbabilities(data,
                languagesSequence,i));
            Set<Language> languages =
                allProbabilities.get(allProbabilities.size() - 1).keySet();
            if (!languages.isEmpty()) languagesSequence =
                languagesSequence.stream().filter(languages::contains)
                    .collect(Collectors.toList());
            if (i == 1) countUnigramsOfInputText(unigramCountsOfInputText,
                data, languagesSequence);
        } while (i++ < 5);


        Map<Language, Double> summedUpProbabilities =
            sumUpProbabilities(allProbabilities, unigramCountsOfInputText,
                languagesSequence);
        double highestProbability = Double.NEGATIVE_INFINITY;
        for (double d : summedUpProbabilities.values()) {
            if (d > highestProbability) highestProbability = d;
        }
        //lets create the structure as sorted so we don't need to sort after
        // populating - let the comparator sort on both val and language in
        // one parse
        SortedMap<Language, Double> confidenceValues =
            new TreeMap<>((o1, o2) -> {
                int c = Double.compare(
                    summedUpProbabilities.get(o2),
                    summedUpProbabilities.get(o1));
                return c == 0 ? o2.compareTo(o1) : c;
            });
        for (Map.Entry<Language, Double> en
            : summedUpProbabilities.entrySet()) {
            confidenceValues.put(en.getKey(),
                highestProbability / en.getValue());
        }
        return confidenceValues;
    }

    private Map<Language, Double> sumUpProbabilities(List<Map<Language,
        Double>> props, Map<Language, AtomicInteger> uni,
                                                     List<Language> languages) {
        Map<Language, Double> summedUpProbabilities
            = new EnumMap<>(Language.class);
        for (Language la : languages) {
            double d = 0.0D;
            for (Map<Language, Double> m : props) {
                d += m.get(la) == null ? 0 : m.get(la);
            }
            if (d != 0.0D) {
                AtomicInteger count = uni.get(la);
                summedUpProbabilities.put(la, count == null ? d : d / count.get());
            }
        }
        return summedUpProbabilities;
    }

    private void countUnigramsOfInputText(Map<Language, AtomicInteger> counts,
                                          List<Ngram> data,
                                          List<Language> languages) {
        for (Language language : languages) {
            TrainingDataLanguageModel model = getTrainingDataLanguageModel(1,
                language);
            for (Ngram unigram : data) {
                if (model.getWeight(unigram) != 0)
                    incrementCounter(counts, language);
            }
        }
    }

    private Map<Language, Double> computeLanguageProbabilities(
        List<Ngram> ngrams, List<Language> languagesSequence,
        int ngramLength) {
        Map<Language, Double> probabilities = new EnumMap<>(Language.class);
        for (Language l : languagesSequence) {
            TrainingDataLanguageModel model =
                getTrainingDataLanguageModel(ngramLength, l);
            double sum = 0.0D;
            for (Ngram ngram : ngrams) sum += model.getWeight(ngram);
            if (sum < 0.0) probabilities.put(l, sum);
        }
        return probabilities;
    }

    //to keep lazy loading -- move computeIf
    private TrainingDataLanguageModel getTrainingDataLanguageModel (
        int ngramLength, Language l) {
        if (lazyLanguageLoad != null) {
            //need sync - without models might get loaded multiple times and
            //IO errors may happen
            synchronized (lazyLanguageLoad) {
                if (ngramLength == 1) return unigramLanguageModels
                    .computeIfAbsent(l, la -> loadLanguageModels(la ,1));
                else if (ngramLength == 2) return bigramLanguageModels
                    .computeIfAbsent(l, la -> loadLanguageModels(la ,2));
                else if (ngramLength == 3) return trigramLanguageModels
                    .computeIfAbsent(l, la -> loadLanguageModels(la ,3));
                else if (ngramLength == 4) return quadrigramLanguageModels
                    .computeIfAbsent(l, la -> loadLanguageModels(la ,4));
                else if (ngramLength == 5) return fivegramLanguageModels
                    .computeIfAbsent(l, la -> loadLanguageModels(la ,5));
                throw new IllegalStateException("no models for ngram length "
                    + ngramLength);
            }
        }
        if (ngramLength == 1) return unigramLanguageModels.get(l);
        else if (ngramLength == 2) return bigramLanguageModels.get(l);
        else if (ngramLength == 3) return trigramLanguageModels.get(l);
        else if (ngramLength == 4) return quadrigramLanguageModels.get(l);
        else if (ngramLength == 5) return fivegramLanguageModels.get(l);
        throw new IllegalStateException("no models for ngram length "
            + ngramLength);
    }


    private <E> void incrementCounter(Map<E, AtomicInteger> map, E key) {
        map.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
    }

    private List<Language> filterLanguagesByRules(List<String> words) {
        Map<Alphabet, AtomicInteger> detectedAlphabets =
            new EnumMap<>(Alphabet.class);
        for (String word : words) {
            for (Alphabet alphabet : Alphabet.values()) {
                if (alphabet.matches(word)) {
                    incrementCounter(detectedAlphabets, alphabet);
                    break;
                }
            }
        }

        if (detectedAlphabets.isEmpty()) {
            return languages;
        }

        Alphabet mostFrequentAlphabet = findTop(detectedAlphabets)[0].getKey();
        List<Language> filteredLanguages =
            languages.stream().filter(
                l -> Arrays.stream(l.script).anyMatch(
                    alfa -> alfa == mostFrequentAlphabet))
                .collect(Collectors.toList());

        Map<Language, AtomicInteger> languageCount = new EnumMap<>(Language.class);
        for (String word : words) {
            for (char c : word.toCharArray()) {
                Language[] mapping = CharacterMapping.getLanguageMapping(c);
                if (mapping.length > 0) {
                    for (Language l : mapping)
                        incrementCounter(languageCount, l);
                    break;
                }
            }
        }
        List<Language> subSet =
            languageCount.entrySet()
                .stream()
                .filter(en -> en.getValue().get() >= words.size() / 2
                    && filteredLanguages.contains(en.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return subSet.isEmpty() ? filteredLanguages : subSet;
    }

    private Language detectLanguageWithRules(List<String> words) {
        Map<Language, AtomicInteger> totalLanguageCounts
            = new EnumMap<>(Language.class);
        for (String word : words) {
            Map<Language, AtomicInteger>
                wordLanguageCounts = new EnumMap<>(Language.class);
            for (char c : word.toCharArray()) {
                boolean isMatch = false;
                for (Alphabet a : alphabetsSupportingExactlyOneLanguage.keySet()) {
                    if (a.matches(c)) {
                        isMatch = true;
                        incrementCounter(wordLanguageCounts,
                            alphabetsSupportingExactlyOneLanguage.get(a));
                    }
                }
                if (!isMatch) {
                    if (Alphabet.HAN.matches(c))
                        incrementCounter(wordLanguageCounts, Language.CHINESE);
                    for (Alphabet a : Language.JAPANESE.script)
                        if (a.matches(word))
                            incrementCounter(wordLanguageCounts, Language.JAPANESE);

                    if (Alphabet.LATIN.matches(c)
                        || Alphabet.CYRILLIC.matches(c)
                        || Alphabet.DEVANAGARI.matches(c)) {
                        for (Language l : languagesWithUniqueCharacters) {
                            if (l.containsUnique(c))
                                incrementCounter(wordLanguageCounts, l);
                        }
                    }
                }
            }

            if (wordLanguageCounts.isEmpty())
                incrementCounter(totalLanguageCounts, Language.UNKNOWN);
            else if (wordLanguageCounts.size() == 1) {
                Language language =
                    wordLanguageCounts.keySet().toArray(new Language[1])[0];
                incrementCounter(totalLanguageCounts,
                    languages.contains(language)
                        ? language : Language.UNKNOWN);
            } else if (wordLanguageCounts.containsKey(Language.JAPANESE)
                && wordLanguageCounts.containsKey(Language.CHINESE))
                incrementCounter(totalLanguageCounts, Language.JAPANESE);
            else {
                Map.Entry<Language, AtomicInteger>[] top
                    = findTop(wordLanguageCounts);
                Map.Entry<Language, AtomicInteger> mostFL = top[0], sec = top[1];
                if (mostFL == null)
                    incrementCounter(totalLanguageCounts, Language.UNKNOWN);
                else if (sec == null)
                    incrementCounter(totalLanguageCounts, mostFL.getKey());
                else if (mostFL.getValue().get() > sec.getValue().get()
                    && languages.contains(mostFL.getKey()))
                    incrementCounter(totalLanguageCounts, mostFL.getKey());
                else incrementCounter(totalLanguageCounts, Language.UNKNOWN);
            }
        }

        int unknownLanguageCount =
            totalLanguageCounts.get(Language.UNKNOWN) == null ? 0 :
                totalLanguageCounts.get(Language.UNKNOWN).get();

        if (0 < unknownLanguageCount
            && unknownLanguageCount < 0.5 * words.size())
            totalLanguageCounts.remove(Language.UNKNOWN);


        if (totalLanguageCounts.isEmpty()) {
            return Language.UNKNOWN;
        }
        if (totalLanguageCounts.size() == 1) {
            return totalLanguageCounts.keySet().toArray(new Language[1])[0];
        }
        Map.Entry<Language, AtomicInteger>[] topT = findTop(totalLanguageCounts);
        if ( topT[0].getValue().get() == topT[1].getValue().get())
            return Language.UNKNOWN;
        return topT[0].getKey();
    }

    /**
     * Finding the top two entries can be done with O(n) time complexity.
     * Replaces waist full sorts only to look at the first 2 elements with
     * potential O(n^2) time complexity;
     */
    private <K> Map.Entry<K, AtomicInteger>[] findTop(Map<K, AtomicInteger> map) {
        Map.Entry<K, AtomicInteger> first = null, second = null;
        for (Map.Entry<K, AtomicInteger> en : map.entrySet()) {
            if (first == null) {
                first = en;
                continue;
            }
            if (en.getValue().get() >= first.getValue().get()) {
                second = first;
                first = en;
                continue;
            }
            if (second == null) {
                second = en;
            }
        }
        return new Map.Entry[]{first, second};
    }
}
