
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

/**
 * 
 * SongUserServicePA3V2
 */
public class SongUserServicePA3V2 extends SongUserFileService {

    // private ArrayList<Double> userRatingList;
    public Double[][] songSimilarityMatrix = new Double[masterSongNameList.size()][masterSongNameList.size()];

    /**
     * Calls methods to load from CSV, clear data, populate placeholder data, and
     * calculate song similarity.
     * 
     * @param inputFile Input CSV file path.
     * @throws IOException If invalid input file.
     */
    public void loadAndProcessData(String inputFile) throws IOException { // for main
        loadSongsandUsersFromCSV(inputFile);
        dataCleaner();
        populateRatingPlaceHolderData();
        calcSongSimilarity();
    }

    /**
     * Caclulates similarity of two songs for each user.
     * 
     * @return matrix of song similarity.
     * @throws IOException if validate cooperative users fail.
     */
    public Double[][] calcSongSimilarity() throws IOException {
        validateCooperativeUsers();

        int numSongs = this.masterSongNameList.size();
        this.songSimilarityMatrix = new Double[numSongs][numSongs];

        for (int firstSongIndex = 0; firstSongIndex < numSongs; firstSongIndex++) {
            for (int secondSongIndex = firstSongIndex + 1; secondSongIndex < numSongs; secondSongIndex++) {
                songSimilarityMatrix[firstSongIndex][secondSongIndex] = calcSongPairDistance(firstSongIndex,
                        secondSongIndex);
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
    private double calcSongPairDistance(int firstSongIndex, int secondSongIndex) {
        double[] song1Rating = new double[userList.size()];
        double[] song2Rating = new double[userList.size()];
        boolean songsHaveSharedUser = false;
        int userIndex = 0; // index tracker for the for-each loop

        for (Map.Entry<String, Cs214User> userEntry : this.userList.entrySet()) {
            double[] normalizedRatingPair = fillUserRatings(userEntry, firstSongIndex, secondSongIndex);
            if (normalizedRatingPair != null) {
                song1Rating[userIndex] = normalizedRatingPair[0];
                song2Rating[userIndex] = normalizedRatingPair[1];
                songsHaveSharedUser = true;
            }
            userIndex++;
        }

        if (!songsHaveSharedUser) {
            return Double.NaN;
        }
        return new EuclideanDistance().compute(song1Rating, song2Rating);
    }

    /**
     * Gets one user's normalized ratings for two songs.
     * 
     * @param userEntry       User map entry.
     * @param firstSongIndex  Index of first song for user.
     * @param secondSongIndex Index of second song for user.
     * @return {song1, song2} normalized ratings, or null if either rating is NaN.
     */
    private double[] fillUserRatings(Map.Entry<String, Cs214User> userEntry, int firstSongIndex, int secondSongIndex) {
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
    private ArrayList<Double> getSongRatingArray(Map.Entry<String, Cs214User> userEntry) {
        TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();
        ArrayList<Double> songRating = new ArrayList<>(songNameRating.values());
        return songRating;
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
     * Outputs song1 name, song2 name, and similarity between songs.
     * 
     * @param outputFile Output CSV file path.
     * @throws IOException If printer service fail.
     */
    public void outputUsersSongsRatingsCSV(String outputFile) throws IOException {
        validateArguments(outputFile);

        CSVPrinterService printerService = null;

        try {
            String[] headers = new String[] { "name1", "name2", "similarity" };

            printerService = new CSVPrinterService(outputFile, headers);

            for (int i = 0; i < songSimilarityMatrix.length; i++) {
                for (int j = 0; j < songSimilarityMatrix.length; j++) {
                    printRecord(printerService, i, j);
                }
            }

        } finally {
            if (printerService != null) {
                printerService.closeCSVFile();
            }
        }
    }

    /**
     * Outputs values objects array with song 1 name, song 2 name, and dimensions
     * matrix.
     * 
     * @param printerService CSV printer used to print record.
     * @param i              Index 1 of matrix.
     * @param j              Index 2 of matrix.
     * @throws IOException if printer fails.
     */
    private void printRecord(CSVPrinterService printerService, int i, int j) throws IOException {
        if (this.songSimilarityMatrix[i][j] != null && (i != j && this.songSimilarityMatrix[i][j] > 0.0
                || this.songSimilarityMatrix[i][j].equals(Double.NaN))) {
            Object[] values = { masterSongNameList.get(i), masterSongNameList.get(j), songSimilarityMatrix[i][j] };
            printerService.printRecord(values);
        }
    }
}
