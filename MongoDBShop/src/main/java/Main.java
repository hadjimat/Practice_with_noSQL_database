import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;

public class Main {

    public static void main(String[] args) {

        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<Document> shops = database.getCollection("Shops");
        MongoCollection<Document> items = database.getCollection("Items");

        String shopName;
        String itemName;
        boolean exit = false;
        System.out.println("""
                    Use commands:
                    addShop
                    addItem
                    addItemToShop
                    showStatistics
                    exit""");

        while (!exit) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine().trim();

            switch (command) {
                case "addShop" -> {
                    System.out.print("Input shop: ");
                    shopName = scanner.nextLine();
                    Document shop = new Document()
                            .append("Name", shopName)
                            .append("Items", new ArrayList<String>());
                    shops.insertOne(shop);
                }
                case "addItem" -> {
                    System.out.print("Input item: ");
                    itemName = scanner.nextLine();
                    Document item = new Document().append("Name", itemName);
                    System.out.print("Input item's price: ");
                    String price = scanner.nextLine();
                    item.append("Price", Integer.valueOf(price));
                    items.insertOne(item);
                }
                case "addItemToShop" -> {
                    System.out.print("Insert item in shop. \n Input item: ");
                    itemName = scanner.nextLine();
                    System.out.print(" Input shop: ");
                    shopName = scanner.nextLine();
                    Document query = new Document().append("Name", shopName);
                    Document update = new Document("$push", new Document("Items", itemName));
                    shops.updateOne(query, update, new UpdateOptions().upsert(true));
                }
                case "showStatistics" -> {
                    System.out.println("Statistics.");
                    AggregateIterable<Document> aggregateIterable =
                            shops.aggregate(List.of(
                                    Aggregates.lookup("Items", "Items", "Name", "Statistics"),
                                    unwind("$Statistics"),
                                    group("$Name",
                                            avg("avgPrice", "$Statistics.Price"),
                                            min("minPrice", "$Statistics.Name"),
                                            max("maxPrice", "$Statistics.Name"),
                                            sum("count", 1),
                                            sum("countLessThanHundred", new Document("$cond",
                                                    Arrays.asList(new Document("$lt",
                                                            Arrays.asList("$Statistics.Price", 100)), 1, 0)))
                                    )));
                    for (Document doc : aggregateIterable) {
                        System.out.println("Shop: " + doc.get("_id") +
                                "\n Items count: " + doc.get("count") +
                                "\n Items count less than hundred: " + doc.get("countLessThanHundred") +
                                "\n Average price items: " + doc.get("avgPrice") +
                                "\n Min price item: " + doc.get("minPrice") +
                                "\n Max price item: " + doc.get("maxPrice"));
                    }
                }
                case "exit" -> {
                    System.out.println("Exit program!");
                    exit = true;
                }
                default -> {
                }
            }
        }
    }
}
