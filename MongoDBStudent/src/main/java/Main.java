import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.bson.Document;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, CsvValidationException {

        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<Document> collection = database.getCollection("Students");
        collection.drop();

        CSVReader csvReader = new CSVReader(new FileReader("src/main/resources/mongo.csv"));

        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            Document document = new Document()
                    .append("Name", nextLine[0])
                    .append("Age", nextLine[1]);
            CSVReader coursesReader = new CSVReader(new StringReader(nextLine[2]));
            String[] coursesLine;
            if ((coursesLine = coursesReader.readNext()) != null) {
                document.append("Courses", Arrays.stream(coursesLine).collect(Collectors.toList()));
            }
            collection.insertOne(document);
        }

        System.out.println("Total students count: " +
                collection.countDocuments());

        System.out.println("Students count more than 40 age: " +
                collection.countDocuments(Filters.gt("Age", "40")));

        System.out.println("Most young student's name: " +
                Objects.requireNonNull(collection.find().sort(Sorts.ascending("Age")).first()).get("Name"));

        System.out.println("Elder student's courses: " +
                Objects.requireNonNull(collection.find().sort(Sorts.descending("Age")).first()).get("Courses"));

    }
}

