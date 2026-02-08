package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static Object[][] readCsv(String filePath) {

        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] values = line.split(",", -1);
                rows.add(values);

                if (values.length > maxColumns) {
                    maxColumns = values.length;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read CSV: " + filePath, e);
        }

        Object[][] data = new Object[rows.size()][maxColumns];

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            for (int j = 0; j < maxColumns; j++) {
                data[i][j] = j < row.length
                        ? row[j].trim()
                        : "";
            }
        }

        return data;
    }
}
