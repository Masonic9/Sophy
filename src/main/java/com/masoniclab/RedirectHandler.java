package com.masoniclab;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.util.ExceptionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RedirectHandler extends BreadthCrawler {
    ArrayList<String> urls = new ArrayList<>();
   ArrayList<String> urls_output = new ArrayList<>();

    public RedirectHandler(String path, boolean autoParse) {
        super(path, autoParse);
    }

    public void visit(Page page, CrawlDatums next) {
        if (page.code() == 302 || page.code() == 307) {
            try {
                String redirectUrl = new URL(new URL(page.url()), page.location()).toExternalForm();
                urls_output.add(redirectUrl);
            } catch (MalformedURLException e) {
                ExceptionUtils.fail(e);
            }
        }
    }

    public static ArrayList<String> start(ArrayList<String> urls) throws Exception {
        RedirectHandler rh = new RedirectHandler("crawl", false);
        rh.urls = urls;
        for (String url : rh.urls) {
            rh.addSeed(url);
        }
        rh.getConf().setExecuteInterval(1000);
        rh.urls.clear();
        rh.urls_output.clear();
        rh.start(1);
        rh.stop();
        return rh.urls_output;
    }
}
