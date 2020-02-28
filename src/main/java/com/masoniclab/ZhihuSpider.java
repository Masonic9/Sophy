package com.masoniclab;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import okhttp3.Request;
import org.bson.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ZhihuSpider extends BreadthCrawler {

    ArrayList<String> urls = new ArrayList<>();
    ArrayList<Item> output = new ArrayList<>();

    List<Document> documents = new ArrayList<Document>();

    public ZhihuSpider(String path, boolean autoParse) {
        super(path, autoParse);
        setRequester(new ZhihuSpider.MyRequester());
    }

    public static class MyRequester extends OkHttpRequester {
        @Override
        public Request.Builder createRequestBuilder(CrawlDatum crawlDatum) {
            return super.createRequestBuilder(crawlDatum)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
        }
    }

    public void visit(Page page, CrawlDatums next) {
        if (page.code() == 404) {
            System.out.println("Error, request blocked");
            this.stop();
        }

        String empty_placeholder = page.select("div[class=zu-list-empyt-place-holder zg-r5px]").text();
        System.out.println(empty_placeholder);
        String url = page.url();
        // 页码溢出
        if (!empty_placeholder.contains("该收藏夹还没有任何内容")) {
            // 扩增url
            if (!url.contains("page")) {
                url += "?page=1";
            }

            int cursor = Integer.parseInt(url.substring(url.indexOf("=") + 1));
            cursor += 1;
            String baseurl = url.substring(0, url.indexOf("=") + 1);
            String nexturl = baseurl + cursor;
            next.add(nexturl);

            // 写入
            Elements items = page.select("div[class=zm-item]");
            documents.clear();
            for (Element item : items) {
                Elements title = item.select("h2[class=zm-item-title]").select("a[target=_blank]");
                Elements favs = item.select("div[class=zm-item-fav]");

                Item i = new Item(title.text(),
                        title.attr("href").startsWith("/") ? "http://www.zhihu.com" + title.attr("href") : title.attr("href"),
                        favs.select("div[tabindex=-1]").select("div[tabindex=-1]").select("div[class=zm-item-vote]").select("a[class=zm-item-vote-count js-expand js-vote-count]").text(),
                        favs.select("a[class=author-link]").text(),
                        "http://www.zhihu.com" + favs.select("a[class=author-link]").attr("href"));
                System.out.println(i.title);
                output.add(i);
            }
        }
    }

    public static ArrayList<Item> start(ArrayList<String> urls) throws Exception {
        ZhihuSpider zs = new ZhihuSpider("crawl", true);
        zs.urls = urls;

        // 格式: https://www.zhihu.com/collection/196329856
        // 或https://www.zhihu.com/collection/196329856?page=1

        String pattern1 = "https://www.zhihu.com/collection/\\d+";
        String pattern2 = "http://www.zhihu.com/collection/\\d+\\?page=1";

        for (String url : urls) {
            if (Pattern.matches(pattern1, url) || Pattern.matches(pattern2, url)) {
                zs.addSeed(url);
            }
        }

        zs.setThreads(5);
        zs.getConf().setExecuteInterval(2500);

        zs.output.clear();
        zs.start(Core.fav_pages);
        zs.stop();
        return zs.output;
    }
}

class Item {
    String title;
    String url;
    String upvote;
    String author_name;
    String author_link;

    public Item(String title, String url, String upvote, String author_name, String author_link) {
        this.title = title;
        this.url = url;
        this.upvote = upvote;
        this.author_name = author_name;
        this.author_link = author_link;
    }
}