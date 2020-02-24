package com.masoniclab;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import okhttp3.Request;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class IndexSpider extends BreadthCrawler {

    static ArrayList<String> urls = new ArrayList<>();

    public IndexSpider(String path, boolean autoParse) {
        super(path, autoParse);
        setRequester(new MyRequester());
    }

    public static class MyRequester extends OkHttpRequester {
        // 伪装Header访问百度搜索
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
        page.charset("utf-8");

        Elements items = page.select("div[class=result c-container ]");
        for (Element item : items) {
            urls.add(item.select("h3[class=t]").select("a[target=_blank]").attr("href"));
        }

    }

    public static ArrayList<String> start(String keyword, int pages) throws Exception {
        IndexSpider is = new IndexSpider("crawl", true);
        is.getConf().setExecuteInterval(2000);
        is.setThreads(10);

        for (int i = 0; i < pages; i++) {
            is.addSeed("https://www.baidu.com/s?wd=" + keyword + "+收藏夹&si=zhihu.com&ct=2097152&pn=" + i*10);
        }
        urls.clear();
        is.start(1);
        is.stop();
        return urls;
    }
}