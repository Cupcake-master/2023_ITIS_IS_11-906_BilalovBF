package ru.itis;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            //Первая домашняя работа
            Map<String, String> urls = WebCrawlerImpl.getAllInternalUrlWithScanner();

            //Вторая домашняя работа
            Map<String, List<String>> listWithoutIndexesOfWord = secondHomework(urls);


            Map<String, Set<String>> newMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : listWithoutIndexesOfWord.entrySet()) {
                String key = entry.getKey();
                List<String> valueList = entry.getValue();
                Set<String> valueSet = new HashSet<>(valueList);
                newMap.put(key, valueSet);
            }

            thirdHomework(newMap);

            Map<String, Map<String, Double>> calculateTermFrequencyInverseDocumentFrequency =
                    fourthHomework(listWithoutIndexesOfWord);

            fifthHomework(calculateTermFrequencyInverseDocumentFrequency);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<String>> secondHomework(Map<String, String> urls) throws IOException {
        Map<String, List<String>> map = TextProcessorImpl.start(urls);


        Map<String, List<String>> listWithoutIndexesOfWord =
                removeUnnecessaryInformationAfterLematization(map);

        int index = 0;
        for (Map.Entry<String, List<String>> l : listWithoutIndexesOfWord.entrySet()) {
            String text = String.format("URL: %s \nWords: %s \n", l.getKey(), l.getValue());
            WebCrawlerImpl.writeToFile("lematized_" + index + ".txt", text);
            index++;
        }
        return listWithoutIndexesOfWord;
    }

    private static void thirdHomework(Map<String, Set<String>> newMap) throws IOException {
        BooleanSearch.searchDocumentsByQuery(newMap);
    }

    private static Map<String, Map<String, Double>> fourthHomework(Map<String, List<String>>
                                                                           listWithoutIndexesOfWord) throws IOException {
        return CalculatorTFIDF.calculateTermFrequencyInverseDocumentFrequency(listWithoutIndexesOfWord);
    }

    private static void fifthHomework(Map<String, Map<String, Double>>
                                              calculateTermFrequencyInverseDocumentFrequency) throws IOException {
        String[] requests = {"экспорт", "экспорт просмотр", "экспорт просмотр советник"};
        StringBuilder builder = new StringBuilder();
        for (String request : requests) {
            builder.append("Query: ").append(request).append("\n");
            List<String> list = VectorSearch.search(request,
                    calculateTermFrequencyInverseDocumentFrequency);
            for (String document : list) {
                builder.append("- ").append(document).append('\n');
            }
            builder.append('\n').append('\n');
        }
        WebCrawlerImpl.writeToFile("vectorSearch.txt", builder.toString());
    }

    private static Map<String, List<String>> removeUnnecessaryInformationAfterLematization(
            Map<String, List<String>> map) {
        Map<String, List<String>> listWithoutIndexesOfWord = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> words = new LinkedList<>();
            for (String s : entry.getValue()) {
                String value = s.substring(1).split(",")[0];
                words.add(value);
            }
            listWithoutIndexesOfWord.put(entry.getKey(), words);
        }
        return listWithoutIndexesOfWord;
    }
}
