import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

// extends PA 
/**
 * 
 * SongUserServicePA4
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA4V2 extends SongUserServicePA3V2 {

    //private TreeMap<String, Cs214User> userList = new TreeMap<>();
    private TreeMap<Cs214User, TreeMap<Double, Cs214User>> similarUserList = new TreeMap<>();
    private Double[][] userSimilarityMatrix = new Double[userList.size()][userList.size()];
    public TreeMap<String, Double> userSongRatingPredictions = new TreeMap<>();

    private CSVPrinterService printerService = null;

    public void loadAndProcessDataPrediction(String inputFile, boolean dontAllowEmptyUserSongPredictionRatings) throws IOException {
        loadAndProcessData(inputFile); //Call PA3 Load and process data to load csv, clean data, populate placeholder data, and calculate song similarity
        calcUserSimilarity();
        predictMissingUserSongRating(dontAllowEmptyUserSongPredictionRatings);
    }

    public List<String> getMasterSongNameList() {
        return masterSongNameList;
    }

    public Double[][] getUserSimilarityMatrix() {
        return userSimilarityMatrix;
    }

    /**
     * Calculates similarity between two users
     * 
     * @return Matrix of similarity for first user and second user.
     */
    public void calcUserSimilarity() {
        validateCooperativeUsers();

        int numUsers = this.userList.size();
        this.userSimilarityMatrix = new Double[numUsers][numUsers];

        // https://www.tutorialspoint.com/article/how-to-get-treemap-key-or-value-using-index-in-java

        initilizeSimilarUserListKey();

        int firstUserIndex = 0;
        for (Map.Entry<String, Cs214User> userEntryRow : this.userList.entrySet()) {
            int secondUserIndex = 0;
            for (Map.Entry<String, Cs214User> userEntryColumn : this.userList.entrySet()) {
                double calcSongPair = calcSongPairDistance(userEntryRow.getValue(), userEntryColumn.getValue());
                userSimilarityMatrix[firstUserIndex][secondUserIndex] = calcSongPair;
                // userSimilarityMatrix[secondUserIndex][firstUserIndex] = calcSongPair;

                populateUserSimilarityMap(userEntryRow.getValue(), userEntryColumn.getValue(), calcSongPair);

                secondUserIndex++;
            }
            
            firstUserIndex++;
        }
    }

    public void initilizeSimilarUserListKey() {
        this.similarUserList = new TreeMap<>();
        for (Map.Entry<String, Cs214User> userEntry : userList.entrySet()) { // populate similarUserList treemap for all
                                                                             // users
            this.similarUserList.put(userEntry.getValue(), new TreeMap<Double, Cs214User>());
        }
    }

    private void populateUserSimilarityMap(Cs214User userEntryRow, Cs214User userEntryColumn, Double similarityValue) {
        if (userEntryRow != userEntryColumn && !similarityValue.isNaN()) {
            // can use key to get user
            TreeMap<Double, Cs214User> user1NestedTreeMap = similarUserList.get(userEntryRow); // get user key from the
                                                                                               // treemap
            user1NestedTreeMap.put(similarityValue, userEntryColumn);
        }
    }

    /**
     * Distance between user firstUserIndex and user secondUserIndex across all
     * songs they both rated (NaN if no both users didn't rate the
     * songlist.entrySet())
     * 
     * @param firstUser
     * @param secondUser
     * @return Euclidean distance between songlist.entrySet() of each user.
     */
    private double calcSongPairDistance(Cs214User firstUser, Cs214User secondUser) {

        if (firstUser == secondUser) {
            return 0.0;
        }

        double[] song1Ratings = new double[masterSongNameList.size()];
        double[] song2Ratings = new double[masterSongNameList.size()];
        boolean usersHaveSharedSong = false;

        TreeMap<String, Double> secondUserSongList = secondUser.getSongList();
        int index = 0;

        for (Map.Entry<String, Double> songNameRatingEntry : firstUser.getSongList().entrySet()) {
            Double song1 = firstUser.getSongNormalizedRating(songNameRatingEntry.getValue());
            Double song2 = secondUser.getSongNormalizedRating(secondUserSongList.get(songNameRatingEntry.getKey()));

            if (!Double.isFinite(song1) || !Double.isFinite(song2)) { // isFinite returns true if its not infinity or
                                                                      // NaN
                song1Ratings[index] = 0.0;
                song2Ratings[index] = 0.0;
            } else {
                song1Ratings[index] = song1;
                song2Ratings[index] = song2;
                usersHaveSharedSong = true;
            }
            index++;
        }

        if (!usersHaveSharedSong) {
            return Double.NaN;
        }

        return new EuclideanDistance().compute(song1Ratings, song2Ratings);
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

        if (this.userList == null) {
            throw new IllegalArgumentException("songs parameter required.");
        }
    }

    /**
     * Predict a target user's empty rating based on the most similar users rating
     */
    public void predictMissingUserSongRating(boolean dontAllowEmptyUserSongPredictionRatings) {
        this.userSongRatingPredictions = new TreeMap<>();
        // targetUser: User that is missing rating data.
        // otherUser: user that is the closest in similarity to targetUser.

        // Loop through all users as targets
        for (Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry : this.similarUserList.entrySet()) {
            TreeMap<String, Double> songListUser1 = userEntry.getKey().getSongList();

            predictTargetUserSongRatings(userEntry, songListUser1);

        }
        if (dontAllowEmptyUserSongPredictionRatings && userSongRatingPredictions.size() == 0) {
             throw new IllegalArgumentException("All users rated all songs. No predictions needed.");
        }
    }

    private void predictTargetUserSongRatings(Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry,
            TreeMap<String, Double> songListUser1) {
        // Loop through target users songs
        for (Map.Entry<String, Double> songEntry : songListUser1.entrySet()) {
            if (Double.isNaN(songEntry.getValue())) {
                String key = songEntry.getKey() + "," + userEntry.getKey().getUserName();

                double predictedRating = calculatePredictedRating(userEntry, songEntry);

                this.userSongRatingPredictions.put(key, predictedRating);
            }
        }
    }

    private double calculatePredictedRating(Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry,
            Map.Entry<String, Double> songEntry) {
        double predictedRating = Double.NaN;

        // Loop through target user similar user list
        for (Map.Entry<Double, Cs214User> similarUserEntry : userEntry.getValue().entrySet()) {
            // get most similar user (other user) and check to see if they rated
            // targetuser's song
            TreeMap<String, Double> songListUser2 = similarUserEntry.getValue().getSongList();
            Double user2SongRating = songListUser2.get(songEntry.getKey());

            if (!user2SongRating.isNaN()) { // checks if valid rating
                double otherUserNormalizedRating = similarUserEntry.getValue().getSongNormalizedRating(user2SongRating);

                // de-normalizing predicted rating
                predictedRating = otherUserNormalizedRating * userEntry.getKey().getStandardDeviation()
                        + userEntry.getKey().getRunningMean();

                predictedRating = Math.round(predictedRating);

                break;
            }
        }

        if (!Double.isNaN(predictedRating)) {
            predictedRating = Math.max(1.0, Math.min(5.0, predictedRating));
        }

        return predictedRating;
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

            printerService = new CSVPrinterService(outputFile, headers);

            List<Map.Entry<String, Cs214User>> usersArray = new ArrayList<>(this.userList.entrySet());

            for (int rowIndex = 0; rowIndex < userSimilarityMatrix.length; rowIndex++) {
                printDataRow(usersArray, rowIndex);
            }
        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }

    public void printDataRow(List<Map.Entry<String, Cs214User>> usersArray, int rowIndex) throws IOException {
        for (int colIndex = rowIndex + 1; colIndex < userSimilarityMatrix.length; colIndex++) {
            Double similariyValue = this.userSimilarityMatrix[rowIndex][colIndex];

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

        CSVPrinterService printerService = null;

        try {
            String[] headers = new String[] { "song", "user", "predicted rating" };

            printerService = new CSVPrinterService(outputFile, headers);

            for (Map.Entry<String, Double> predictionEntry : this.userSongRatingPredictions.entrySet()) {
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
