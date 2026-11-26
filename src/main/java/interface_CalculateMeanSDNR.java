import java.util.ArrayList;

public interface interface_CalculateMeanSDNR {
    /**
     * default constructor
     */
    // public util_CalculateMeanSDNR(){
    // }

    /**
     * Calculate running mean and standard deviation for current user
     * 
     * @param ratings Ratings array list used for calculations.
     */
    void calculateRunningMeanAndStandardDeviation(ArrayList<Double> ratings);

    /**
     * 
     * @return Running mean calculated.
     */
    double getRunningMean();

    /**
     * @return Standard deviation calculated.
     */
    double getStandardDeviation();

    /**
     * Calculates normalized rating using rating, running mean, and standard
     * deviation.
     * 
     * @param rating Rating of song used for calculation.
     * @return Normalized rating.
     */
    double getSongNormalizedRating(double rating);

}
