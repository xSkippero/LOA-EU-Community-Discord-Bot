package de.Skippero.LOA.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Website {
    private Document doc;

    private Website(String url) {
        try {
            setDoc(Jsoup.connect(url).get());
        } catch (Exception e) {
        }
    }

    public static Website getWebsiteByUrl(String url) {
        return new Website(url);
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }
}
