import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * SongUserServicePA5
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA5 {
    service_UserSongCSVDataLoad fileService;
    service_SongSimilarity songSimilarityService;
    service_UserSimilarity userSimilarityService;
    service_UserPrediction userPredictionService;
    service_Clustering clusteringService;

    private String[] userSongNamesSelection;
    // private CSVPrinterService printerService = null;

    private void loadAndProcessDataPrediction(String inputFile, boolean dontAllowEmptyUserSongPredictionRatings)
            throws IOException {
        fileService = new service_UserSongCSVDataLoad();
        songSimilarityService = new service_SongSimilarity();
        userSimilarityService = new service_UserSimilarity();
        userPredictionService = new service_UserPrediction();

        fileService.loadAndCleanAndProcessFileData(inputFile); // Call PA3 Load and process data to load csv, clean
                                                               // data,
        // populate
        // placeholder data, and calculate song similarity

        userSimilarityService.calcUserSimilarity(fileService.getUserList(), fileService.getMasterSongNameList());

        userPredictionService.predictMissingUserSongRating(dontAllowEmptyUserSongPredictionRatings,
                userSimilarityService.getSimilarUserList());
        populateMissingRatingsWithPredictions();
    }

    public void loadAndProcessDataUserClustering(String inputFile, String[] userSongSelection) throws IOException { // PA5
        this.userSongNamesSelection = userSongSelection;
        validateDuplicateUserSelection();

        loadAndProcessDataPrediction(inputFile, false);

        userSelectionNotFound();
        replaceNaNWithWeightedAverageRating();

        // Up until this point, I have only been working with users and song lists in
        // the userList tree map
        // Now move all data from userList.songList treemap into the
        // songList.songEnrichedRatings ArrayList
        populateSongListRatingsFromUserSongList();

        removeOneDistinctRating();
        updateRunningMeanStandardDeviation();
        normalizeRatingWithSongs();

        // k-means cluster methods
        clusteringService = new service_Clustering(fileService.getSongList());
        clusteringService.initializeKCentroids(userSongSelection);
        clusteringService.iterateKMeansClustering(10);
        clusteringService.removeUserChoiceSongsFromRecommendation();

        clusteringService.emptyClusterAndUserChoiceValidationCheck(this.userSongNamesSelection);
        clusteringService.UserChoiceValidationCheck(this.userSongNamesSelection);
    }

    public void validateDuplicateUserSelection() {
        Set<String> tempSet = new TreeSet<>(Arrays.asList(this.userSongNamesSelection));
        if (tempSet.size() != this.userSongNamesSelection.length) {
            throw new IllegalArgumentException("User choice song selections must be unique.");
        }
    }

    public String[] getUserSongNamesSelection() {
        return userSongNamesSelection;
    }

    public void setUserSongNamesSelection(String[] userSongNamesSelection) {
        this.userSongNamesSelection = userSongNamesSelection;
    }

    private void populateMissingRatingsWithPredictions() {
        // Update user predicted ratings
        for (Map.Entry<String, Double> userPredictedEntry : userPredictionService.getUserSongRatingPredictions()
                .entrySet()) {
            // go through each predicted rating, update actual user rating with predicted
            Double predictedRating = userPredictedEntry.getValue();
            String[] keys = userPredictedEntry.getKey().split(",");
            fileService.getUserList().get(keys[1]).getSongList().put(keys[0], predictedRating);

            // this.songList.get(keys[0]).addPredictedRating(predictedRating); // WRONG, is
            // adding rating then predicted
        }
    }

    public void replaceNaNWithWeightedAverageRating() throws IOException {
        // update NaN ratings with weighted average ratings
        for (Map.Entry<String, data_User> aUserEntry : fileService.getUserList().entrySet()) {
            TreeMap<String, Double> userSongList = aUserEntry.getValue().getSongList();

            for (Map.Entry<String, Double> aSongEntry : userSongList.entrySet()) {
                if (aSongEntry.getValue().isNaN()) { // if predicted rating NaN, calculate weighted average
                    Double unpredicted = calculateWeightedAverage(aUserEntry.getValue(),
                            fileService.getSongList().get(aSongEntry.getKey()));

                    aSongEntry.setValue(unpredicted);
                }
            }
        }
    }

    public void populateSongListRatingsFromUserSongList() {
        for (Map.Entry<String, data_User> userEntry : fileService.getUserList().entrySet()) {
            TreeMap<String, Double> songList = userEntry.getValue().getSongList();
            for (Map.Entry<String, Double> songUserListEntry : songList.entrySet()) {
                ArrayList<Double> predictedRatings = fileService.getSongList().get(songUserListEntry.getKey())
                        .getSongEnrichedRatings();
                predictedRatings.add(songUserListEntry.getValue());
            }

        }
    }

    public Double calculateWeightedAverage(data_User user, data_Song song) {
        double songMean = song.getRunningMean();
        double userMean = user.getRunningMean();
        int numSongRatings = song.getRatingsCount();
        int numUserRatings = user.getSongRatingCount();

        double unpredicted = Math
                .round((songMean * numSongRatings + userMean * numUserRatings) / (numSongRatings + numUserRatings));
        return unpredicted;
    }

    public void removeOneDistinctRating() {
        ArrayList<String> removeSongs = new ArrayList<>();
        for (Map.Entry<String, data_Song> songListEntry : fileService.getSongList().entrySet()) {
            Set<Double> tempSet = new TreeSet<>(songListEntry.getValue().getSongEnrichedRatings());
            // for (double predRating : songListEntry.getValue().getEnrichedRatings()) {
            // tempSet.add(Math.round(predRating * 100.0) / 100.0);
            // }
            if (tempSet.size() == 1) {
                // this song has same rating across all users, remove from user songList
                for (Map.Entry<String, data_User> userListEntry : fileService.getUserList().entrySet()) {
                    userListEntry.getValue().getSongList().remove(songListEntry.getKey());
                    removeSongs.add(songListEntry.getKey());
                }
            }
        }
        for (String song : removeSongs) {
            fileService.getSongList().remove(song);
        }

        if (fileService.getSongList().size() < (this.userSongNamesSelection.length + 1)) {
            throw new IllegalArgumentException("Number of songs in the dataset is less than user choice size + 1");
        }
    }

    public void updateRunningMeanStandardDeviation() {
        for (Map.Entry<String, data_Song> songEntry : fileService.getSongList().entrySet()) {
            songEntry.getValue().calculateRunningMeanAndStandardDeviation(
                    new ArrayList<>(songEntry.getValue().getSongEnrichedRatings()));
        }
    }

    // Updating songList ArrayList<Double> normalizedRating with all song ratings
    // normalized rating
    private void normalizeRatingWithSongs() throws IOException {
        for (Map.Entry<String, data_Song> songEntry : fileService.getSongList().entrySet()) {
            data_Song song = songEntry.getValue();
            song.getNormalizedRatings().clear();

            for (Map.Entry<String, data_User> userEntry : fileService.getUserList().entrySet()) {
                Double rating = userEntry.getValue().getSongList().get(songEntry.getKey());
                if (rating == null || rating.isNaN()) {
                    rating = 0.0;
                }
                song.addNormalizedRatings(song.getSongNormalizedRating(rating));
            }
        }
    }

    public void userSelectionNotFound() {
        for (String userChoice : this.userSongNamesSelection) {
            if (!fileService.getSongList().containsKey(userChoice)) {
                throw new IllegalArgumentException("userChoice song must exist in input file.");
            }
        }
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

    public void outputUserChoiceRecommendations(String outputFile) throws IOException {
        validateArguments(outputFile);
        service_CSVPrinter printerService = null;

        try {
            String[] headers = new String[] { "user choice", "recommendation" };

            printerService = new service_CSVPrinter(outputFile, headers);

            Collections.sort(clusteringService.getClusterList()); // sort clusters by user song choice

            for (data_Cluster cluster : this.clusteringService.getClusterList()) {
                for (int songIndex = 0; songIndex < cluster.getSongs().size(); songIndex++) {
                    Object[] values = { cluster.getClusterUserChoiceSongName(), cluster.getSongs().get(songIndex) };
                    printerService.printRecord(values);
                }

            }
        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }
}