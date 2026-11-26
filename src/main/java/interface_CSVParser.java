import java.io.IOException;

import org.apache.commons.csv.CSVParser;

public interface interface_CSVParser {
     CSVParser getParser();
     void closeCSVFile()  throws IOException;
}
