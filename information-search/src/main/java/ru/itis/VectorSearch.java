package ru.itis;

import java.io.IOException;
import java.util.*;

public class VectorSearch {
    private static int count = 0;

    public static List<String> search(String query, Map<String, Map<String, Double>> index) {
        count++;
        List<String> result = new ArrayList<>();
        Map<String, Double> queryVector = new HashMap<>();
        String[] terms = query.split("\\s+");

        for (String term : terms) {
            for (Map.Entry<String, Map<String, Double>> entry : index.entrySet()) {
                if (entry.getValue().containsKey(term)) {
                    double idf = Math.log10(index.size() / (double) entry.getValue().size());
                    queryVector.put(term, idf);
                }
            }
        }

        Map<String, Double> scores = new HashMap<>();
        for (String term : queryVector.keySet()) {
            double queryWeight = queryVector.get(term);
            for (Map.Entry<String, Map<String, Double>> entry : index.entrySet()) {
                String docId = entry.getKey();
                Map<String, Double> postingList = entry.getValue();
                if (postingList.containsKey(term)) {
                    double docWeight = postingList.get(term);
                    double idf = Math.log10(index.size() / (double) postingList.size());
                    double weight = queryWeight * docWeight * idf;
                    if (!scores.containsKey(docId)) {
                        scores.put(docId, 0.0);
                    }
                    scores.put(docId, scores.get(docId) + weight);
                }
            }
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        for (Map.Entry<String, Double> entry : sorted) {
            result.add(entry.getKey());
        }

        return result;
    }
}