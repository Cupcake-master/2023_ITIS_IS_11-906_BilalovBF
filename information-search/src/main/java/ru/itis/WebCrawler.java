package ru.itis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;

public class WebCrawler {
    private static final int MIN_WORDS_PER_PAGE = 1000;
    private static final int MAX_PAGES_TO_DOWNLOAD = 100;
    private static int pageCount = 0;

    private static final Set<String> visitedPages = new HashSet<>();
    private static final Set<String> downloadPages = new HashSet<>();

    public static Set<String> startFirstHomework() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter starting web page URL: ");
        String startingUrl = scanner.nextLine();
        downloadPages(startingUrl);
        return downloadPages;
    }

    private static void downloadPages(String url) throws IOException {
        Queue<String> urlsToVisit = new LinkedList<>();
        urlsToVisit.add(url);

        while (!urlsToVisit.isEmpty() && pageCount < MAX_PAGES_TO_DOWNLOAD) {
            String currentUrl = urlsToVisit.poll();
            if (visitedPages.contains(currentUrl)) {
                continue;
            }
            visitedPages.add(currentUrl);

            Document doc = Jsoup.connect(currentUrl).get();
            String text = doc.text();
            if (text.split("\\s+").length < MIN_WORDS_PER_PAGE) {
                continue;
            }
            String fileName = "page_" + pageCount + ".txt";
            writeToFile(fileName, text);
            writeToFile("index.txt", pageCount + "," + currentUrl);
            pageCount++;

            doc.select("a[href]").stream()
                    .map(link -> link.attr("abs:href"))
                    .filter(childUrl -> childUrl.startsWith(url))
                    .forEachOrdered(urlsToVisit::add);
        }
    }

    private static void writeToFile(String fileName, String text) throws IOException {
        PrintWriter writer = new PrintWriter(
                new FileOutputStream(
                        new File("pages", fileName), true));
        writer.println(text);
        downloadPages.add(text);
        writer.close();
    }
}