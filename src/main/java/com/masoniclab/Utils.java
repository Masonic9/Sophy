package com.masoniclab;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Utils {

    public static MongoCollection<Document> getCollection(String col_name) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("zhihu");
        return database.getCollection(col_name);
    }
}
