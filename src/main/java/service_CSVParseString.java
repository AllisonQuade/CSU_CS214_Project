
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 * 
 * CSVParserService
 */
public class service_CSVParseString implements interface_CSVParser {
  private CSVParser parser = null;
  private Reader reader = null;

  /**
   * Read and parse through input file.
   * Chooses between file path and data string input.
   * 
   * @param inputDataString Choice of input type.
   * @param skipHeader      Choice to add or skip header.
   * @throws IOException If input file null or empty or file data empty.
   */
  public service_CSVParseString(String inputDataString, Boolean skipHeader) throws IOException { // create new file
                                                                                                 // object for each file
    if (util_FormatOutput.isNullOrEmpty(inputDataString)) {
      throw new IllegalArgumentException("inputDataString is required.");
    }
    this.reader = new StringReader(inputDataString); // opening text file

    CSVFormat inputFormat = CSVFormat.DEFAULT.builder() // open file
        .setSkipHeaderRecord(skipHeader)
        .build();

    this.parser = inputFormat.parse(reader); // parse through reader

    if (!this.parser.iterator().hasNext()) { // error handle input file empty
      throw new IllegalStateException("File data empty.");
    }

  }

  /**
   * Gets parser.
   * 
   * @return CSV parser.
   */
  public CSVParser getParser() {
    return this.parser;
  }

  /**
   * Closes CSV file.
   * 
   * @throws IOException If reader or parser closes.
   */
  public void closeCSVFile() throws IOException {
    if (this.parser != null) { // handles parser instance variable in THIS parser service
      this.parser.close();
    }

    if (this.reader != null) {
      this.reader.close();
    }
  }

}
