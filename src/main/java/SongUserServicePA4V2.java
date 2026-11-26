import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// extends PA 
/**
 * 
 * SongUserServicePA4
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA4V2 {

    service_UserSongCSVDataLoad fileService;

    service_UserSimilarity userSimilarityService;
    service_UserPrediction userPredictionService;

    private service_CSVPrinter printerService = null;

    public void loadAndProcessDataPrediction(String inputFile, boolean dontAllowEmptyUserSongPredictionRatings)
            throws IOException {
        fileService = new service_UserSongCSVDataLoad();

        userPredictionService = new service_UserPrediction();

        userSimilarityService = new service_UserSimilarity();

        fileService.loadAndCleanAndProcessFileData(inputFile); // Call PA3 Load and process data to load csv, clean
                                                               // data,
        // populate
        // placeholder data

        userSimilarityService.calcUserSimilarity(fileService.getUserList(), fileService.getMasterSongNameList());

        userPredictionService.predictMissingUserSongRating(dontAllowEmptyUserSongPredictionRatings,
                userSimilarityService.getSimilarUserList());
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
     * Outputs user1 name, user2 name, and similarity between users.
     * 
     * @param outputFile Output CSV file path.
     * @throws IOException If printer service fail.
     */
    public void outputUsersSongsRatingsCSV(String outputFile) throws IOException {
        validateArguments(outputFile);

        try {
            String[] headers = new String[] { "name1", "name2", "similarity" };

            printerService = new service_CSVPrinter(outputFile, headers);

            List<Map.Entry<String, data_User>> usersArray = new ArrayList<>(fileService.getUserList().entrySet());

            for (int rowIndex = 0; rowIndex < userSimilarityService.getUserSimilarityMatrix().length; rowIndex++) {
                printDataRow(usersArray, rowIndex);
            }
        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }

    public void printDataRow(List<Map.Entry<String, data_User>> usersArray, int rowIndex) throws IOException {
        for (int colIndex = rowIndex + 1; colIndex < userSimilarityService
                .getUserSimilarityMatrix().length; colIndex++) {
            Double similariyValue = this.userSimilarityService.getUserSimilarityMatrix()[rowIndex][colIndex];

            if (similariyValue != null && rowIndex != colIndex
                    && (similariyValue >= 0.0 || similariyValue.equals(Double.NaN))) {
                Object[] values = { usersArray.get(rowIndex).getKey(), usersArray.get(colIndex).getKey(),
                        similariyValue };
                printerService.printRecord(values);
            }
        }
    }

    public void outputUsersSongsRatingPredictionsCSV(String outputFile) throws IOException {
        validateArguments(outputFile);

        service_CSVPrinter printerService = null;

        try {
            String[] headers = new String[] { "song", "user", "predicted rating" };

            printerService = new service_CSVPrinter(outputFile, headers);

            for (Map.Entry<String, Double> predictionEntry : userPredictionService.userSongRatingPredictions
                    .entrySet()) {
                String[] keys = predictionEntry.getKey().split(",");
                String valuePredictionEntry = null;

                if (predictionEntry.getValue().isNaN()) {
                    valuePredictionEntry = predictionEntry.getValue().toString();
                } else {
                    valuePredictionEntry = Integer.toString(predictionEntry.getValue().intValue());
                }

                Object[] values = { keys[0], keys[1], valuePredictionEntry };
                printerService.printRecord(values);
            }

        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }
}
