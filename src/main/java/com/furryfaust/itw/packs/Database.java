package com.furryfaust.itw.packs;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    public ArrayList<String> getMembers(String packName) {
        Document packQuery = getPack(packName);
        if (packQuery == null) {
            return null;
        }

        final ArrayList<String> members = new ArrayList<String>();
        BasicDBList dbList = (BasicDBList) packQuery.get("Members");

        Iterator<Object> iterator = dbList.iterator();
        while (iterator.hasNext()) {
            String uuid = (String) iterator.next();

            members.add(Bukkit.getServer().getPlayer(UUID.fromString(uuid)) != null ?
                    Bukkit.getServer().getPlayer(UUID.fromString(uuid)).getName() :
                    Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName());
        }

        return members;
    }

}
