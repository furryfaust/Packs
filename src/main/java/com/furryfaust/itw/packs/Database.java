package com.furryfaust.itw.packs;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.UUID;

public class Database {
    MongoCollection<Document> collection;

    public Database(FileConfiguration config) {
        MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017", MongoClientOptions.builder().socketKeepAlive(true));
        MongoClient client = new MongoClient(uri);

        collection = client.getDatabase(config.getString("MongoDB.DBName")).getCollection(config.getString("MongoDB.Collection"));
    }

    public void createPack(String packName, String alpha) {
        Document pack = new Document()
                .append("Name", packName)
                .append("Name_lwr", packName.toLowerCase())
                .append("Members", Arrays.asList(new Document()
                        .append("Name", alpha)
                        .append("Role", "Alpha")))
                .append("Invited", Arrays.asList())
                .append("Claims", Arrays.asList());
        collection.insertOne(pack);
    }

    public Document getPack(UUID uuid) {
        FindIterable<Document> results = collection.find(new Document("Members", new Document("$elemMatch", new Document("Name", uuid.toString()))));

        return results.first();
    }

    public Document getPack(String packName) {
        FindIterable<Document> results = collection.find(new Document("Name_lwr", packName.toLowerCase()));

        return results.first();
    }

    public boolean disbandPack(UUID uuid) {
        Document packQuery = new Document("Members", new Document("$elemMatch", new Document("Name", uuid.toString()).append("Role", "Alpha")));

        return collection.deleteOne(packQuery).getDeletedCount() != 0;
    }

    public boolean inviteToPack(UUID uuid, String packName) {
        Document inviteCheck = new Document()
                .append("Name_lwr", packName.toLowerCase())
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

    public boolean joinPack(UUID uuid, String packName) {
        Document packQuery = new Document("Name_lwr", packName.toLowerCase());

        UpdateResult result = collection.updateOne(packQuery,
                new Document("$pull", new Document("Invited", uuid.toString())));

        if (result.getModifiedCount() == 0) {
            return false;
        }
        stripeInvites(uuid);

        Document member = new Document()
                .append("Name", uuid.toString())
                .append("Role", "Beta");

        collection.updateOne(packQuery,
                new Document("$push", new Document("Members", member)));
        return true;
    }

    public void leavePack(UUID uuid) {
        Document member = new Document()
                .append("Name", uuid.toString());

        collection.updateOne(new Document("Members", member), new Document("$pull", new Document("Members", member)));
    }

    public void toElder(UUID uuid) {
        Document packQuery = new Document("Members.Name", uuid.toString());

        collection.updateOne(packQuery,
                new Document("$set", new Document("Members.$.Role", "Elder")));
    }

    public void toMember(UUID uuid) {
        Document packQuery = new Document("Members.Name", uuid.toString());

        collection.updateOne(packQuery,
                new Document("$set", new Document("Members.$.Role", "Member")));
    }

    public Document getPackAtChunk(Chunk chunk) {
        Document claimQuery = new Document("Claims", new Document()
                .append("World", chunk.getWorld().getName())
                .append("X", chunk.getX())
                .append("Z", chunk.getZ()));

        FindIterable<Document> results = collection.find(claimQuery);

        return results.first();
    }

    public void claim(Chunk chunk, String packName) {
        Document claim = new Document()
                .append("World", chunk.getWorld().getName())
                .append("X", chunk.getX())
                .append("Z", chunk.getZ());

        Document packQuery = new Document("Name_lwr", packName.toLowerCase());

        collection.updateOne(packQuery, new Document("$push", new Document("Claims", claim)));
    }

    public void declaim(Chunk chunk) {
        Document claim = new Document()
                .append("World", chunk.getWorld().getName())
                .append("X", chunk.getX())
                .append("Z", chunk.getZ());

        collection.updateOne(new Document("Claims", claim), new Document("$pull", new Document("Claims", claim)));
    }

}
