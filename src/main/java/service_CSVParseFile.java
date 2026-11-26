import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 * 
 * CSVParserService
 */
public class service_CSVParseFile implements interface_CSVParser {
  private String inputFile;
  private CSVParser parser = null;
  private Reader reader = null;

  /**
   * Read and parse CSV input file.
   * 
   * @param newInputFile Input CSV file path,
   * @param skipHeader   Choice of header.
   * @throws IOException If input file null or empty, if input file not CSV
   */
  public service_CSVParseFile(String newInputFile, boolean skipHeader) throws IOException { // create new file object
                                                                                            // for each file

    validateInputFile(newInputFile);

    this.inputFile = newInputFile;

    CSVFormat inputFormat = CSVFormat.DEFAULT.builder() // open file
        .setSkipHeaderRecord(skipHeader)
        .build();

    this.reader = new FileReader(this.inputFile); // opening text file
    this.parser = inputFormat.parse(reader); // parse through reader

    if (!this.parser.iterator().hasNext()) { // error handle input file empty
      throw new IllegalStateException("File data empty.");
    }

  }

  private void validateInputFile(String newInputFile) {
    if (util_FormatOutput.isNullOrEmpty(newInputFile)) {
      throw new IllegalArgumentException("newInputFile is required and must contain a file path and file name.");
    }

    boolean fileEnd = newInputFile.endsWith("csv");
    if (!fileEnd) {
      throw new IllegalArgumentException("The parameter \"newInputFile\" must have `.csv` extension");
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
