import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

// extends PA 4
/**
 * 
 * SongUserServicePA5
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA5 extends SongUserServicePA4V2 {

    private ArrayList<Cs214Cluster> clusterList = new ArrayList<>();
    private String[] userSongNamesSelection;
    // private CSVPrinterService printerService = null;

    private void loadAndProcessClusterData(String inputFile) throws IOException { // PA4
        loadAndProcessDataPrediction(inputFile, false); // Call PA4 Load and process data to load csv, clean data,
                                                        // populate placeholder data, calculate user similarity, predict
                                                        // missing user song ratings, and populate missing ratings with
                                                        // predictions
        populateMissingRatingsWithPredictions();
    }

    public void loadAndProcessDataUserClustering(String inputFile, String[] userSongSelection) throws IOException { // PA5
        this.userSongNamesSelection = userSongSelection;
        validateDuplicateUserSelection();

        loadAndProcessClusterData(inputFile);

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

        initializeKCentroids(userSongSelection);
        iterateKMeansClustering(10);
        removeUserChoiceSongsFromRecommendation();

        emptyClusterAndUserChoiceValidationCheck();
        UserChoiceValidationCheck();
    }

    public void validateDuplicateUserSelection() {
        Set<String> tempSet = new TreeSet<>(Arrays.asList(this.userSongNamesSelection));
        if (tempSet.size() != this.userSongNamesSelection.length) {
            throw new IllegalArgumentException("User choice song selections must be unique.");
        }
    }

    public ArrayList<Cs214Cluster> getClusterList() {
        return clusterList;
    }

    public String[] getUserSongNamesSelection() {
        return userSongNamesSelection;
    }

    public void setUserSongNamesSelection(String[] userSongNamesSelection) {
        this.userSongNamesSelection = userSongNamesSelection;
    }

    private void populateMissingRatingsWithPredictions() {
        // Update user predicted ratings
        for (Map.Entry<String, Double> userPredictedEntry : this.userSongRatingPredictions.entrySet()) {
            // go through each predicted rating, update actual user rating with predicted
            Double predictedRating = userPredictedEntry.getValue();
            String[] keys = userPredictedEntry.getKey().split(",");
            this.userList.get(keys[1]).getSongList().put(keys[0], predictedRating);

            // this.songList.get(keys[0]).addPredictedRating(predictedRating); // WRONG, is
            // adding rating then predicted
        }
    }

    public void replaceNaNWithWeightedAverageRating() throws IOException {
        // update NaN ratings with weighted average ratings
        for (Map.Entry<String, Cs214User> aUserEntry : this.userList.entrySet()) {
            TreeMap<String, Double> userSongList = aUserEntry.getValue().getSongList();

            for (Map.Entry<String, Double> aSongEntry : userSongList.entrySet()) {
                if (aSongEntry.getValue().isNaN()) { // if predicted rating NaN, calculate weighted average
                    Double unpredicted = calculateWeightedAverage(aUserEntry.getValue(),
                            songList.get(aSongEntry.getKey()));

                    aSongEntry.setValue(unpredicted);
                }
            }
        }
    }

    public void populateSongListRatingsFromUserSongList() {
        for (Map.Entry<String, Cs214User> userEntry : this.userList.entrySet()) {
            TreeMap<String, Double> songList = userEntry.getValue().getSongList();
            for (Map.Entry<String, Double> songUserListEntry : songList.entrySet()) {
                ArrayList<Double> predictedRatings = this.songList.get(songUserListEntry.getKey())
                        .getSongEnrichedRatings();
                predictedRatings.add(songUserListEntry.getValue());
            }

        }
    }

    public Double calculateWeightedAverage(Cs214User user, Cs214Song song) {
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
        for (Map.Entry<String, Cs214Song> songListEntry : this.songList.entrySet()) {
            Set<Double> tempSet = new TreeSet<>(songListEntry.getValue().getSongEnrichedRatings());
            // for (double predRating : songListEntry.getValue().getEnrichedRatings()) {
            // tempSet.add(Math.round(predRating * 100.0) / 100.0);
            // }
            if (tempSet.size() == 1) {
                // this song has same rating across all users, remove from user songList
                for (Map.Entry<String, Cs214User> userListEntry : this.userList.entrySet()) {
                    userListEntry.getValue().getSongList().remove(songListEntry.getKey());
                    removeSongs.add(songListEntry.getKey());
                }
            }
        }
        for (String song : removeSongs) {
            songList.remove(song);
        }

        if (songList.size() < (this.userSongNamesSelection.length + 1)) {
            throw new IllegalArgumentException("Number of songs in the dataset is less than user choice size + 1");
        }
    }

    public void updateRunningMeanStandardDeviation() {
        for (Map.Entry<String, Cs214Song> songEntry : this.songList.entrySet()) {
            songEntry.getValue().calculateRunningMeanAndStandardDeviation(
                    new ArrayList<>(songEntry.getValue().getSongEnrichedRatings()));
        }
    }

    // Updating songList ArrayList<Double> normalizedRating with all song ratings
    // normalized rating
    private void normalizeRatingWithSongs() throws IOException {
        for (Map.Entry<String, Cs214Song> songEntry : this.songList.entrySet()) {
            ArrayList<Double> songPredictedRatings = songEntry.getValue().getSongEnrichedRatings();

            for (Double rating : songPredictedRatings) {
                Double normRating = songEntry.getValue().getSongNormalizedRating(rating);
                songEntry.getValue().addNormalizedRatings(normRating);
            }
        }
    }

    public void initializeKCentroids(String[] userSongSelection) {

        // userSelectionNotFound(userSongSelection);

        for (int i = 0; i < userSongSelection.length; i++) {
            Cs214Cluster cluster = new Cs214Cluster(userSongSelection[i]);
            clusterList.add(cluster);
            // cluster.getInitialCentroid().add(normRating);
            cluster.getCentroid().addAll(songList.get(userSongSelection[i]).getNormalizedRatings());

        }
    }

    // START KMEANS ALGORITHM
    public void iterateKMeansClustering(int numberIterations) {
        for (int index = 0; index < numberIterations; index++) {
            placeSongInCluster();
            // K-Means Clustering Algorithm (last bullet point)
            recalculateClusterCentroids();
        }
    }

    private void placeSongInCluster() {
        for (Cs214Cluster cluster : this.clusterList) {
            cluster.getSongs().clear();
        }

        for (Map.Entry<String, Cs214Song> songListEntry : this.songList.entrySet()) {
            ArrayList<Double> songNormRating = songListEntry.getValue().getNormalizedRatings();
            // need array for euclidean distance
            double[] songRatingArray = songNormRating.stream().mapToDouble(Double::doubleValue).toArray();
            Double minDistance = Double.POSITIVE_INFINITY;
            int clusterIndex = 0;
            int minClusterIndex = 0;

            minClusterIndex = findMinClusterPlacement(songRatingArray, minDistance, clusterIndex, minClusterIndex);

            clusterList.get(minClusterIndex).getSongs().add(songListEntry.getKey());
        }
    }

    private int findMinClusterPlacement(double[] songRatingArray, Double minDistance, int clusterIndex,
            int minClusterIndex) {

        for (Cs214Cluster cluster : this.clusterList) {
            ArrayList<Double> clusterCentroid = cluster.getCentroid();
            double[] clusterCentroidArray = clusterCentroid.stream().mapToDouble(Double::doubleValue).toArray();

            Double euclidDistance = new EuclideanDistance().compute(songRatingArray, clusterCentroidArray);
            if (euclidDistance < minDistance) {
                minDistance = euclidDistance;
                minClusterIndex = clusterIndex;
            }
            clusterIndex++; // keeps track of which cluster
        }
        
        return minClusterIndex;
    }

    private void recalculateClusterCentroids() {
        for (Cs214Cluster cluster : this.clusterList) {
            ArrayList<double[]> allSongsNormalizedRatingsAsArrays = new ArrayList<>();
            ArrayList<String> currentSongList = cluster.getSongs();

            // go through songlist of the cluster and get the normalized ratings for each
            // song add them up and divide
            if (currentSongList.size() > 0) {
                double[] sum = calculateSumOfRatings(allSongsNormalizedRatingsAsArrays, currentSongList);

                // https://stackoverflow.com/questions/5178854/convert-a-double-array-to-double-arraylist
                // convert array into ArrayList
                ArrayList<Double> list = DoubleStream.of(sum)
                        .boxed()
                        .collect(Collectors.toCollection(ArrayList::new));

                cluster.setCentroid(list);
            }
        }
    }
    // END KMEANS ALGORITHM

    public void userSelectionNotFound() {
        for (String userChoice : this.userSongNamesSelection) {
            if (!this.songList.containsKey(userChoice)) {
                throw new IllegalArgumentException("userChoice song must exist in input file.");
            }
        }
    }

    private double[] calculateSumOfRatings(ArrayList<double[]> allSongsNormalizedRatingsAsArrays,
            ArrayList<String> currentSongList) {
        for (String song : currentSongList) {
            ArrayList<Double> normalizedRatings = this.songList.get(song).getNormalizedRatings();
            allSongsNormalizedRatingsAsArrays
                    .add(normalizedRatings.stream().mapToDouble(Double::doubleValue).toArray());
        }

        double[] sum = new double[allSongsNormalizedRatingsAsArrays.get(0).length];

        divideRatings(allSongsNormalizedRatingsAsArrays, currentSongList, sum);
        return sum;
    }

    private void divideRatings(ArrayList<double[]> allSongsNormalizedRatingsAsArrays, ArrayList<String> currentSongList,
            double[] sum) {
        int songCount = currentSongList.size();
        int length = allSongsNormalizedRatingsAsArrays.get(0).length;
        for (int i = 0; i < length; i++) {
            // goes through each array in allSongsNormRatings, adds each index
            for (double[] songRatings : allSongsNormalizedRatingsAsArrays) {
                sum[i] += songRatings[i];
            }
            sum[i] = sum[i] / songCount;
        }
    }

    public void emptyClusterAndUserChoiceValidationCheck() {
        boolean allClustersAreEmpty = true;
        boolean allClustersContainOnlyUserChoice = true;
        for (Cs214Cluster cluster : this.clusterList) {
            if (!cluster.getSongs().isEmpty()) {
                allClustersAreEmpty = false;
                allClustersContainOnlyUserChoice = validateAllSongsUserChoice(allClustersContainOnlyUserChoice,
                        cluster);
            }
            if (!allClustersContainOnlyUserChoice) {
                break;
            }
        }

        errorMessages(allClustersAreEmpty, allClustersContainOnlyUserChoice);
    }

    private boolean validateAllSongsUserChoice(boolean allClustersContainOnlyUserChoice, Cs214Cluster cluster) {
        for (String songName : cluster.getSongs()) {
            if (!Arrays.asList(this.userSongNamesSelection).contains(songName)) {
                allClustersContainOnlyUserChoice = false;
                break;
            }
        }
        return allClustersContainOnlyUserChoice;
    }

    private void errorMessages(boolean allClustersAreEmpty, boolean allClustersContainOnlyUserChoice) {
        if (allClustersAreEmpty) {
            throw new IllegalArgumentException("All clusters are empty.");
        }

        if (allClustersContainOnlyUserChoice) {
            throw new IllegalArgumentException("All clusters only contain user selections.");
        }
    }

    public void UserChoiceValidationCheck() {
        for (Cs214Cluster cluster : this.clusterList) {
            boolean clustersContainOnlyUserChoice = true;
            clustersContainOnlyUserChoice = validateAllSongsUserChoice(clustersContainOnlyUserChoice, cluster);
            if (clustersContainOnlyUserChoice) {
                cluster.getSongs().clear();
            }
        }
    }

    public void removeUserChoiceSongsFromRecommendation() {
        for (Cs214Cluster cluster : this.clusterList) {
            ArrayList<String> songs = cluster.getSongs();
            String clusterSong = cluster.getClusterUserChoiceSongName();
            if (songs.contains(clusterSong)) {
                songs.remove(clusterSong);
            }
        }
    }

    public void outputUserChoiceRecommendations(String outputFile) throws IOException {
        validateArguments(outputFile);
        CSVPrinterService printerService = null;

        try {
            String[] headers = new String[] { "user choice", "recommendation" };

            printerService = new CSVPrinterService(outputFile, headers);

            Collections.sort(clusterList); // sort clusters by user song choice

            for (Cs214Cluster cluster : this.clusterList) {
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
