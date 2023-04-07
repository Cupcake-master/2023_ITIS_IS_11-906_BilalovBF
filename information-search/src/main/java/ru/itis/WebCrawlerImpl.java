package ru.itis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebCrawlerImpl {
    private static final int MIN_WORDS_PER_PAGE = 1001;
    private static final int MAX_PAGES_TO_DOWNLOAD = 101;
    private final static Map<String, String> urls = new ConcurrentHashMap<>();

    public static Map<String, String> getAllInternalUrlWithScanner() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter starting web page URL: ");
        String startingUrl = scanner.nextLine();
        findInternalUrls(startingUrl);
        writePagesToFile();
        int pageCount = 0;
        for (Map.Entry<String, String> map : urls.entrySet()) {
            String fileName = "page_" + pageCount + ".txt";
            writeToFile(fileName, map.getValue());
            pageCount++;
        }
        return urls;
    }

    private static void findInternalUrls(String url) {
        while (MAX_PAGES_TO_DOWNLOAD > urls.size()) {
            try {
                Document document = Jsoup.connect(url).get();
                Elements links = document.select("a[href]");
                String text = document.text();

                if (text.length() > MIN_WORDS_PER_PAGE){
                    text = text.substring(0, MIN_WORDS_PER_PAGE);
                }

                if (!urls.containsKey(url)) {
                    urls.put(url, text);
                }

                List<String> linkList = links.stream()
                        .map(link -> link.attr("abs:href"))
                        .limit(MAX_PAGES_TO_DOWNLOAD)
                        .toList();

                for (String currentUrl : linkList) {
                    try {
                        String currentText = Jsoup.connect(currentUrl).get().text();
                        if (MAX_PAGES_TO_DOWNLOAD > urls.size()) {
                            urls.put(currentUrl, currentText);
                        }
                    } catch (Exception ex) {
                        System.err.println("Unhandled content type. Must be text/*, application/xml, or application/*+xml." +
                                " URL=" + currentUrl);
                    }
                }
                urls.keySet().forEach(WebCrawlerImpl::findInternalUrls);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writePagesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pages/index.txt"))) {
            int index = 0;
            for (String page : urls.keySet()) {
                writer.write(index + "," + page);
                index++;
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String fileName, String text) throws IOException {
        PrintWriter writer = new PrintWriter(
                new FileOutputStream(
                        new File("pages", fileName), true));
        writer.println(text);
        writer.close();
    }
}
