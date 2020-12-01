package com.github.hlund.lingua;

import java.util.Map;
import java.util.SortedMap;


public class PerformanceShowCase {

    //random strings from wikipedia
    static String[] testStrings = new String[] {
        "In computer science, a container is a class, a data structure,[1][2]" +
            " or an abstract data type (ADT) whose instances are collections " +
            "of other objects. In other words, they store objects in an organized way that follows specific access rules. The size of the container depends on the number of objects (elements) it contains. Underlying (inherited) implementations of various container types may vary in size and complexity, and provide flexibility in choosing the right implementation for any given scenario.",
        "كلمة حياة (الجمع: حَيَوَاتٌ) كلمة متعددة الأوجه تستخدم استخدامات " +
            "عديدة حسب مجال الكلام ونوعه. فقد تدل على مجمل الأحداث الجارية التي تحدث على الأرض و تتشارك فيها كافة الكائنات الحية. وقد تدل على الفترة التي يحياها كل كائن حي بين ولادته (عندما يصبح كينونة مستقلة حية) إلى لحظة موته و انقطاعه عن أي فعالية حية ملحوظة. تستخدم كلمة حياة أيضا لتدل على حالة الكائن الحي الذي يستطيع بفاعليته أن يثبت وجوده وأنه لم يمت ب",
        "Planeta (grčki planetes=lutalica) je nebesko tijelo znatne mase koja" +
            " orbitira oko zvijezde i ne proizvodi nikakvu energiju tokom nuklearne fuzije. Sve do 1990-ih samo je devet planeta bilo poznato (sve u našem solarnom sistemu), dok ih je do augusta 2009. godine otkriveno 373[1], a koje su uključivale i novootkrivene planete izvan Sunčevog sistema poznate kao ekstrasolarne planete ili jednostavno \"egzoplanete\".",
        "L'os bru als Pirineus, de tradició mil·lenària, ha estat perseguit " +
            "els darrers segles fins a la seva pràctica desaparició a la segona meitat del segle xx, raó per la qual l'any 1993 la Unió Europea, França i Espanya, incloses les diputacions de Navarra, Aragó i la Generalitat de Catalunya, ratificarien una sèrie d'acords per tal d'impulsar el primer projecte LIFE, dirigit a la salvaguarda dels tres animals més compromesos dels Pirineus, entre ells l'os bru."

    };

    public static void main(String[] args) {


        LanguageDetector l1 = new LanguageDetector();
        LanguageDetector.Parser p = new LanguageDetector.Parser();
        p.process(testStrings[0]);

        SortedMap<Language, Double> m1 = null;
        SortedMap<com.github.pemistahl.lingua.api.Language, Double> m2=null;
        long s1 =0,e1,s2=0,e2;
        for (int i = 0 ; i < 10; i++) {
            if (i == 1) s1 = System.currentTimeMillis();
            for (String s : testStrings)
            m1= l1.computeLanguageConfidenceValues(s);
        }
        e1 = System.currentTimeMillis();
        l1 = null;
        System.gc();

        com.github.pemistahl.lingua.api.LanguageDetector l2 =
            com.github.pemistahl.lingua.api.LanguageDetectorBuilder
                .fromAllSpokenLanguages().build();

        for (int i = 0 ; i < 10; i++) {
            if (i == 1) s2 = System.currentTimeMillis();
            for (String s : testStrings)
                m2 = l2.computeLanguageConfidenceValues(s);
        }

        e2 = System.currentTimeMillis();
        System.out.println((1d * e2-s2)/(e1-s1));
    }





}
