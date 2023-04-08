package ru.itis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatorTFIDF {

    public static Map<String, Map<String, Double>> calculateTermFrequencyInverseDocumentFrequency
            (Map<String, List<String>> documents) throws IOException {
        List<TFIDF> tfidfs = new ArrayList<>();
        Map<String, Map<String, Double>> tfidf = new HashMap<>();
        Map<String, Double> idf = calculateInverseDocumentFrequency(documents);
        writeDataInFile(idf, "idf.txt");

        int count = 0;
        for (Map.Entry<String, List<String>> entry : documents.entrySet()) {
            String docId = entry.getKey();
            List<String> words = entry.getValue();

            Map<String, Double> tf = calculateTermFrequency(words);
            writeDataInFile(tf, "tf_" + count + ".txt");

            Map<String, Double> docTFIDF = new HashMap<>();

            for (Map.Entry<String, Double> tfEntry : tf.entrySet()) {
                String term = tfEntry.getKey();
                double tfValue = tfEntry.getValue();
                double idfValue = idf.getOrDefault(term, 0.0);
                double value = Math.round((tfValue * idfValue) * 100000.0) / 100000.0;
                docTFIDF.put(term, value);
                tfidfs.add(new TFIDF(term, tfValue, idfValue, value));
            }

            tfidf.put(docId, docTFIDF);

            count++;
        }

        saveAsCsv(tfidfs, "tfidfs.csv");
        return tfidf;
    }

    public static void saveAsCsv(List<TFIDF> tfidfList, String filePath) throws IOException {
        FileWriter out = new FileWriter(filePath);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader("Term", "TF", "IDF", "TF-IDF"));

        for (TFIDF tfidf : tfidfList) {
            printer.printRecord(tfidf.getTerm(), tfidf.getTfValue(),
                    tfidf.getIdfValue(), tfidf.getValue());
        }

        printer.flush();
        printer.close();
    }

    private static void writeDataInFile(Map<String, Double> map, String fileName) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Double> mapEntry: map.entrySet()){
            builder.append(mapEntry.getKey()).append(" --- ")
                    .append(mapEntry.getValue()).append('\n');
        }
        WebCrawlerImpl.writeToFile(fileName, builder.toString());
    }

    public static Map<String, Double> calculateInverseDocumentFrequency(Map<String, List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = documents.size();
        for (String docId : documents.keySet()) {
            List<String> words = documents.get(docId);
            for (String word : words) {
                if (!idf.containsKey(word)) {
                    double count = 0;
                    for (String id : documents.keySet()) {
                        if (documents.get(id).contains(word)) {
                            count++;
                            System.err.println(count);
                        }
                    }
                    double roundedValue = Math.round((Math.log10(totalDocs / count)) * 100000.0) / 100000.0;
                    idf.put(word, roundedValue);
                }
            }
        }
        return idf;
    }

    public static Map<String, Double> calculateTermFrequency(List<String> words) {
        Map<String, Double> tf = new HashMap<>();
        int wordCount = words.size();

        for (String word : words) {
            tf.put(word, tf.getOrDefault(word, 0.0) + 1.0);
        }

        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double freq = entry.getValue();
            double roundedValue = Math.round((freq / wordCount) * 100000.0) / 100000.0;
            tf.put(term, roundedValue);
        }

        return tf;
    }
}
