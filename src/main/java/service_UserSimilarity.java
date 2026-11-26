import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

public class service_UserSimilarity {

    private Double[][] userSimilarityMatrix;

    private TreeMap<data_User, TreeMap<Double, data_User>> similarUserList = new TreeMap<>();

    public TreeMap<data_User, TreeMap<Double, data_User>> getSimilarUserList() {
        return similarUserList;
    }

    public Double[][] getUserSimilarityMatrix() {
        return userSimilarityMatrix;
    }

    /**
     * Calculates similarity between two users
     * 
     * @return Matrix of similarity for first user and second user.
     */
    public void calcUserSimilarity(TreeMap<String, data_User> userList, List<String> masterSongNameList) {
        int numUsers = userList.size();
        userSimilarityMatrix = new Double[numUsers][numUsers];

        // https://www.tutorialspoint.com/article/how-to-get-treemap-key-or-value-using-index-in-java

        initilizeSimilarUserListKey(userList);

        int firstUserIndex = 0;
        for (Map.Entry<String, data_User> userEntryRow : userList.entrySet()) {
            int secondUserIndex = 0;
            for (Map.Entry<String, data_User> userEntryColumn : userList.entrySet()) {
                double calcSongPair = calcSongPairDistance(userEntryRow.getValue(), userEntryColumn.getValue(), masterSongNameList);
                userSimilarityMatrix[firstUserIndex][secondUserIndex] = calcSongPair;

                populateUserSimilarityMap(userEntryRow.getValue(), userEntryColumn.getValue(), calcSongPair);

                secondUserIndex++;
            }

            firstUserIndex++;
        }
    }

    private void initilizeSimilarUserListKey(TreeMap<String, data_User> userList) {
        this.similarUserList = new TreeMap<>();
        for (Map.Entry<String, data_User> userEntry : userList.entrySet()) { // populate
                                                                                              // similarUserList treemap
                                                                                              // for all
            // users
            this.similarUserList.put(userEntry.getValue(), new TreeMap<Double, data_User>());
        }
    }

    private void populateUserSimilarityMap(data_User userEntryRow, data_User userEntryColumn, Double similarityValue) {
        if (userEntryRow != userEntryColumn && !similarityValue.isNaN()) {
            // can use key to get user
            TreeMap<Double, data_User> user1NestedTreeMap = similarUserList.get(userEntryRow); // get user key from the
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
    private double calcSongPairDistance(data_User firstUser, data_User secondUser, List<String> masterSongNameList) {

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

}
