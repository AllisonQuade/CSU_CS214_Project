import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
//DLLs ^

/**
 * 
 * SongUserServicePA1
 */
public class SongUserServicePA1 {
    service_UserSongCSVDataLoad fileService;
    // https://www.geeksforgeeks.org/java/sorting-hashmap-according-key-value-java/
    // use object to return song and users so you don't have to read file multiple
    // times
    // private LoadSongsandUsersFromCSV_Result result;

    // #region PA1

    /**
     * Loads in input file path.
     * Uses CSVParserService to parse through inputFile.
     * 
     * @param inputFile A CSV input file path.
     * @return songList and userList.
     * @throws IOException If input file is null or empty.
     */
    public TreeMap<String, data_Song> loadSongsandUsersFromCSV(String inputFile) throws IOException {
        fileService = new service_UserSongCSVDataLoad();

        if (util_FormatOutput.isNullOrEmpty(inputFile)) {
            throw new IllegalArgumentException("Input file is required and must contain a file path and file name.");
        }

        fileService.loadFileData(inputFile);

        return fileService.getSongList();

    }

    /**
     * Output song name, number of ratings for song, mean, and standard deviation.
     * 
     * @param outputFile Output CSV file path created by user to output data to.
     * @param songs      Song list used to populate output.
     * @throws IOException If output file is null or empty and if song map is null.
     */
    public void outputUsersSongsRatingsCSV(String outputFile, TreeMap<String, data_Song> songs) throws IOException {
        // TreeMap<String, Cs214Song> songs = result.songList;
        if (util_FormatOutput.isNullOrEmpty(outputFile)) {
            throw new IllegalArgumentException("Output file is required and must contain a file name.");
        }

        if (songs == null) {
            throw new IllegalArgumentException("songs parameter required.");
        }

        service_CSVPrinter printerService = null;

        try {
            String[] headers = new String[] { "song", "number of ratings", "mean", "standard deviation" };

            printerService = new service_CSVPrinter(outputFile, headers);
            for (Map.Entry<String, data_Song> entry : songs.entrySet()) {
                data_Song song = entry.getValue();
                Object[] values = new Object[] { song.getSongName(), song.getRatingsCount(), song.getRunningMean(),
                        song.getStandardDeviation() };

                printerService.printRecord(values);
            }

        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }
    // #endregion PA1
}
