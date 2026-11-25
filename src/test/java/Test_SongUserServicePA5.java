import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

public class Test_SongUserServicePA5 {
    @Test
    public void test_inputfileNotFound() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        FileNotFoundException e = assertThrows(
                FileNotFoundException.class,
                () -> songUserServicePA5.loadAndProcessDataUserClustering("database/files/testfiles/unkown.csv", new String[] { "song1", "song2" }),
                "test_inputfileNotFound did not return expected exception.");
        util_FormatOutput.printError(e.getMessage());
    }

    @Test
    public void test_inputfileEmpty() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> songUserServicePA5.loadAndProcessDataUserClustering("", new String[] { "song1", "song2" }),
                "test_inputfileEmpty did not return expected exception.");
        util_FormatOutput.printError(e.getMessage());
    }

    @Test
    public void test_inputfileNull() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> songUserServicePA5.loadAndProcessDataUserClustering(null, new String[] { "song1", "song2" }),
                "test_inputfileNull did not return expected exception.");
        util_FormatOutput.printError(e.getMessage());
    }

    @Test
    void test_ProcessSongData_Add() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        // TreeMap<String, Cs214Song> songList = new TreeMap<>();
        songUserServicePA5.processSongData("song1", 5);
        assertTrue(songUserServicePA5.getSongList().containsKey("song1"));
        Cs214Song currSong = songUserServicePA5.getSongList().get("song1");
        assertEquals(1, currSong.getNumberSongsOccured());
    }

    @Test
    void test_ProcessUserData_Add() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processUserData("song1", "user1", 2.0);
        assertTrue(songUserServicePA5.getUserList().containsKey("user1"));
        Cs214User currUser = songUserServicePA5.getUserList().get("user1");
        assertEquals(1, currUser.getuserCount());
    }

    @Test
    public void test_OutputFileSuccess() throws IOException {
        String outputFileName = "output.csv";
        File file = new File(outputFileName);
        if (file.exists()) { // Delete file if it exists
            file.delete();
        }
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.loadAndProcessDataUserClustering("database/files/testfiles/PA2tests/test_outputfile_success.csv", new String[] { "song1", "song2" });
        songUserServicePA5.outputUserChoiceRecommendations(outputFileName);
        assertEquals(true, file.exists(), "test_OutputFileSuccess did not return expected exception.");
    }

    @Test
    public void test_NaNReplace() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", Double.NaN);
        songUserServicePA5.processSongData("song2", 3);
        songUserServicePA5.processUserData("song1", "user1", Double.NaN);
        songUserServicePA5.processUserData("song2", "user1", 3);
        songUserServicePA5.replaceNaNWithWeightedAverageRating();
        Double q = songUserServicePA5.getUserList().get("user1").getSongList().get("song2");
        assertNotNull(q);
    }

    @Test
    public void test_DataCleaner() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.processSongData("song2", 3);
        songUserServicePA5.processUserData("song1", "invalidUser", 3);
        songUserServicePA5.processUserData("song2", "invalidUser", 3);
        songUserServicePA5.processSongData("song3", 2);
        songUserServicePA5.processSongData("song4", 5);
        songUserServicePA5.processUserData("song3", "validUser", 2);
        songUserServicePA5.processUserData("song4", "validUser", 5);
        songUserServicePA5.dataCleaner();

        assertFalse(songUserServicePA5.getUserList().containsKey("invalidUser"));
        assertTrue(songUserServicePA5.getUserList().containsKey("validUser"));
    }

    @Test
    public void test_initializeKCentroids() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 1);
        songUserServicePA5.processSongData("song1", 2);
        songUserServicePA5.processSongData("song2", 5);
        songUserServicePA5.processSongData("song3", 3);
        String[] selectionExample = { "song1", "song2" };
        songUserServicePA5.initializeKCentroids(selectionExample);
        List<Cs214Cluster> clusterExample = songUserServicePA5.getClusterList();
        assertEquals(2, clusterExample.size());
    }

    @Test
    public void test_calcSimilarity() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 5);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.processUserData("song1", "user1", 5);
        songUserServicePA5.processUserData("song2", "user1", 2);
        songUserServicePA5.processUserData("song1", "user2", 1);
        songUserServicePA5.processUserData("song2", "user2", 4);
        songUserServicePA5.getMasterSongNameList().add("song1");
        songUserServicePA5.getMasterSongNameList().add("song2");
        songUserServicePA5.calcUserSimilarity();
        Double[][] exampleMatrix = songUserServicePA5.getUserSimilarityMatrix();
        assertEquals(2, exampleMatrix.length);
        assertEquals(2, exampleMatrix[0].length);
        assertNotNull(exampleMatrix[0][1]);
    }

    @Test
    public void test_populateRatingPlaceHolderData() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 4);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.processUserData("song1", "user1", 4);
        songUserServicePA5.processUserData("song2", "user2", 2);
        songUserServicePA5.getMasterSongNameList().add("song1");
        songUserServicePA5.getMasterSongNameList().add("song2");
        songUserServicePA5.populateRatingPlaceHolderData();
        TreeMap<String, Double> user1 = songUserServicePA5.getUserList().get("user1").getSongList();
        TreeMap<String, Double> user2 = songUserServicePA5.getUserList().get("user2").getSongList();
        assertEquals(2, user1.size());
        assertEquals(2, user2.size());
        assertTrue(user1.get("song2").isNaN());
        assertTrue(user2.get("song1").isNaN());
        assertEquals(4, user1.get("song1"));
        assertEquals(2, user2.get("song2"));
    }

    @Test
    public void test_initilizeSimilarUserListKey_noUser() {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.initilizeSimilarUserListKey();
        assertNotNull(songUserServicePA5.getUserList());
        assertTrue(songUserServicePA5.getUserList().isEmpty());
    }

    @Test
    public void test_UserChoiceValidationCheck() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.processSongData("song3", 4);
        String[] selection = new String[] { "song1", "song2" };
        songUserServicePA5.setUserSongNamesSelection(selection);
        songUserServicePA5.initializeKCentroids(selection);
        Cs214Cluster choice = songUserServicePA5.getClusterList().get(0);
        Cs214Cluster choice1 = songUserServicePA5.getClusterList().get(1);
        choice.getSongs().add("song1");
        choice1.getSongs().add("song2");
        choice1.getSongs().add("song3");
        songUserServicePA5.UserChoiceValidationCheck();
        assertTrue(choice.getSongs().isEmpty());
        assertEquals(2, choice1.getSongs().size());
    }

    @Test
    public void test_removeOneDistinctRating() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.processSongData("song2", 1);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.processSongData("song3", 4);
        songUserServicePA5.processSongData("song3", 3);
        songUserServicePA5.processUserData("song1", "user1", 3);
        songUserServicePA5.processUserData("song2", "user1", 1);
        songUserServicePA5.processUserData("song3", "user1", 4);
        songUserServicePA5.processUserData("song1", "user2", 3);
        songUserServicePA5.processUserData("song2", "user2", 2);
        songUserServicePA5.processUserData("song3", "user2", 3);
        songUserServicePA5.getSongList().get("song1").getSongEnrichedRatings().add(3.0);
        songUserServicePA5.getSongList().get("song1").getSongEnrichedRatings().add(3.0);
        songUserServicePA5.getSongList().get("song2").getSongEnrichedRatings().add(1.0);
        songUserServicePA5.getSongList().get("song2").getSongEnrichedRatings().add(2.0);
        songUserServicePA5.getSongList().get("song3").getSongEnrichedRatings().add(4.0);
        songUserServicePA5.getSongList().get("song3").getSongEnrichedRatings().add(3.0);
        songUserServicePA5.setUserSongNamesSelection(new String[] { "song2" });
        songUserServicePA5.removeOneDistinctRating();
        assertFalse(songUserServicePA5.getSongList().containsKey("song1"));
        assertTrue(songUserServicePA5.getSongList().containsKey("song2"));
        assertTrue(songUserServicePA5.getSongList().containsKey("song3"));
        assertFalse(songUserServicePA5.getUserList().get("user1").getSongList().containsKey("song1"));
        assertFalse(songUserServicePA5.getUserList().get("user2").getSongList().containsKey("song1"));
    }

    @Test
    public void test_validateDuplicateUserSelection() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.setUserSongNamesSelection(new String[] { "song1", "song2", "song2" });
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                songUserServicePA5::validateDuplicateUserSelection);
        assertEquals("User choice song selections must be unique.", e.getMessage());
    }

    @Test
    public void test_userSelectionNotFound_ValidInput() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.setUserSongNamesSelection(new String[] { "song1", "song2" });
        assertDoesNotThrow(songUserServicePA5::userSelectionNotFound);
    }

    @Test
    public void test_userSelectionNotFound() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 3);
        songUserServicePA5.setUserSongNamesSelection(new String[] { "song1", "invalidSong" });
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                songUserServicePA5::userSelectionNotFound);
        assertEquals("userChoice song must exist in input file.", e.getMessage());
    }

    @Test
    public void test_emptyClusterAndUserChoiceValidationCheck() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 4);
        songUserServicePA5.processSongData("song2", 3);
        songUserServicePA5.processSongData("song3", 2);
        String[] userChoicesExample = { "song1", "song2" };
        songUserServicePA5.setUserSongNamesSelection(userChoicesExample);
        songUserServicePA5.initializeKCentroids(userChoicesExample);
        songUserServicePA5.getClusterList().get(0).getSongs().add("song1");
        songUserServicePA5.getClusterList().get(0).getSongs().add("song3");
        assertDoesNotThrow(songUserServicePA5::emptyClusterAndUserChoiceValidationCheck);
    }

    @Test
    public void test_removeUserChoiceSongsFromRecommendation() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 4);
        songUserServicePA5.processSongData("song2", 3);
        songUserServicePA5.processSongData("song3", 2);
        songUserServicePA5.setUserSongNamesSelection(new String[] { "song1", "song2" });
        songUserServicePA5.initializeKCentroids(new String[] { "song1", "song2" });
        Cs214Cluster cluster1 = songUserServicePA5.getClusterList().get(0);
        Cs214Cluster cluster2 = songUserServicePA5.getClusterList().get(1);
        cluster1.getSongs().add("song1");
        cluster1.getSongs().add("song3");
        cluster2.getSongs().add("song3");
        songUserServicePA5.removeUserChoiceSongsFromRecommendation();
        assertFalse(cluster1.getSongs().contains("song1"));
        assertTrue(cluster1.getSongs().contains("song3"));
        assertEquals(1, cluster1.getSongs().size());
        assertEquals(1, cluster2.getSongs().size());
    }

    @Test
    public void test_updateRunningMeanStandardDeviation() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 2);
        songUserServicePA5.processSongData("song2", 5);
        songUserServicePA5.getSongList().get("song1").getSongEnrichedRatings().add(2.0);
        songUserServicePA5.getSongList().get("song1").getSongEnrichedRatings().add(3.0);
        songUserServicePA5.getSongList().get("song2").getSongEnrichedRatings().add(4.0);
        songUserServicePA5.getSongList().get("song2").getSongEnrichedRatings().add(4.0);
        songUserServicePA5.updateRunningMeanStandardDeviation();
        Cs214Song song1 = songUserServicePA5.getSongList().get("song1");
        Cs214Song song2 = songUserServicePA5.getSongList().get("song2");
        assertEquals(2.5, song1.getRunningMean());
        assertTrue(song1.getStandardDeviation() > 0);
        assertEquals(4.0, song2.getRunningMean());
        assertEquals(0.0, song2.getStandardDeviation());
    }

    @Test
    public void test_populateSongListRatingsFromUserSongList() throws IOException {
        SongUserServicePA5 songUserServicePA5 = new SongUserServicePA5();
        songUserServicePA5.processSongData("song1", 4);
        songUserServicePA5.processSongData("song2", 2);
        songUserServicePA5.processUserData("song1", "user1", 4);
        songUserServicePA5.processUserData("song2", "user1", 2);
        songUserServicePA5.processUserData("song1", "user2", 3);
        songUserServicePA5.populateSongListRatingsFromUserSongList();
        ArrayList<Double> song1 = songUserServicePA5.getSongList().get("song1").getSongEnrichedRatings();
        ArrayList<Double> song2 = songUserServicePA5.getSongList().get("song2").getSongEnrichedRatings();
        assertEquals(2, song1.size());
        assertTrue(song1.contains(4.0));
        assertTrue(song1.contains(3.0));
        assertEquals(1, song2.size());
        assertTrue(song2.contains(2.0));

    }
}
