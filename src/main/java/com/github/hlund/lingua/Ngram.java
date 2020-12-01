package com.github.hlund.lingua;

/**
 * missing serialization - but adds a few instance variables that else is
 * used in performance critical paths - cashing critocal calculations like
 * toCharArray and isAscii.
 */
class Ngram implements Comparable<Ngram> {

    String value;
    char[] chars;
    boolean isAscii = true;
    int len;

    Ngram(String value) {
        this.value = value;
        this.chars = value.toCharArray();
        for (int i = 0; i < value.length(); i++) {
            if ('a' > value.charAt(i) || 'z' < value.charAt(i)) {
                isAscii = false;
                break;
            }
        }
        if (isAscii) isAscii = value.length() <= 3;
        len = value.length();
    }

    public String toString() {
        return value;
    }

    public int compareTo(Ngram other) {
        return Integer.compare(this.value.length(), other.value.length());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ngram)) return false;
        Ngram ngram = (Ngram) o;
        return value.equals(ngram.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
