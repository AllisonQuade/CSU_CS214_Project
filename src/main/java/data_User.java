import java.util.TreeMap;

/**
 * 
 * Cs214User
 */
public class data_User extends util_CalculateMeanSDNR implements Comparable<data_User> { // comparable used to sort by
                                                                                         // name

    private String userName;
    private int userCount = 0;
    private final TreeMap<String, Double> songList = new TreeMap<>();
    int songRatingCount = 0;

    public data_User(String newUserName) {
        if (util_FormatOutput.isNullOrEmpty(newUserName)) {
            throw new IllegalArgumentException("The prarameter \"newUserName\" is required.");
        }
        this.userName = newUserName;
    }

    @Override // take in Cs214User, returns compared username
    public int compareTo(data_User other) {
        return this.userName.compareTo(other.userName);
    }

    public void incrementsUserCount() {
        this.userCount++;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }

    public TreeMap<String, Double> getSongList() {
        return this.songList;
    }

    public int getSongRatingCount() {
        return this.songRatingCount;
    }

    public void addSongAndRating(String songName, Double newRating) {
        validateSongName(songName);
        validateRating(newRating);
        this.songList.put(songName, newRating);
        songRatingCount++;
    }

    private void validateSongName(String songName) {
        if (util_FormatOutput.isNullOrEmpty(songName)) {
            throw new IllegalArgumentException("The parameter \"songName\" is required.");
        }
        if (this.songList.containsKey(songName)) {
            throw new IllegalArgumentException("Song name '" + songName + "' already exists");
        }
    }

    private void validateRating(Double rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating " + rating + " is not between 1 and 5.");
        }
    }
}
