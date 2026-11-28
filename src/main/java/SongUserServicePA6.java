import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SongUserServicePA6 {
    service_UserSongCSVDataLoad fileService;
    service_SongSimilarity songSimilarityService;
    service_UserSimilarity userSimilarityService;
    service_UserPrediction userPredictionService;
    service_Clustering clusteringService;

    private String[] userSongNamesSelection;
    private int kNumberOfCentroids;

    private TreeMap<String, Double> candidateSongsWithBestDistance; // key = song name, value = best euclidean distance
    ArrayList<Map.Entry<String, Double>> sortByDistanceList;

    private void loadAndProcessDataPrediction(String inputFile, boolean dontAllowEmptyUserSongPredictionRatings)
            throws IOException {
        fileService = new service_UserSongCSVDataLoad();
        songSimilarityService = new service_SongSimilarity();
        userSimilarityService = new service_UserSimilarity();
        userPredictionService = new service_UserPrediction();
        

        fileService.loadAndCleanAndProcessFileData(inputFile);

        userSimilarityService.calcUserSimilarity(fileService.getUserList(), fileService.getMasterSongNameList());

        userPredictionService.predictMissingUserSongRating(dontAllowEmptyUserSongPredictionRatings,
                userSimilarityService.getSimilarUserList());
        userPredictionService.populateMissingRatingsWithPredictions(fileService.getUserList());
    }

    public void loadAndProcessDataUserClustering(String inputFile, String[] userSongSelection, int kNumOfCentorids)
            throws IOException { // PA5
        this.userSongNamesSelection = userSongSelection;
        this.kNumberOfCentroids = kNumOfCentorids;
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
        clusteringService.proccessingClustering(userSongSelection, kNumberOfCentroids, false);
        findSimilarSongsToUserSelectionSongs();
    }


    public void validateDuplicateUserSelection() {
        Set<String> tempSet = new TreeSet<>(Arrays.asList(this.userSongNamesSelection));
        if (tempSet.size() != this.userSongNamesSelection.length) {
            throw new IllegalArgumentException("User choice song selections must be unique.");
        }
    }

    public void initializeSongsForDistances() {
        candidateSongsWithBestDistance = new TreeMap<>();
        for (String song : fileService.getMasterSongNameList()) {
            candidateSongsWithBestDistance.put(song, 0.0);
        }
    }

    public void findSimilarSongsToUserSelectionSongs() {
        initializeSongsForDistances();
        for (String userLikedSong : this.userSongNamesSelection) {
            data_Cluster cluster = findClusterForSong(userLikedSong);

            for (String clusterSong : cluster.getSongs()) {
                if (!clusterSong.equals(userLikedSong)) {
                    data_Song clusterSongObject = fileService.getSongList().get(clusterSong);
                    data_Song userLikedSongObject = fileService.getSongList().get(userLikedSong);

                    double distance = 1.0 / songSimilarityService.calcEuclideanDistance(
                            clusterSongObject.getNormalizedRatings(),
                            userLikedSongObject.getNormalizedRatings());

                    if (distance > candidateSongsWithBestDistance.get(clusterSong)) {
                        candidateSongsWithBestDistance.replace(clusterSong, distance);
                    }
                }
            }
        }
        List<String> songsToRemove = Arrays.asList(this.userSongNamesSelection);
        candidateSongsWithBestDistance.keySet().removeAll(songsToRemove);

        candidateSongsWithBestDistance.values().removeIf(value-> value == 0.0);

        populateBestSongDistances();
    }


    private void populateBestSongDistances() {
        //https://sentry.io/answers/how-to-sort-a-map-key-value-by-values-in-java/
        
        sortByDistanceList = new ArrayList<>(candidateSongsWithBestDistance.entrySet());
        sortByDistanceList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }

    private data_Cluster findClusterForSong(String userLikedSong) throws NoSuchElementException{
        for (data_Cluster cluster : clusteringService.getClusterList()) {
            if (cluster.getSongs().contains(userLikedSong)) {
                return cluster;
            }
        }
        throw new NoSuchElementException("No cluster contains song \"" + userLikedSong + "\"");
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
            printerService = new service_CSVPrinter(outputFile);

            for (Map.Entry<String,Double> song: this.sortByDistanceList) {
                    Object[] values = {song.getKey()};
                    printerService.printRecord(values);
            }
        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }
}