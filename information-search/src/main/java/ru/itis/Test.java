package ru.itis;

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String originalString = "https://ru.wikisource.org/wiki/%D0%92%D0%BE%D0%B9%D0%BD%D0%B0_%D0%B8_%D0%BC%D0%B8%D1%80_(%D0%A2%D0%BE%D0%BB%D1%81%D1%82%D0%BE%D0%B9)";
        String decodedString = URLDecoder.decode(originalString, StandardCharsets.UTF_8);
        System.out.println(decodedString);
    }
}
