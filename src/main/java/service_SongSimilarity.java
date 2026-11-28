import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

public class service_SongSimilarity {
    private Double[][] songSimilarityMatrix;

    public Double[][] getSongSimilarityMatrix() {
        return songSimilarityMatrix;
    }

    /**
     * Caclulates similarity of two songs for each user.
     * 
     * @return matrix of song similarity.
     * @throws IOException if validate cooperative users fail.
     */
    public Double[][] calcSongSimilarity(TreeMap<String, data_User> userList, List<String> masterSongNameList)
            throws IOException {
        int numSongs = masterSongNameList.size();
        this.songSimilarityMatrix = new Double[numSongs][numSongs];

        for (int firstSongIndex = 0; firstSongIndex < numSongs; firstSongIndex++) {
            for (int secondSongIndex = firstSongIndex + 1; secondSongIndex < numSongs; secondSongIndex++) {
                songSimilarityMatrix[firstSongIndex][secondSongIndex] = calcSongPairDistance(firstSongIndex,
                        secondSongIndex, userList);
            }
        }
        return songSimilarityMatrix;
    }

    /**
     * Calculates euclidean distance for each song.
     * 
     * @param firstSongIndex
     * @param secondSongIndex
     * @return Euclidean distance.
     */
    // Distance between song i and song s across all users (NaN if no user rated
    // both)
    private double calcSongPairDistance(int firstSongIndex, int secondSongIndex, TreeMap<String, data_User> userList) {
        double[] song1NormalizedRating = new double[userList.size()];
        double[] song2NormalizedRating = new double[userList.size()];
        boolean songsHaveSharedUser = false;
        int userIndex = 0; // index tracker for the for-each loop

        for (Map.Entry<String, data_User> userEntry : userList.entrySet()) {
            double[] normalizedRatingPair = fillUserRatings(userEntry, firstSongIndex, secondSongIndex);
            if (normalizedRatingPair != null) {
                song1NormalizedRating[userIndex] = normalizedRatingPair[0];
                song2NormalizedRating[userIndex] = normalizedRatingPair[1];
                songsHaveSharedUser = true;
            }
            userIndex++;
        }

        if (!songsHaveSharedUser) {
            return Double.NaN;
        }
        return calcEuclideanDistance(song1NormalizedRating, song2NormalizedRating);
    }

    public double calcEuclideanDistance(double[] song1NormalizedRating, double[] song2NormalizedRating) {
        return new EuclideanDistance().compute(song1NormalizedRating, song2NormalizedRating);
    }

    public double calcEuclideanDistance(ArrayList<Double> song1NormalizedRating, ArrayList<Double> song2NormalizedRating) {
        double[] song1NormalizedRatingArray = song1NormalizedRating.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        double[] song2NormalizedRatingArray = song2NormalizedRating.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        return calcEuclideanDistance(song1NormalizedRatingArray, song2NormalizedRatingArray);
    }

    /**
     * Gets one user's normalized ratings for two songs.
     * 
     * @param userEntry       User map entry.
     * @param firstSongIndex  Index of first song for user.
     * @param secondSongIndex Index of second song for user.
     * @return {song1, song2} normalized ratings, or null if either rating is NaN.
     */
    private double[] fillUserRatings(Map.Entry<String, data_User> userEntry, int firstSongIndex, int secondSongIndex) {
        ArrayList<Double> songRatingList = getSongRatingArray(userEntry); // turns treemap to array list

        Double song1 = userEntry.getValue().getSongNormalizedRating(songRatingList.get(firstSongIndex));
        Double song2 = userEntry.getValue().getSongNormalizedRating(songRatingList.get(secondSongIndex));

        if (song1.isNaN() || song2.isNaN()) {
            return null;
        }
        return new double[] { song1, song2 };
    }

    /**
     * Transforms song map into array.
     * 
     * @param userEntry Used to get song list.
     * @return Arraylist of ratings.
     */
    private ArrayList<Double> getSongRatingArray(Map.Entry<String, data_User> userEntry) {
        TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();
        ArrayList<Double> songRating = new ArrayList<>(songNameRating.values());
        return songRating;
    }
}
