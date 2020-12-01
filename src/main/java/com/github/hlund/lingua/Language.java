package com.github.hlund.lingua;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;

/**
 * Language mapping with language specific chars for early termination.
 */
public enum Language {

    AFRIKAANS(forLanguageTag("AF"), Alphabet.LATIN),
    ALBANIAN(forLanguageTag("SQ"), Alphabet.LATIN),
    ARABIC(forLanguageTag("AR"), Alphabet.ARABIC),
    ARMENIAN(forLanguageTag("HY"), Alphabet.ARMENIAN),
    AZERBAIJANI(forLanguageTag("AZ"),"Əə", Alphabet.LATIN),
    BASQUE(forLanguageTag("EU"), Alphabet.LATIN),
    BELARUSIAN(forLanguageTag("BE"), Alphabet.LATIN),
    BENGALI(forLanguageTag("BN"), Alphabet.BENGALI),
    BOKMAL(forLanguageTag("NB"), Alphabet.LATIN),
    BOSNIAN(forLanguageTag("BS"), Alphabet.LATIN),
    BULGARIAN(forLanguageTag("BG"), Alphabet.CYRILLIC),
    CATALAN(forLanguageTag("CA"), "Ïï",  Alphabet.LATIN),
    CHINESE(forLanguageTag("ZH"), Alphabet.HAN),
    CROATIAN(forLanguageTag("HR"), Alphabet.LATIN),
    CZECH(forLanguageTag("CS"), "ĚěŘřŮů", Alphabet.LATIN),
    DANISH(forLanguageTag("DA"), Alphabet.LATIN),
    DUTCH(forLanguageTag("NL"), Alphabet.LATIN),
    ENGLISH(forLanguageTag("EN"), Alphabet.LATIN),
    ESPERANTO(forLanguageTag("EO"), "ĈĉĜĝĤĥĴĵŜŝŬŭ", Alphabet.LATIN),
    ESTONIAN(forLanguageTag("ET"), Alphabet.LATIN),
    FINNISH(forLanguageTag("FI"), Alphabet.LATIN),
    FRENCH(forLanguageTag("FR"), Alphabet.LATIN),
    GANDA(forLanguageTag("LG"), Alphabet.LATIN),
    GEORGIAN(forLanguageTag("KA"), Alphabet.GEORGIAN),
    GERMAN(forLanguageTag("DE"), "ß",  Alphabet.LATIN),
    GREEK(forLanguageTag("EL"), Alphabet.GREEK),
    GUJARATI(forLanguageTag("GU"), Alphabet.GUJARATI),
    HEBREW(forLanguageTag("HE"), Alphabet.HEBREW),
    HINDI(forLanguageTag("HI"), Alphabet.DEVANAGARI),
    HUNGARIAN(forLanguageTag("HU"),"ŐőŰű", Alphabet.LATIN),
    ICELANDIC(forLanguageTag("IS"), Alphabet.LATIN),
    INDONESIAN(forLanguageTag("ID"), Alphabet.LATIN),
    IRISH(forLanguageTag("GA"), Alphabet.LATIN),
    ITALIAN(forLanguageTag("IT"), Alphabet.LATIN),
    JAPANESE(forLanguageTag("JA"), Alphabet.HIRAGANA, Alphabet.KATAKANA, Alphabet.HAN),
    KAZAKH(forLanguageTag("KK"), "ӘәҒғҚқҢңҰұ", Alphabet.CYRILLIC),
    KOREAN(forLanguageTag("KO"), Alphabet.HANGUL),
    LATIN(forLanguageTag("LA"), Alphabet.LATIN),
    LATVIAN(forLanguageTag("LV"), "ĢģĶķĻļŅņ", Alphabet.LATIN),
    LITHUANIAN(forLanguageTag("LT"), "ĖėĮįŲų", Alphabet.LATIN),
    MACEDONIAN(forLanguageTag("MK"), "ЃѓЅѕЌќЏџ", Alphabet.CYRILLIC),
    MALAY(forLanguageTag("MS"), Alphabet.LATIN),
    MARATHI(forLanguageTag("MR"), "ळ", Alphabet.DEVANAGARI),
    MONGOLIAN(forLanguageTag("MN"), "ӨөҮү", Alphabet.CYRILLIC),
    NYNORSK(forLanguageTag("NN"), Alphabet.LATIN),
    PERSIAN(forLanguageTag("FA"), Alphabet.ARABIC),
    POLISH(forLanguageTag("PL"), "ŁłŃńŚśŹź", Alphabet.LATIN),
    PORTUGUESE(forLanguageTag("PT"), Alphabet.LATIN),
    PUNJABI(forLanguageTag("PA"), Alphabet.GURMUKHI),
    ROMANIAN(forLanguageTag("RO"), "Țţ", Alphabet.LATIN),
    RUSSIAN(forLanguageTag("RU"), Alphabet.CYRILLIC),
    SERBIAN(forLanguageTag("SR"), "ЂђЋћ", Alphabet.CYRILLIC),
    SHONA(forLanguageTag("SN"), Alphabet.LATIN),
    SLOVAK(forLanguageTag("SK"), "ĹĺĽľŔŕ", Alphabet.LATIN),
    SLOVENE(forLanguageTag("SL"), Alphabet.LATIN),
    SOMALI(forLanguageTag("SO"), Alphabet.LATIN),
    SOTHO(forLanguageTag("ST"), Alphabet.LATIN),
    SPANISH(forLanguageTag("ES"), "¿¡", Alphabet.LATIN),
    SWAHILI(forLanguageTag("SW"), Alphabet.LATIN),
    SWEDISH(forLanguageTag("SV"), Alphabet.LATIN),
    TAGALOG(forLanguageTag("TL"), Alphabet.LATIN),
    TAMIL(forLanguageTag("TA"), Alphabet.TAMIL),
    TELUGU(forLanguageTag("TE"), Alphabet.TELUGU),
    THAI(forLanguageTag("TH"), Alphabet.THAI),
    TSONGA(forLanguageTag("TS"), Alphabet.LATIN),
    TSWANA(forLanguageTag("TN"), Alphabet.LATIN),
    TURKISH(forLanguageTag("TR"), Alphabet.LATIN),
    UKRAINIAN(forLanguageTag("UK"), "ҐґЄєЇї", Alphabet.CYRILLIC),
    URDU(forLanguageTag("UR"), Alphabet.ARABIC),
    VIETNAMESE(forLanguageTag("VI"),"ẰằẦầẲẳẨẩẴẵẪẫẮắẤấẠạẶặẬậỀềẺẻỂểẼẽỄễẾếỆệỈỉĨĩỊịƠ"
        + "ơỒồỜờỎỏỔổỞởỖỗỠỡỐốỚớỘộỢợƯưỪừỦủỬửŨũỮữỨứỤụỰựỲỳỶỷỸỹỴỵ",
        Alphabet.LATIN),
    WELSH(forLanguageTag("CY"), Alphabet.LATIN),
    XHOSA(forLanguageTag("XH"), Alphabet.LATIN),
    YORUBA(forLanguageTag("YO"), "ŌōṢṣ", Alphabet.LATIN),
    ZULU(forLanguageTag("ZU"), Alphabet.LATIN),

    /**
     * The imaginary unknown language.
     *
     * This value is returned if no language can be detected reliably.
     */
    UNKNOWN(Locale.ROOT);


    char[] unique;
    Alphabet[] script;
    Locale locale;

    static List<Language> all, spoken,arabic,cyrillic,devanagari, latin;

    static {
        all = new ArrayList<>(); spoken = new ArrayList<>();
        arabic = new ArrayList<>(); cyrillic = new ArrayList<>();
        devanagari = new ArrayList<>(); latin = new ArrayList<>();
        for (Language l : values()) {
            if (l != UNKNOWN) all.add(l);
            if (l != UNKNOWN && l != LATIN) spoken.add(l);
            for (Alphabet a : l.script) {
                if (a == Alphabet.ARABIC) arabic.add(l);
                if (a == Alphabet.CYRILLIC) cyrillic.add(l);
                if (a == Alphabet.DEVANAGARI) devanagari.add(l);
                if (a == Alphabet.LATIN) latin.add(l);
            }
        }
    }

    Language(Locale af, Alphabet... scripts) {
        this(af,"",scripts);
    }

    Language(Locale locale, String uniq, Alphabet... scripts){
        this.unique = uniq.toCharArray();
        Arrays.sort(unique);
        this.script = scripts;
        this.locale = locale;
    }

    /**
     * fix JAVA Local
     */
    public  String getISOCode() {
        if (this == HEBREW) return "he";
        if (this == INDONESIAN) return "id";
        return locale.getLanguage();
    }

    public static List<Language> languagesWithAlphabet(List<Language> in,
                                                       Alphabet a) {
        return in.stream().filter(language ->
            Arrays.stream(language.script)
                .anyMatch( alphabet -> alphabet == a)
        ).collect(Collectors.toList());
    }

    public static List<Language> all() {return all;}

    public static List<Language> allSpoken() {return spoken;};

    public static List<Language> allWithArabicScript() {return arabic;}

    public static List<Language> allWithCyrillicScript() {return cyrillic;}

    public static List<Language> allWithDevanagariScript() { return devanagari;}

    public static List<Language> allWithLatinScript() { return latin;}

    public static Language getByIsoCode639_1(String code) {
        for (Language l :
            values()) if (l.locale.getLanguage().equals(code)) return l;
        return null;
    }

    public static Language getByIsoCode639_3(String code) {
        for (Language l :
            values()) if (l.locale.getISO3Language().equals(code)) return l;
        return null;
    }

    public boolean containsUnique(String word) {
        if (unique == null || unique.length == 0) return false;
        for (char c : unique) if (word.indexOf(c) != -1) return true;
        return false;
    }

    public boolean containsUnique(char c) {
        if (unique == null || unique.length == 0) return false;
        for (char u : unique) if (c == u) return true;
        return false;
    }
}
