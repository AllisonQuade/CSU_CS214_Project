import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
//DLLs ^

/**
 * 
 * SongUserServicePA2
 */
public class SongUserServicePA2 {
    service_UserSongCSVDataLoad fileService;
    // https://www.geeksforgeeks.org/java/sorting-hashmap-according-key-value-java/
    // use object to return song and users so you don't have to read file multiple
    // times

    /**
     * Calls methods to load data and clean data.
     * 
     * @param inputFile Input CSV file path.
     * @throws IOException If load songs and users from csv fails.
     */
    public void loadAndProcessFileData(String inputFile) throws IOException {
        fileService = new service_UserSongCSVDataLoad();
        fileService.loadAndCleanFileData(inputFile);
    }

    /**
     * Given that the input path is in string format, parse the data.
     * 
     * @param inputData Input data string.
     * @throws IOException If parser service fails.
     */
    public void loadAndProcessStringData(String inputData) throws IOException {
        fileService = new service_UserSongCSVDataLoad();
        fileService.loadAndCleanStringData(inputData);
    }

    // #region PA2

    /**
     * Outputs username, song name, and rating of data.
     * 
     * @param outputFile Output CSV file path created by user to output data to.
     * @throws IOException If outputfile null or empty.
     */
    public void outputUsersSongsRatingsCSV(String outputFile) throws IOException {
        if (util_FormatOutput.isNullOrEmpty(outputFile)) {
            throw new IllegalArgumentException("Output file is required and must contain a file name.");
        }

        service_CSVPrinter printerService = null;

        try {
            String[] headers = new String[] { "username", "song", "rating" };

            printerService = new service_CSVPrinter(outputFile, headers);
            // Step through each user
            for (Map.Entry<String, data_User> entry : fileService.getUserList().entrySet()) {

                String userName = entry.getKey();
                TreeMap<String, Double> songNameRating = entry.getValue().getSongList();

                // Step through each song/rating for this user
                Double rating;
                Object[] record;
                for (String songName : fileService.getMasterSongNameList()) {
                    if (songNameRating.containsKey(songName)) {
                        rating = songNameRating.get(songName);
                        Integer intRating = rating.intValue();
                        record = new Object[] { userName, songName, intRating };
                    } else {
                        rating = Double.NaN;
                        record = new Object[] { userName, songName, rating };
                    }
                    printerService.printRecord(record);
                }
            }

            if (printerService != null) {
                printerService.closeCSVFile();
            }
        } finally {
        }
        ;
    }
    // #endregion PA2
}
