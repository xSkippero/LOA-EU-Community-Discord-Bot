package de.Skippero.LOA.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Website {
    private Document doc;

    private Website(String url) {
        try {
            Connection con = Jsoup.connect(url);
            doc = con.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Website getWebsiteByUrl(String url) {
        return new Website(url);
    }

    public Document getDoc() {
        return doc;
    }
}
