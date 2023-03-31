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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class TextProcessor {
    // Объявляем константу ANALYZER, инициализируем её объектом класса StandardAnalyzer()
    private static final Analyzer ANALYZER = new StandardAnalyzer();
    // Объявляем константу STOPWORDS, инициализируем её пустым множеством строк
    private static final Set<String> STOPWORDS = getDefaultStopWords();
    // Создаем экземпляр класса RussianStemmer, который будем использовать для стемминга
    private static final RussianStemmer stemmer = new RussianStemmer();

    // Метод tokenize получает на вход коллекцию строк и возвращает список токенов
    public static Set<String> tokenize(Collection<String> strings) throws IOException {
        // Создаем объект класса StandardAnalyzer
        Analyzer analyzer = new StandardAnalyzer();
        // Создаем список токенов
        Set<String> tokens = new HashSet<>();
        // Проходим по каждой строке из переданной коллекции
        for (String string : strings) {
            // Создаем объект tokenStream, который разбивает строку на токены
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(string));
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
        }
        // Закрываем объект analyzer
        analyzer.close();
        // Возвращаем список токенов
        return tokens;
    }

    // Метод lemmatize получает на вход список токенов и возвращает список лемм
    public static String[] lemmatize(Set<String> tokens) throws IOException {
        // Создаем список лемм
        Set<String> lemmas = new HashSet<>();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lemmas.toArray(new String[0]);
    }

    public static String[] removeStopWords(String[] tokens) {
        return Arrays.stream(tokens)
                .filter(token -> !STOPWORDS.contains(token))
                .toArray(String[]::new);
    }

    private static Set<String> getDefaultStopWords() {
        Set<String> stopWords = new HashSet<>();
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