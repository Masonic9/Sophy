package com.masoniclab;

import java.io.*;
import java.util.ArrayList;

public class Core {

    static int fav_pages = 10;
    static int index_pages = 5;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("请输入搜索关键词");
            System.out.println("Usage: java -jar sophy.jar <keyword> [index_pages] [fav_pages]");
            System.out.println("Powered By MasonicLab");
            return;
        }

        String keyword;

        switch (args.length) {
            case 1:
                switch (args[0]) {
                    case "help":
                        System.out.println("Usage: java -jar sophy.jar <keyword> [index_pages] [fav_pages]");
                        System.out.println("Powered By MasonicLab");
                        return;

                    default:
                        keyword = args[0];
                        run(keyword);
                        return;
                }
            case 2:
                switch (args[0]) {
                    case "mine":
                        String id = args[1];
                        fav_pages = 100;
                        runById(id);
                        return;
                }
            case 3:
                keyword = args[0];
                index_pages = Integer.parseInt(args[1]);
                fav_pages = Integer.parseInt(args[2]);
                run(keyword);
        }
    }


    public static void runById(String id) throws Exception {
        ArrayList<String> urls = new ArrayList<>();
        urls.add("https://www.zhihu.com/collection/" + id);
        ArrayList<Item> items = ZhihuSpider.start(urls);
        save(items, id);
    }

    public static void run(String keyword) throws Exception {
        ArrayList<String> urls = IndexSpider.start(keyword, index_pages);
        urls = RedirectHandler.start(urls);

        ArrayList<Item> items = ZhihuSpider.start(urls);
        save(items, keyword);
    }

    public static void save(ArrayList<Item> items, String fname) throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname + ".csv"), "GBk")));
        out.write("title,url,upvote,author_name,author_link\n");
        for (Item item : items) {
            out.write(item.title.replace(",","") + "," + item.url + "," + item.upvote + "," + item.author_name + "," + item.author_link + "\n");
        }
        out.flush();
        out.close();
    }
}