package ru.itis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearch {

    private static final Analyzer ANALYZER = new StandardAnalyzer();

    private static final RussianStemmer stemmer = new RussianStemmer();

    public static void main(String[] args) throws IOException {
        Map<String, Set<String>> wordMap = new HashMap<>();
        wordMap.put("doc1", new HashSet<>(Arrays.asList("hello", "world")));
        wordMap.put("doc2", new HashSet<>(Arrays.asList("world", "java")));
        wordMap.put("doc3", new HashSet<>(Arrays.asList("programming", "language")));

        String query = "world & hello | !java";
        Set<String> result = search(wordMap, query);

        System.out.println("Результаты поиска для запроса: " + query);
        for (String doc : result) {
            System.out.println("- " + doc);
        }

        Map<String, Set<String>> wordMap1 = new HashMap<>();
        wordMap1.put("https://example.com/doc1", new HashSet<>(Arrays.asList("hello", "world", "java")));
        wordMap1.put("https://example.com/doc2", new HashSet<>(Arrays.asList("world", "programming", "language")));
        wordMap1.put("https://example.com/doc3", new HashSet<>(Arrays.asList("programming", "java", "python")));
        Map<String, Set<String>> invertedIndex = reverseIndex(wordMap1);
        System.out.println(invertedIndex);
    }

    public static Map<String, Set<String>> reverseIndex(Map<String, Set<String>> wordMap) throws IOException {
        Map<String, Set<String>> index = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : wordMap.entrySet()) {
            String url = entry.getKey();
            Set<String> words = entry.getValue();
            for (String word : words) {
                if (!index.containsKey(word)) {
                    index.put(word, new HashSet<>());
                }
                index.get(word).add(url);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : wordMap.entrySet()) {
            builder.append(entry.getKey()).append('\n');
            for (String s: entry.getValue()) {
                builder.append("- ").append(s).append('\n');
            }
            builder.append('\n').append('\n').append('\n');
        }
        WebCrawlerImpl.writeToFile("reverseIndex.txt", builder.toString());
        return index;
    }

    public static void searchDocumentsByQuery(Map<String, Set<String>> wordMap) throws IOException {
        String[] queries = {"мир & при | идеа", "мир | при | идеа",
                "мир & !при | !идеа", "мир | !при | !идеа"};
        StringBuilder resultBuilder = new StringBuilder();
        for (String query : queries) {
            Set<String> result = search(wordMap, query);
            String builder = result.stream().map(doc -> "- " + doc + "\n")
                    .collect(Collectors
                            .joining("", "Результаты поиска для запроса: " + query + '\n',
                                    ""));
            resultBuilder.append(builder).append('\n').append('\n').append('\n');
        }
        WebCrawlerImpl.writeToFile("queries.txt", resultBuilder.toString());
    }

    private static String getQueryByScanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter query: ");
        return scanner.nextLine();
    }

    public static Set<String> search(Map<String, Set<String>> wordMap, String query) throws IOException {
        // Разбиваем запрос на токены
        String[] tokens = query.split("\\s+");

        //Map<String, Set<String>> lematizedMap = new HashMap<>();
        //lematizedMap.put("", new HashSet<>(List.of(tokens)));
        //tokens = TextProcessorImpl.lemmatize(lematizedMap).get("").toArray(new String[0]);

        // Преобразуем в обратную польскую запись
        List<String> rpn = toRPN(tokens);
        // Выполняем операции в правильном порядке
        Stack<Set<String>> stack = new Stack<>();
        for (String token : rpn) {
            if (isOperator(token)) {
                Set<String> set2 = stack.pop();
                Set<String> set1 = stack.pop();
                Set<String> result = applyOperator(set1, set2, token);
                stack.push(result);
            } else if (token.startsWith("!")) {
                String word = token.substring(1);
                stack.push(findDocumentByToken(word, wordMap, false));
            } else {
                Set<String> result = findDocumentByToken(token, wordMap, true);
                stack.push(result);
            }
        }
        return stack.pop();
    }

    private static Set<String> findDocumentByToken(String token, Map<String, Set<String>> wordMap,
                                                   boolean isNeededContains) {
        Set<String> documents = new HashSet<>();
        wordMap.forEach((key, words) -> {
            if (words.contains(token) == isNeededContains) {
                documents.add(key);
            }
        });
        return documents;
    }

    public static List<String> toRPN(String[] tokens) {
        Stack<String> operatorStack = new Stack<>();
        List<String> outputQueue = new ArrayList<>();

        Map<String, Integer> precedenceMap = new HashMap<>();
        precedenceMap.put("&", 2);
        precedenceMap.put("|", 1);
        precedenceMap.put("!", 3);

        for (String token : tokens) {
            if (!isOperator(token)) {
                outputQueue.add(token);
            } else {
                // Пока стек операторов не пуст и приоритет оператора на вершине стека больше или равен приоритету текущего оператора
                while (!operatorStack.empty() && (precedenceMap.get(operatorStack.peek()) >= precedenceMap.get(token))) {
                    // Извлекаем оператор из стека операторов и добавляем его в выходную строку
                    outputQueue.add(operatorStack.pop());
                }
                // Добавляем текущий оператор в стек операторов
                operatorStack.push(token);
            }
        }
        // Если больше нет токенов, переносим оставшиеся операторы из стека операторов в выходную строку
        while (!operatorStack.empty()) {
            outputQueue.add(operatorStack.pop());
        }

        return outputQueue;
    }

    public static boolean isOperator(String token) {
        return token.equals("&") || token.equals("|") || token.equals("!");
    }

    public static Set<String> applyOperator(Set<String> set1, Set<String> set2, String operator) {
        Set<String> result = new HashSet<>();
        switch (operator) {
            case "&" -> {
                result.addAll(set1);
                result.retainAll(set2);
            }
            case "|" -> {
                result.addAll(set1);
                result.addAll(set2);
            }
            case "!" -> {
                result.addAll(set1);
                result.removeAll(set2);
            }
        }
        return result;
    }
}
