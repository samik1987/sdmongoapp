package com.example.sdmongoapp.service;


import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PaymentService {

    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 27017;
    private static final String DB_NAME = "payments";
    private static final String COLLECTION_NAME = "payment_details";
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "admin123";
    private static final String AUTH_DB = "admin"; // or "your_db_name" if user is created there

    private MongoCollection<Document> getCollection() {
        MongoCredential credential = MongoCredential.createCredential(
                DB_USER, AUTH_DB, DB_PASS.toCharArray());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(DB_HOST, DB_PORT))))
                .credential(credential)
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        return mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
    }

    // -----------------------
    // 1. Get Aggregated Payments
    // -----------------------
    public List<Document> getAggregatedPayments() {
        MongoCollection<Document> collection = getCollection();

        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("status", "SUCCESS")),
                Aggregates.group("$status", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
        )).allowDiskUse(true);

        return StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
    }

    public List<Document> getAllSuccessPayments() {
        MongoCollection<Document> collection = getCollection();

        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("status", "SUCCESS"))
               )).allowDiskUse(true);

        return StreamSupport.stream(result.spliterator(), false)
                .collect(Collectors.toList());
    }

    // -----------------------
    // 2. Add Payment
    // -----------------------
    public String addPayment(String payer, double amount, String status) {
        Document doc = new Document("payer", payer)
                .append("amount", amount)
                .append("status", status)
                .append("createdAt", new Date());

        getCollection().insertOne(doc);
        return doc.getObjectId("_id").toHexString();
    }

    // -----------------------
    // 3. Update Payment Status by ID
    // -----------------------
    public boolean updatePaymentStatus(String id, String newStatus) {
        ObjectId objectId = new ObjectId(id);
        UpdateResult result = getCollection().updateOne(
                Filters.eq("_id", objectId),
                Updates.set("status", newStatus)
        );
        return result.getModifiedCount() > 0;
    }

    // -----------------------
    // 4. Delete Payment by ID
    // -----------------------
    public boolean deletePayment(String id) {
        ObjectId objectId = new ObjectId(id);
        DeleteResult result = getCollection().deleteOne(Filters.eq("_id", objectId));
        return result.getDeletedCount() > 0;
    }
}
