import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math4.legacy.ml.distance.EuclideanDistance;

// extends PA 
/**
 * 
 * SongUserServicePA5
 * This service handles importing csv file and analyzing that data to display
 * user similarity user rating prediction
 */
public class SongUserServicePA5 {

    private TreeMap<String, Cs214User> userList = new TreeMap<>();
    private TreeMap<String, Cs214Song> songList = new TreeMap<>();
    private List<String> masterSongNameList = new ArrayList<>();
    private Double[][] userSimilarityMatrix;
    private TreeMap<Cs214User, TreeMap<Double, Cs214User>> similarUserList;
    private TreeMap<String, Double> userSongRatingPredictions;
    private ArrayList<Cs214Cluster> clusterList = new ArrayList<>();
    private String[] userSongNamesSelection;
    //private CSVPrinterService printerService = null;

    /**
     * deafult constructor
     */
    // public SongUserServicePA4() {
    // }

    /**
     * Calls methods to load data, clean data, populate placeholder data, and
     * calculate similarity.
     * 
     * @param inputFile Input CSV file path.
     * @throws IOException If load songlist.entrySet() and users from CSV fails.
     */
    public void loadAndProcessDataUserSimilarity(String inputFile) throws IOException { // PA3
        loadUsersFromCSV(inputFile);
        dataCleaner();
        populateRatingPlaceHolderData();
        calcSimilarity();
    }

    public void loadAndProcessDataPrediction(String inputFile) throws IOException { // PA4
        loadAndProcessDataUserSimilarity(inputFile);
        predictMissingUserSongRating();
    }

    public void loadAndProcessDataUserClustering(String inputFile, String[] userSongSelection) throws IOException { // PA5
        this.userSongNamesSelection = userSongSelection;
        validateDuplicateUserSelection();

        loadAndProcessDataPrediction(inputFile);
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

    public TreeMap<String, Cs214User> getUserList() {
        return userList;
    }

    public TreeMap<String, Cs214Song> getSongList() {
        return songList;
    }

    public ArrayList<Cs214Cluster> getClusterList() {
        return clusterList;
    }

    public List<String> getMasterSongNameList() {
        return masterSongNameList;
    }
       public Double[][] getUserSimilarityMatrix() {
        return userSimilarityMatrix;
    }

        public String[] getUserSongNamesSelection() {
        return userSongNamesSelection;
    }
       public void setUserSongNamesSelection(String[] userSongNamesSelection) {
        this.userSongNamesSelection = userSongNamesSelection;
    }

    /**
     * Parses through input file .
     * Populate and sort master songlist.entrySet() list with all unique songs.
     * 
     * @param inputFile Input CSV file path.
     * @return populated songlist.entrySet() list and user list.
     * @throws IOException
     */
    public TreeMap<String, Cs214User> loadUsersFromCSV(String inputFile) throws IOException {

        if (util_FormatOutput.isNullOrEmpty(inputFile)) {
            throw new IllegalArgumentException(
                    "Input file is required and must contain a file path and file name.");
        }

        this.songList = new TreeMap<>();
        this.userList = new TreeMap<>();

        this.masterSongNameList = new ArrayList<>();

        CSVParserService parserService = new CSVParserService(inputFile, false);
        for (CSVRecord record : parserService.getParser()) {
            if (record.size() != 3) {
                throw new IllegalArgumentException("Expected 3 fields but got " + record.size()
                        + " at record number " + record.getRecordNumber());
            }

            String songName = record.get(0); // Gets Song
            String userName = record.get(1); // Gets User
            double rating = Integer.parseInt(record.get(2));

            if (!masterSongNameList.contains(songName)) {
                masterSongNameList.add(songName);
            }

            processSongData(songName, rating);

            processUserData(songName, userName, rating);

            Collections.sort(masterSongNameList); // sorts final master list
        }

        parserService.closeCSVFile();

        return userList;

    }

    /**
     * Process song data.
     * 
     * @param songName Song name of song.
     * @param rating   Numeric rating of users song.
     */
    public void processSongData(String songName, double rating) throws IOException {
        Cs214Song currentSong;
        // If the song list already contains that song grab the song
        if (songList.containsKey(songName)) {
            currentSong = songList.get(songName);
        } // Otherwise add song
        else {
            currentSong = new Cs214Song(songName);
            songList.put(songName, currentSong);
        }

        currentSong.incrementsSongCount();
        currentSong.addRating(rating);
        // currentSong.addPredictedRating(rating);

    }

    /**
     * JavaDoc
     * Processes user data.
     * Calls methods from Cs214User to calculate mean and standard deviation.
     * 
     * @param songName Name of songlist.entrySet() for user
     * @param userName Name of user name for user.
     * @param rating   Numeric rating for users songlist.entrySet().
     */
    public void processUserData(String songName, String userName, double rating) {
        Cs214User currentUser;
        // If the user list already contains that user grab the user
        if (userList.containsKey(userName)) {
            currentUser = userList.get(userName);
        } // Otherwise add user
        else {
            currentUser = new Cs214User(userName);

            userList.put(userName, currentUser);
        }

        currentUser.addSongAndRating(songName, rating);

        currentUser.calculateRunningMeanAndStandardDeviation(new ArrayList<>(currentUser.getSongList().values()));

        currentUser.incrementsUserCount();
    }

    /**
     * Removes invalid users from data.
     * Removes invalid user songlist.entrySet() from master songlist.entrySet() lsit
     * if songlist.entrySet() only belonged to invalid user.
     */
    public void dataCleaner() {
        List<String> invalidUsers = new ArrayList<>();

        Set<String> validUserSongs = new TreeSet<>();
        Set<String> invalidUserSongs = new TreeSet<>();

        for (Map.Entry<String, Cs214User> userEntry : this.userList.entrySet()) { // stepthrough users
            TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();

            // this list keeps track of songlist.entrySet() names for current user
            Set<String> currentUserSongs = new TreeSet<>();

            Double firstRating = songNameRating.firstEntry().getValue(); // the first rating in the treeMap

            Boolean invalidUser = isInvalidUser(songNameRating, currentUserSongs, firstRating);

            if (invalidUser) { // invalid user
                invalidUserSongs.addAll(currentUserSongs); // merge all user songs into invalid user songs list
                invalidUsers.add(userEntry.getKey()); // add current invalid user name to invalidUsers list
            } else {
                validUserSongs.addAll(currentUserSongs);
            }
        }

        invalidUserSongs.removeAll(validUserSongs);
        masterSongNameList.removeAll(invalidUserSongs);

        for (String user : invalidUsers) {
            userList.remove(user);
        }
    }

    /**
     * Determines if a user is invalid or valid.
     * 
     * @param songNameRating   Used to stepthrough user's songs and ratings.
     * @param currentUserSongs List of all current user songs.
     * @param firstRating      Users first rating.
     * @param invalidUser      Status if user invalid or valid.
     * @return true if invalid user, false if valid.
     */
    private Boolean isInvalidUser(TreeMap<String, Double> songNameRating, Set<String> currentUserSongs,
            Double firstRating) {
        Boolean invalidUser = true;
        for (Map.Entry<String, Double> songEntry : songNameRating.entrySet()) { // stepthrough user's songs and
            // ratings
            currentUserSongs.add(songEntry.getKey()); // list of all my current user songs

            // if(!firstRating.equals(songEntry.getValue())){ //valid user //This if way
            // works same as if below, gets value of Double and NOT object (memory space)
            if (firstRating.doubleValue() != songEntry.getValue().doubleValue()) {
                invalidUser = false;
            }
        }
        return invalidUser;
    }

    /**
     * Puts missing songlist.entrySet() from user into songlist.entrySet() list with
     * NaN.
     */
    public void populateRatingPlaceHolderData() {
        for (String currentSongName : this.masterSongNameList) {
            for (Map.Entry<String, Cs214User> userEntry : this.userList.entrySet()) {
                TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();
                if (!songNameRating.containsKey(currentSongName)) {
                    songNameRating.put(currentSongName, Double.NaN);
                }
            }
        }
    }

    /**
     * Calculates similarity between two users
     * 
     * @return Matrix of similarity for first user and second user.
     */
    public void calcSimilarity() {
        validateCooperativeUsers();

        int numUsers = this.userList.size();
        this.userSimilarityMatrix = new Double[numUsers][numUsers];

        // https://www.tutorialspoint.com/article/how-to-get-treemap-key-or-value-using-index-in-java

        initilizeSimilarUserListKey();

        int firstUserIndex = 0;
        for (Map.Entry<String, Cs214User> userEntryRow : this.userList.entrySet()) {
            int secondUserIndex = 0;
            for (Map.Entry<String, Cs214User> userEntryColumn : this.userList.entrySet()) {
                double calcSongPair = calcSongPairDistance(userEntryRow.getValue(), userEntryColumn.getValue());
                userSimilarityMatrix[firstUserIndex][secondUserIndex] = calcSongPair;
                // userSimilarityMatrix[secondUserIndex][firstUserIndex] = calcSongPair;

                populateUserSimilarityMap(userEntryRow.getValue(), userEntryColumn.getValue(), calcSongPair);

                secondUserIndex++;
            }
            firstUserIndex++;
        }
    }

    public void initilizeSimilarUserListKey() {
        this.similarUserList = new TreeMap<>();
        for (Map.Entry<String, Cs214User> userEntry : userList.entrySet()) { // populate similarUserList treemap for all
                                                                             // users
            this.similarUserList.put(userEntry.getValue(), new TreeMap<Double, Cs214User>());
        }
    }

    private void populateUserSimilarityMap(Cs214User userEntryRow, Cs214User userEntryColumn, Double similarityValue) {
        if (userEntryRow != userEntryColumn && !similarityValue.isNaN()) {
            // can use key to get user
            TreeMap<Double, Cs214User> user1NestedTreeMap = similarUserList.get(userEntryRow); // get user key from the
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
    private double calcSongPairDistance(Cs214User firstUser, Cs214User secondUser) {

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

    /**
     * Used to require two cooperative user to compute user similarity.
     */
    private void validateCooperativeUsers() {
        if (this.userList.size() < 2) {
            throw new IllegalArgumentException("at least two cooperative users are required for user similarity");
        }
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
     * Predict a target user's empty rating based on the most similar users rating
     */
    public void predictMissingUserSongRating() throws IOException {
        this.userSongRatingPredictions = new TreeMap<>();
        // targetUser: User that is missing rating data.
        // otherUser: user that is the closest in similarity to targetUser.

        // Loop through all users as targets
        for (Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry : this.similarUserList.entrySet()) {
            TreeMap<String, Double> songListUser1 = userEntry.getKey().getSongList();

            predictTargetUserSongRatings(userEntry, songListUser1);

        }

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

    private void predictTargetUserSongRatings(Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry,
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

    private double calculatePredictedRating(Map.Entry<Cs214User, TreeMap<Double, Cs214User>> userEntry,
            Map.Entry<String, Double> songEntry) {
        double predictedRating = Double.NaN;

        // Loop through target user similar user list
        for (Map.Entry<Double, Cs214User> similarUserEntry : userEntry.getValue().entrySet()) {
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

    public void initializeKCentroids(String[] userSongSelection) {

        // userSelectionNotFound(userSongSelection);

        for (int i = 0; i < userSongSelection.length; i++) {
            Cs214Cluster cluster = new Cs214Cluster(userSongSelection[i]);
            clusterList.add(cluster);
            // cluster.getInitialCentroid().add(normRating);
            cluster.getCentroid().addAll(songList.get(userSongSelection[i]).getNormalizedRatings());

        }
        int x = 1;
    }

    public void userSelectionNotFound() {
        for (String userChoice : this.userSongNamesSelection) {
            if (!this.songList.containsKey(userChoice)) {
                throw new IllegalArgumentException("userChoice song must exist in input file.");
            }
        }
    }

    // START KMEANS ALGORITHM
    public void iterateKMeansClustering(int numberIterations) {
        for (int index = 0; index < numberIterations; index++) {
            placeSongInCluster();
            // K-Means Clustering Algorithm (last bullet point)
            recalculateClusterCentroids();
        }
        int x = 1;
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

    /**
     * Outputs user1 name, user2 name, and similarity between users.
     * 
     * @param outputFile Output CSV file path.
     * @throws IOException If printer service fail.
     */
    // public void outputUsersSongsRatingsCSV(String outputFile) throws IOException
    // {
    // validateArguments(outputFile);

    // try {
    // String[] headers = new String[] { "name1", "name2", "similarity" };

    // printerService = new CSVPrinterService(outputFile, headers);

    // List<Map.Entry<String, Cs214User>> usersArray = new
    // ArrayList<>(this.userList.entrySet());

    // for (int rowIndex = 0; rowIndex < userSimilarityMatrix.length; rowIndex++) {
    // printDataRow(usersArray, rowIndex);
    // }
    // } finally {
    // if (printerService != null) {
    // printerService.closeCSVFile();
    // }
    // }
    // }

    // private void printDataRow(List<Map.Entry<String, Cs214User>> usersArray, int
    // rowIndex) throws IOException {
    // for (int colIndex = rowIndex + 1; colIndex < userSimilarityMatrix.length;
    // colIndex++) {
    // Double similariyValue = this.userSimilarityMatrix[rowIndex][colIndex];

    // if (similariyValue != null && rowIndex != colIndex
    // && (similariyValue >= 0.0 || similariyValue.equals(Double.NaN))) {
    // Object[] values = { usersArray.get(rowIndex).getKey(),
    // usersArray.get(colIndex).getKey(),
    // similariyValue };
    // printerService.printRecord(values);
    // }
    // }
    // }

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
