
import java.io.IOException;

/**
 * 
 * SongUserServicePA3V2
 */
public class SongUserServicePA3V2 {
    service_SongSimilarity songSimilarityService;
    service_UserSongCSVDataLoad fileService;

    /**
     * Calls methods to load from CSV, clear data, populate placeholder data, and
     * calculate song similarity.
     * 
     * @param inputFile Input CSV file path.
     * @throws IOException If invalid input file.
     */
    public void loadAndProcessData(String inputFile) throws IOException { // for main
        fileService = new service_UserSongCSVDataLoad();

        songSimilarityService = new service_SongSimilarity();

        fileService.loadAndCleanAndProcessFileData(inputFile);

        songSimilarityService.calcSongSimilarity(fileService.getUserList(), fileService.getMasterSongNameList());
    }

    /**
     * Validate output arguments.
     * 
     * @param outputFile Output CSV file path.
     */
    public void validateArguments(String outputFile) {
        if (util_FormatOutput.isNullOrEmpty(outputFile)) {
            throw new IllegalArgumentException("Output file is required and must contain a file name.");
        }

        if (fileService.getUserList() == null) {
            throw new IllegalArgumentException("songs parameter required.");
        }
    }

    /**
     * Outputs song1 name, song2 name, and similarity between songs.
     * 
     * @param outputFile Output CSV file path.
     * @throws IOException If printer service fail.
     */
    public void outputUsersSongsRatingsCSV(String outputFile) throws IOException {
        validateArguments(outputFile);

        service_CSVPrinter printerService = null;

        try {
            String[] headers = new String[] { "name1", "name2", "similarity" };

            printerService = new service_CSVPrinter(outputFile, headers);

            for (int i = 0; i < songSimilarityService.getSongSimilarityMatrix().length; i++) {
                for (int j = 0; j < songSimilarityService.getSongSimilarityMatrix().length; j++) {
                    printRecord(printerService, i, j);
                }
            }

        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }

    /**
     * Outputs values objects array with song 1 name, song 2 name, and dimensions
     * matrix.
     * 
     * @param printerService CSV printer used to print record.
     * @param i              Index 1 of matrix.
     * @param j              Index 2 of matrix.
     * @throws IOException if printer fails.
     */
    private void printRecord(service_CSVPrinter printerService, int i, int j) throws IOException {
        if (this.songSimilarityService.getSongSimilarityMatrix()[i][j] != null
                && (i != j && this.songSimilarityService.getSongSimilarityMatrix()[i][j] > 0.0
                        || this.songSimilarityService.getSongSimilarityMatrix()[i][j].equals(Double.NaN))) {
            Object[] values = { fileService.getMasterSongNameList().get(i), fileService.getMasterSongNameList().get(j),
                    songSimilarityService.getSongSimilarityMatrix()[i][j] };
            printerService.printRecord(values);
        }
    }
}
