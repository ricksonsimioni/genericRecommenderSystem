package genericRecommenderSystem.genericRecommenderSystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
	public static void writeToFile(String csvContent, String csvFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
        writer.write(csvContent);
        writer.close();
    }
}
