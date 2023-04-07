package ru.itis;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            Map<String, String> urls = WebCrawlerImpl.getAllInternalUrlWithScanner();
            Map<String, List<String>> map = TextProcessorImpl.start(urls);


            Map<String, List<String>> listWithoutIndexesOfWord = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                List<String> words = new LinkedList<>();
                for (String s : entry.getValue()) {
                    String value = s.substring(1).split(",")[0];
                    words.add(value);
                }
                listWithoutIndexesOfWord.put(entry.getKey(), words);
            }

            Map<String, Set<String>> newMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : listWithoutIndexesOfWord.entrySet()) {
                String key = entry.getKey();
                List<String> valueList = entry.getValue();
                Set<String> valueSet = new HashSet<>(valueList);
                newMap.put(key, valueSet);
            }

            BooleanSearch.searchDocumentsByQuery(newMap);
            CalculatorTFIDF.calculateTFIDF(listWithoutIndexesOfWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
