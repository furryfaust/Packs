package com.furryfaust.itw.packs;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.UUID;

public class Database {
    MongoCollection<Document> collection;

    public Database(FileConfiguration config) {
        MongoClient client = new MongoClient();

        collection = client.getDatabase(config.getString("MongoDB.DBName")).getCollection(config.getString("MongoDB.Collection"));
    }

    public void createPack(String packName, String alpha) {
        Document pack = new Document()
                .append("Name", packName)
                .append("Members", Arrays.asList(new Document()
                        .append("Name", alpha)
                        .append("Role", "Alpha")))
                .append("Invited", Arrays.asList())
                .append("Claims", Arrays.asList());
        collection.insertOne(pack);
    }

    public boolean isAlpha(UUID uuid) {
        FindIterable<Document> results = collection.find(new Document("Members", new Document("$elemMatch", new Document("Name", uuid.toString()).append("Role", "Alpha"))));

        return results.first() != null;
    }

    public Document getPack(UUID uuid) {
        FindIterable<Document> results = collection.find(new Document("Members", new Document("$elemMatch", new Document("Name", uuid.toString()))));

        return results.first() == null ? null : results.first();
    }

    public Document getPack(String packName) {
        FindIterable<Document> results = collection.find(new Document("Name", packName));

        return results.first() == null ? null : results.first();
    }

    public boolean disbandPack(UUID uuid) {
        Document packQuery = new Document("Members", new Document("$elemMatch", new Document("Name", uuid.toString()).append("Role", "Alpha")));

        return collection.deleteOne(packQuery).getDeletedCount() != 0;
    }

    public boolean inviteToPack(UUID uuid, String packName) {
        Document inviteCheck = new Document()
                .append("Name", packName)
                .append("Invited", uuid.toString());

        FindIterable<Document> results = collection.find(inviteCheck);
        if (results.first() != null) {
            return false;
        }

        collection.updateOne(new Document("Name", packName), new Document("$push", new Document("Invited", uuid.toString())));
        return true;
    }

    public void stripeInvites(UUID uuid) {
        Document inviteQuery = new Document("Invited", uuid.toString());

        collection.updateMany(inviteQuery, new Document("$pull", new Document("Invited", uuid.toString())));
    }

}
