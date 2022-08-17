package de.Skippero.LOA.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Website
{
    private Document doc;

    public static Website getWebsiteByUrl(String url)
    {
        return new Website(url);
    }

    private Website(String url)
    {
        try { setDoc(Jsoup.connect(url).get()); } catch (Exception e) { }
    }

    public void setDoc(Document doc)
    {
        this.doc = doc;
    }

    public Document getDoc()
    {
        return doc;
    }
}
