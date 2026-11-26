import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * Cs214Song
 */
public class data_Song extends util_CalculateMeanSDNR {
  private String songName; // stores name of song
  private final ArrayList<Double> ratings = new ArrayList<>(); // Puts rating of this song/user into an arraylist
  private final ArrayList<Double> normalizedRatings = new ArrayList<>();
  private ArrayList<Double> songEnrichedRatings = new ArrayList<>();

  @SuppressWarnings("OverridableMethodCallInConstructor")
  public data_Song(String newSongName) { // garuntees a song name
    this.setSongName(newSongName);
  }

  public void setSongName(String newSongName) {
    if (util_FormatOutput.isNullOrEmpty(newSongName)) {
      throw new IllegalArgumentException("Song Name is required.");
    }

    this.songName = newSongName;
  }

  public String getSongName() {
    return this.songName;
  }

  public int getNumberSongsOccured() {
    return this.ratings.size();
  }

  public int getRatingsCount() {
    return ratings.size();
  }

  public ArrayList<Double> getRatings() {
    return ratings;
  }

  public ArrayList<Double> getNormalizedRatings() {
    return this.normalizedRatings;
  }

  public ArrayList<Double> getSongEnrichedRatings() {
    return this.songEnrichedRatings;
  }

  /**
   * Add rating and uses rating to calculate running mean and standard deviation.
   * 
   * @param newRating Rating added to ratings array.
   * @throws IOException
   */
  public void addRating(double newRating) throws IOException {
    validateRating(newRating);
    this.ratings.add(newRating);
    super.calculateRunningMeanAndStandardDeviation(ratings);
  }

  public void addPredictedRating(double newRating) throws IOException {
    validateRating(newRating);
    this.songEnrichedRatings.add(newRating);
  }

  public void addNormalizedRatings(double newRating) throws IOException {
    this.normalizedRatings.add(newRating);
  }

  private void validateRating(double newRating) {
    if (newRating < 0 || newRating > 5) {
      throw new IllegalArgumentException("The rating" + newRating + "is out of bounds.");
    }
  }
}