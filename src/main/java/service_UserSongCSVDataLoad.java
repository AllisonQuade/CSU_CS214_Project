import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVRecord;

public class service_UserSongCSVDataLoad {
    public TreeMap<String, data_Song> songList = new TreeMap<>();
    public TreeMap<String, data_User> userList = new TreeMap<>();
    public List<String> masterSongNameList = new ArrayList<>();

    public List<String> getMasterSongNameList() {
        return this.masterSongNameList;
    }

    service_SongSimilarity songSimilarityService;

    public TreeMap<String, data_User> getUserList() {
        return this.userList;
    }

    public TreeMap<String, data_Song> getSongList() {
        return this.songList;
    }

    // PA1
    public void loadFileData(String inputFile) throws IOException {
        if (util_FormatOutput.isNullOrEmpty(inputFile)) {
            throw new IllegalArgumentException(
                    "Input file is required and must contain a file path and file name.");
        }
        service_CSVParseFile parserService = new service_CSVParseFile(inputFile,
                false);
        loadSongsandUsersFromCSV(parserService);
    }

    // PA2
    public void loadAndCleanFileData(String inputFile) throws IOException {
        loadFileData(inputFile);
        dataCleaner();
    }

    // PA3
    public void loadAndCleanAndProcessFileData(String inputFile) throws IOException {
        loadAndCleanFileData(inputFile);
        populateRatingPlaceHolderData();
    }

    // PA1
    public void loadStringData(String inputData) throws IOException {
        if (util_FormatOutput.isNullOrEmpty(inputData)) {
            throw new IllegalArgumentException("inputData parameter is required.");
        }
        service_CSVParseString parserService = new service_CSVParseString(inputData,
                false);
        loadSongsandUsersFromCSV(parserService);
    }

    // PA2
    public void loadAndCleanStringData(String inputData) throws IOException {
        loadStringData(inputData);
        dataCleaner();
    }

    // PA3
    public void loadAndCleanAndProcessStringData(String inputData) throws IOException {
        loadAndCleanStringData(inputData);
        populateRatingPlaceHolderData();
    }

    /**
     * Parse through CSV input file.
     * 
     * @param inputFile Input CSV file path.
     * @return song list and user list.
     * @throws IOException If input file null or empty.
     */
    private void loadSongsandUsersFromCSV(interface_CSVParser parserService) throws IOException {

        this.songList = new TreeMap<>();
        this.userList = new TreeMap<>();

        this.masterSongNameList = new ArrayList<>();

        for (CSVRecord record : parserService.getParser()) {
            if (record.size() != 3) {
                throw new IllegalArgumentException("Expected 3 fields but got " + record.size()
                        + " at record number " + record.getRecordNumber());
            }

            String songName = record.get(0); // Gets Song name from CSV
            String userName = record.get(1); // Gets User name from CSV
            double rating = Integer.parseInt(record.get(2));

            if (!masterSongNameList.contains(songName)) {
                masterSongNameList.add(songName);
            }

            processSongData(songName, rating);

            processUserData(songName, userName, rating);

            Collections.sort(masterSongNameList);
        }

        parserService.closeCSVFile();
    }

    /**
     * Process user data.
     * 
     * @param songName Song name of song for user.
     * @param userName User name for user.
     * @param rating   Numeric rating of users song.
     */
    public void processUserData(String songName, String userName, double rating) {
        data_User currentUser;
        // If the user list already contains that user grab the user
        if (userList.containsKey(userName)) {
            currentUser = userList.get(userName);
        } // Otherwise add user
        else {
            currentUser = new data_User(userName);

            userList.put(userName, currentUser);
        }

        currentUser.addSongAndRating(songName, rating);

        currentUser.calculateRunningMeanAndStandardDeviation(new ArrayList<>(currentUser.getSongList().values()));

        currentUser.incrementsUserCount();
    }

    /**
     * Process song data.
     * 
     * @param songName Song name of song.
     * @param rating   Numeric rating of users song.
     */
    public void processSongData(String songName, double rating) throws IOException {
        data_Song currentSong;
        // If the song list already contains that song grab the song
        if (songList.containsKey(songName)) {
            currentSong = songList.get(songName);
        } // Otherwise add song
        else {
            currentSong = new data_Song(songName);
            songList.put(songName, currentSong);
        }
        currentSong.addRating(rating);
    }

    /**
     * Removes invalid users from data.
     * Removes invalid user song from master song lsit if song only belonged to
     * invalid user.
     * 
     * @throws IOException If unable to clean data.
     */
    private void dataCleaner() throws IOException {
        List<String> invalidUsers = new ArrayList<>();

        Set<String> validUserSongs = new TreeSet<>();
        Set<String> invalidUserSongs = new TreeSet<>();

        for (Map.Entry<String, data_User> userEntry : this.userList.entrySet()) { // stepthrough users
            TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();

            // this list keeps track of song names for current user
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

        validateCooperativeUsers();
    }

    /**
     * Puts missing song from user into song list with NaN.
     */
    private void populateRatingPlaceHolderData() { // puts missing song from user into songList with NaN
        for (String currentSongName : this.masterSongNameList) {
            for (Map.Entry<String, data_User> userEntry : this.userList.entrySet()) {
                TreeMap<String, Double> songNameRating = userEntry.getValue().getSongList();
                if (!songNameRating.containsKey(currentSongName)) {
                    songNameRating.put(currentSongName, Double.NaN);
                }
            }
        }
    }

    /**
     * Used to require two cooperative user to compute user similarity.
     */
    public void validateCooperativeUsers() {
        if (this.userList.size() <= 1) {
            throw new IllegalArgumentException("at least two cooperative users are required for song similarity");
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
}
