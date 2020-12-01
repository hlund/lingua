package com.github.hlund.lingua;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.lang.Character.UnicodeScript;

/**
 * Rewrite of linguia Alphabet. Use UnicodeScript instead of regex - both
 * better performance and easier to read.
 */
public enum Alphabet {

    ARABIC,
    ARMENIAN,
    BENGALI,
    CYRILLIC,
    DEVANAGARI,
    GEORGIAN,
    GREEK,
    GUJARATI,
    GURMUKHI,
    HAN,
    HANGUL,
    HEBREW,
    HIRAGANA,
    KATAKANA,
    LATIN,
    TAMIL,
    TELUGU,
    THAI,
    NONE;

    UnicodeScript script;
    private static Map<Alphabet, Language> single = null;

    Alphabet() {
        if (!"NONE".equals(this.name()))
            this.script = UnicodeScript.forName(this.name());
    }

    public boolean matches(CharSequence input) {
        if (this.script == null) return false;
        return input.codePoints()
            .allMatch(p -> this.script == UnicodeScript.of(p));
    }

    public boolean matches(char input) {
        if (this.script == null) return false;
        return this.script == UnicodeScript.of(input);
    }

    static Map<Alphabet, Language> getOnlyOneLanguage() {
        if (single == null) lazy();
        return single;
    }

    //needed to be lazy as Alphabet and Language has cross references -
    // should be merged
    static synchronized void lazy() {
        Map<Alphabet, List<Language>> map = new EnumMap<>(Alphabet.class);
        single = new EnumMap<>(Alphabet.class);
        for (Language l : Language.values())
            for (Alphabet s : l.script)
                map.computeIfAbsent(s, a -> new ArrayList<>()).add(l);
        for (Map.Entry<Alphabet, List<Language>> en : map.entrySet())
            if (en.getValue().size() == 1)
                single.put(en.getKey(), en.getValue().get(0));
    }
}
