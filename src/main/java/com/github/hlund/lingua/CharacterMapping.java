package com.github.hlund.lingua;

/**
 * Efficient data structure to look up language defining chars - simple cast
 * to int from char allows direct lookup at the cost of some heap - the array
 * is just under 8K elements long
 *
 * Most likely this optimization is a bit premature.
 */
class CharacterMapping {

    private static final Language[][] CHARS_TO_LANGUAGES_MAPPING;
    private static final Language[] NO_MAPPING = new Language[0];
    static {
        Language[][] tmp = new Language[0][];
        tmp = pop("Ãã", tmp, Language.PORTUGUESE, Language.VIETNAMESE);
        tmp = pop("ĄąĘę", tmp, Language.LITHUANIAN, Language.POLISH);
        tmp = pop("Żż", tmp, Language.POLISH, Language.ROMANIAN);
        tmp = pop("Îî", tmp, Language.FRENCH, Language.ROMANIAN);
        tmp = pop("Ññ", tmp, Language.BASQUE, Language.SPANISH);
        tmp = pop("ŇňŤť", tmp, Language.CZECH, Language.SLOVAK);
        tmp = pop("Ăă", tmp, Language.ROMANIAN, Language.VIETNAMESE);
        tmp = pop("İıĞğ", tmp, Language.AZERBAIJANI, Language.TURKISH);
        tmp = pop("ЈјЉљЊњ", tmp, Language.MACEDONIAN, Language.SERBIAN);
        tmp = pop("ĀāĒēĪī", tmp, Language.LATVIAN, Language.YORUBA);
        tmp = pop("ẸẹỌọ", tmp, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("Ūū", tmp, Language.LATVIAN, Language.LITHUANIAN, Language.YORUBA);
        tmp = pop("Şş", tmp, Language.AZERBAIJANI, Language.ROMANIAN, Language.TURKISH);
        tmp = pop("Ďď", tmp, Language.CZECH, Language.ROMANIAN, Language.SLOVAK);
        tmp = pop("ÐðÞþ", tmp, Language.ICELANDIC, Language.LATVIAN, Language.TURKISH);
        tmp = pop("Ûû", tmp, Language.FRENCH, Language.HUNGARIAN, Language.LATVIAN);
        tmp = pop("Ćć", tmp, Language.BOSNIAN, Language.CROATIAN, Language.POLISH);
        tmp = pop("Đđ", tmp, Language.BOSNIAN, Language.CROATIAN, Language.VIETNAMESE);
        tmp = pop("Іі", tmp, Language.BELARUSIAN, Language.KAZAKH, Language.UKRAINIAN);
        tmp = pop("Ìì", tmp, Language.ITALIAN, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("Ëë", tmp, Language.AFRIKAANS, Language.ALBANIAN, Language.DUTCH, Language.FRENCH);
        tmp = pop("ÈèÙù", tmp, Language.FRENCH, Language.ITALIAN, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("Êê", tmp, Language.AFRIKAANS, Language.FRENCH, Language.PORTUGUESE, Language.VIETNAMESE);
        tmp = pop("Õõ", tmp, Language.ESTONIAN, Language.HUNGARIAN, Language.PORTUGUESE, Language.VIETNAMESE);
        tmp = pop("Ôô", tmp, Language.FRENCH, Language.PORTUGUESE, Language.SLOVAK, Language.VIETNAMESE);
        tmp = pop("Øø", tmp, Language.BOKMAL, Language.DANISH, Language.NYNORSK);
        tmp = pop("ЁёЫыЭэ", tmp, Language.BELARUSIAN, Language.KAZAKH, Language.MONGOLIAN, Language.RUSSIAN);
        tmp = pop("ЩщЪъ", tmp, Language.BULGARIAN, Language.KAZAKH, Language.MONGOLIAN, Language.RUSSIAN);
        tmp = pop("Òò", tmp, Language.CATALAN, Language.ITALIAN, Language.LATVIAN, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("Ýý", tmp, Language.CZECH, Language.ICELANDIC, Language.SLOVAK, Language.TURKISH, Language.VIETNAMESE);
        tmp = pop("Ää", tmp, Language.ESTONIAN, Language.FINNISH, Language.GERMAN, Language.SLOVAK, Language.SWEDISH);
        tmp = pop("Ââ", tmp, Language.LATVIAN, Language.PORTUGUESE, Language.ROMANIAN, Language.TURKISH, Language.VIETNAMESE);
        tmp = pop("Àà", tmp, Language.CATALAN, Language.FRENCH, Language.ITALIAN, Language.PORTUGUESE, Language.VIETNAMESE);
        tmp = pop("Ææ", tmp, Language.BOKMAL, Language.DANISH, Language.ICELANDIC, Language.NYNORSK);
        tmp = pop("Åå", tmp, Language.BOKMAL, Language.DANISH, Language.NYNORSK, Language.SWEDISH);
        tmp = pop("Üü", tmp, Language.AZERBAIJANI, Language.CATALAN, Language.ESTONIAN, Language.GERMAN, Language.HUNGARIAN,
            Language.SPANISH, Language.TURKISH);
        tmp = pop("ČčŠšŽž", tmp, Language.BOSNIAN, Language.CZECH, Language.CROATIAN, Language.LATVIAN, Language.LITHUANIAN,
            Language.SLOVAK, Language.SLOVENE);
        tmp = pop("Çç", tmp, Language.ALBANIAN, Language.AZERBAIJANI, Language.BASQUE, Language.CATALAN, Language.FRENCH,
            Language.LATVIAN, Language.PORTUGUESE, Language.TURKISH);
        tmp = pop("Öö", tmp, Language.AZERBAIJANI, Language.ESTONIAN, Language.FINNISH, Language.GERMAN, Language.HUNGARIAN,
            Language.ICELANDIC, Language.SWEDISH, Language.TURKISH);
        tmp = pop("Óó", tmp, Language.CATALAN, Language.HUNGARIAN, Language.ICELANDIC, Language.IRISH, Language.POLISH,
            Language.PORTUGUESE, Language.SLOVAK, Language.SPANISH, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("ÁáÍíÚú", tmp, Language.CATALAN, Language.CZECH, Language.ICELANDIC, Language.IRISH, Language.HUNGARIAN,
            Language.PORTUGUESE, Language.SLOVAK, Language.SPANISH, Language.VIETNAMESE, Language.YORUBA);
        tmp = pop("Éé", tmp, Language.CATALAN, Language.CZECH, Language.FRENCH, Language.HUNGARIAN, Language.ICELANDIC,
            Language.IRISH, Language.ITALIAN, Language.PORTUGUESE, Language.SLOVAK, Language.SPANISH, Language.VIETNAMESE,
            Language.YORUBA);
        CHARS_TO_LANGUAGES_MAPPING = tmp;
    }

    private static Language[][] pop(String chars, Language[][] tmp,
                                    Language... value) {
        for (char c : chars.toCharArray()) {
            int index = c;
            if (index >= tmp.length) {
                Language[][] t = new Language[index + 1][];
                System.arraycopy(tmp, 0, t, 0, tmp.length);
                tmp = t;
            }
            if (tmp[index] != null)
                throw new IllegalStateException("Setup char map broken");
            tmp[index] = value;
        }
        return tmp;
    }

    public static Language[] getLanguageMapping(char c) {
        if (c >= CHARS_TO_LANGUAGES_MAPPING.length) return NO_MAPPING;
        return CHARS_TO_LANGUAGES_MAPPING[c] == null
            ? NO_MAPPING : CHARS_TO_LANGUAGES_MAPPING[c];
    }

}
