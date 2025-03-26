package de.Skippero.LOA.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.Optional;

public class Website {
    private final Document doc;

    private Website(Document doc) {
        this.doc = doc;
    }

    public static Optional<Website> getWebsiteByUrl(String url) {
        try {
            Connection con = Jsoup.connect(url)
                    .timeout(5000)
                    .ignoreHttpErrors(true) 
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            Document doc = con.get();
            return Optional.of(new Website(doc));
        } catch (IOException e) {
            System.err.println("Fehler beim Abrufen der Website: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Document getDoc() {
        return doc;
    }
}
