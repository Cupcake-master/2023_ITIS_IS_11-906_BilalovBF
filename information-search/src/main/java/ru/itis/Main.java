package ru.itis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try {
            Set<String> downloadPages = WebCrawler.startFirstHomework();
            Set<String> tokens = TextProcessor.tokenize(downloadPages);
            tokens.removeIf(s -> s.length() == 2);
            System.out.println(tokens);
            String[] textAfterLemmatize = TextProcessor.lemmatize(tokens);
            String[] result = TextProcessor.removeStopWords(textAfterLemmatize);
            System.out.println(Arrays.toString(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
