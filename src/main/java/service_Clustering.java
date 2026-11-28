import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

public class service_Clustering {
    private ArrayList<data_Cluster> clusterList = new ArrayList<>();
    private TreeMap<String, data_Song> songList;

    public service_Clustering(TreeMap<String, data_Song> songList) {
        this.songList = songList;
    }

    public ArrayList<data_Cluster> getClusterList() {
        return clusterList;
    }

    public void initializeKCentroids(String[] userSongSelection) {
        clusterList.clear();

        // userSelectionNotFound(userSongSelection);

        for (int i = 0; i < userSongSelection.length; i++) {
            data_Cluster cluster = new data_Cluster(userSongSelection[i]);
            clusterList.add(cluster);
            cluster.getCentroid().clear();
            cluster.getCentroid().addAll(this.songList.get(userSongSelection[i]).getNormalizedRatings());
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
        for (data_Cluster cluster : this.clusterList) {
            cluster.getSongs().clear();
        }

        for (Map.Entry<String, data_Song> songListEntry : this.songList.entrySet()) {
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

        for (data_Cluster cluster : this.clusterList) {
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
        for (data_Cluster cluster : this.clusterList) {
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

    public void emptyClusterAndUserChoiceValidationCheck(String[] userSongSelection) {
        boolean allClustersAreEmpty = true;
        boolean allClustersContainOnlyUserChoice = true;
        for (data_Cluster cluster : this.clusterList) {
            if (!cluster.getSongs().isEmpty()) {
                allClustersAreEmpty = false;
                allClustersContainOnlyUserChoice = validateAllSongsUserChoice(allClustersContainOnlyUserChoice,
                        cluster, userSongSelection);
            }
            if (!allClustersContainOnlyUserChoice) {
                break;
            }
        }

        errorMessages(allClustersAreEmpty, allClustersContainOnlyUserChoice);
    }

    public void UserChoiceValidationCheck(String[] userSongSelection) {
        for (data_Cluster cluster : this.clusterList) {
            boolean clustersContainOnlyUserChoice = true;
            clustersContainOnlyUserChoice = validateAllSongsUserChoice(clustersContainOnlyUserChoice, cluster,
                    userSongSelection);
            if (clustersContainOnlyUserChoice) {
                cluster.getSongs().clear();
            }
        }
    }

    private boolean validateAllSongsUserChoice(boolean allClustersContainOnlyUserChoice, data_Cluster cluster,
            String[] userSongNamesSelection) {
        List<String> songNameSelection = Arrays.asList(userSongNamesSelection);
        for (String songName : cluster.getSongs()) {
            if (!songNameSelection.contains(songName)) {
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

    public void removeUserChoiceSongsFromRecommendation() {
        for (data_Cluster cluster : this.clusterList) {
            ArrayList<String> songs = cluster.getSongs();
            String clusterSong = cluster.getClusterUserChoiceSongName();
            if (songs.contains(clusterSong)) {
                songs.remove(clusterSong);
            }
        }
    }

    public void proccessingClustering(String[] userSongNamesSelection, int kNumberOfCentroids,
            boolean removeUserChoiceSongsFromRecommendation) {
        String[] initialKCentroidSongs = new String[kNumberOfCentroids];
        System.arraycopy(userSongNamesSelection, 0, initialKCentroidSongs, 0, kNumberOfCentroids);
        // k-means cluster methods
        initializeKCentroids(initialKCentroidSongs);
        iterateKMeansClustering(10);
        if (removeUserChoiceSongsFromRecommendation) {
            removeUserChoiceSongsFromRecommendation();
        }

        emptyClusterAndUserChoiceValidationCheck(userSongNamesSelection);
        UserChoiceValidationCheck(userSongNamesSelection);
    }

    public void proccessingClustering(String[] userSongNamesSelection, boolean removeUserChoiceSongsFromRecommendation) {
            proccessingClustering(userSongNamesSelection, userSongNamesSelection.length, removeUserChoiceSongsFromRecommendation);
    }
}
