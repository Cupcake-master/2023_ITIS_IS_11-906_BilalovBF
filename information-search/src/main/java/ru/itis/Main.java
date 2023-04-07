package ru.itis;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            Map<String, String> urls = WebCrawlerImpl.getAllInternalUrlWithScanner();
            Map<String, List<String>> map = TextProcessorImpl.start(urls);

            Map<String, Set<String>> newMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> valueList = entry.getValue();
                Set<String> valueSet = new HashSet<>(valueList);
                newMap.put(key, valueSet);
            }

            BooleanSearch.searchDocumentsByQuery(newMap);
            CalculatorTFIDF.calculateTFIDF(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
