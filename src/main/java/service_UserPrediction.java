import java.util.Map;
import java.util.TreeMap;

public class service_UserPrediction {
    public TreeMap<String, Double> userSongRatingPredictions = new TreeMap<>();

    public TreeMap<String, Double> getUserSongRatingPredictions() {
        return userSongRatingPredictions;
    }

    /**
     * Predict a target user's empty rating based on the most similar users rating
     */
    public void predictMissingUserSongRating(boolean dontAllowEmptyUserSongPredictionRatings, TreeMap<data_User, TreeMap<Double, data_User>> similarUserList) {

        this.userSongRatingPredictions = new TreeMap<>();
        
        // targetUser: User that is missing rating data.
        // otherUser: user that is the closest in similarity to targetUser.

        // Loop through all users as targets
        for (Map.Entry<data_User, TreeMap<Double, data_User>> userEntry : similarUserList.entrySet()) {
            TreeMap<String, Double> songListUser1 = userEntry.getKey().getSongList();

            predictTargetUserSongRatings(userEntry, songListUser1);

        }
        if (dontAllowEmptyUserSongPredictionRatings && userSongRatingPredictions.size() == 0) {
            throw new IllegalArgumentException("All users rated all songs. No predictions needed.");
        }
    }

    private void predictTargetUserSongRatings(Map.Entry<data_User, TreeMap<Double, data_User>> userEntry,
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

    private double calculatePredictedRating(Map.Entry<data_User, TreeMap<Double, data_User>> userEntry,
            Map.Entry<String, Double> songEntry) {
        double predictedRating = Double.NaN;

        // Loop through target user similar user list
        for (Map.Entry<Double, data_User> similarUserEntry : userEntry.getValue().entrySet()) {
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
}
