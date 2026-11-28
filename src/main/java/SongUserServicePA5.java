import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * SongUserServicePA5
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA5 {
    service_UserSongCSVDataLoad fileService;
    service_UserSimilarity userSimilarityService;
    service_UserPrediction userPredictionService;
    service_Clustering clusteringService;

    private String[] userSongNamesSelection;

    private void loadAndProcessDataPrediction(String inputFile, boolean dontAllowEmptyUserSongPredictionRatings)
            throws IOException {
        fileService = new service_UserSongCSVDataLoad();
        userSimilarityService = new service_UserSimilarity();
        userPredictionService = new service_UserPrediction();

        fileService.loadAndCleanAndProcessFileData(inputFile);

        userSimilarityService.calcUserSimilarity(fileService.getUserList(), fileService.getMasterSongNameList());

        userPredictionService.predictMissingUserSongRating(dontAllowEmptyUserSongPredictionRatings,
                userSimilarityService.getSimilarUserList());
        userPredictionService.populateMissingRatingsWithPredictions(fileService.getUserList());
    }

    public void loadAndProcessDataUserClustering(String inputFile, String[] userSongSelection) throws IOException { // PA5
        this.userSongNamesSelection = userSongSelection;
        validateDuplicateUserSelection();

        loadAndProcessDataPrediction(inputFile, false);

        fileService.assertLoadedSongListContainsSongs(this.userSongNamesSelection);
        fileService.replaceNaNWithWeightedAverageRating();

        // Up until this point, I have only been working with users and song lists in
        // the userList tree map
        // Now move all data from userList.songList treemap into the
        // songList.songEnrichedRatings ArrayList
        fileService.populateSongListRatingsFromUserSongList();

        fileService.removeOneDistinctRating();
        fileService.assertLoadedSongListContainsEnoughSongs(this.userSongNamesSelection);
        fileService.updateRunningMeanStandardDeviation();
        fileService.normalizeRatingWithSongs();

        clusteringService = new service_Clustering(fileService.getSongList());
        clusteringService.proccessingClustering(userSongSelection, true);
    }

    public void validateDuplicateUserSelection() {
        Set<String> tempSet = new TreeSet<>(Arrays.asList(this.userSongNamesSelection));
        if (tempSet.size() != this.userSongNamesSelection.length) {
            throw new IllegalArgumentException("User choice song selections must be unique.");
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