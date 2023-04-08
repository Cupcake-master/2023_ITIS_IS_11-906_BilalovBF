package ru.itis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Attribute;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.*;
import java.util.*;

public class TextProcessorImpl {

    private static final Analyzer ANALYZER = new StandardAnalyzer();

    private static final RussianStemmer stemmer = new RussianStemmer();

    public static Map<String, List<String>> start(Map<String, String> map) throws IOException {
        Map<String, List<String>> tokens = tokenize(map);
        Map<String, List<String>> lemmatizes = lemmatize(tokens);
        return lemmatizes;
    }

    private static Map<String, List<String>> tokenize(Map<String, String> map) throws IOException {
        // Создаем объект класса StandardAnalyzer
        Analyzer analyzer = new StandardAnalyzer();
        Map<String, List<String>> tokenizedMap = new HashMap<>();
        for (Map.Entry<String, String> m : map.entrySet()) {
            // Создаем список токенов
            List<String> tokens = new LinkedList<>();
            // Проходим по каждой строке из переданной коллекции

            // Создаем объект tokenStream, который разбивает строку на токены
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(m.getValue()));
            // Получаем атрибут TermAttribute для извлечения токенов
            TermAttribute termAttribute = (TermAttribute) tokenStream.addAttribute(TermAttribute.class);
            // Сбрасываем токены
            tokenStream.reset();
            // Проходим по всем токенам и добавляем их в список tokens
            while (tokenStream.incrementToken()) {
                String token = termAttribute.term();
                tokens.add(token);
            }
            // Завершаем работу с tokenStream
            tokenStream.end();
            tokenStream.close();
            // Закрываем объект analyzer
            tokenizedMap.put(m.getKey(), tokens);
        }
        return tokenizedMap;
    }

    public static Map<String, List<String>> lemmatize(Map<String, List<String>> map) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>>  m : map.entrySet()) {
            List<String> tokens = m.getValue();
            // Создаем список лемм
            List<String> lemmas = new LinkedList<>();
            try {
                // Создаем объект tokenStream, который фильтрует токены с помощью SnowballFilter, LowerCaseFilter и CachingTokenFilter, а затем стеммирует их с помощью RussianStemmer
                TokenStream tokenStream = new SnowballFilter(new LowerCaseFilter(
                        new CachingTokenFilter(ANALYZER
                                .tokenStream("", new StringReader(String.join(" ", tokens))))), stemmer);
                // Получаем атрибут TermAttribute для извлечения токенов
                Attribute termAttribute = tokenStream.addAttribute(TermAttribute.class);
                // Сбрасываем токены
                tokenStream.reset();
                // Проходим по всем токенам и добавляем их в список lemmas
                while (tokenStream.incrementToken()) {
                    String term = termAttribute.toString();
                    lemmas.add(term);
                }
                // Завершаем работу с tokenStream
                tokenStream.end();
                result.put(m.getKey(), removeStopWords(lemmas));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static List<String> removeStopWords(List<String> inputSet) {
        List<String> stopWords = getDefaultStopWords();
        List<String> result = new LinkedList<>();
        for (String word : inputSet) {
            if (!stopWords.contains(word.toLowerCase())) {
                result.add(word);
            }
        }
        return result;
    }

    private static List<String> getDefaultStopWords() {
        List<String> stopWords = new LinkedList<>();
        try (Scanner scanner = new Scanner(new File("stop_words.txt"))) {
            while (scanner.hasNext()) {
                String output = scanner.next();
                String[] words = output.split(" ");
                Collections.addAll(stopWords, words);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return stopWords;
    }
}
