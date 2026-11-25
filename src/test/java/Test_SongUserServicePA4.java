import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class Test_SongUserServicePA4 {
    @Test
    public void inputfileEmpty() throws IOException {
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> songUserServicePA4.loadUsersFromCSV(""));
        util_FormatOutput.printError(e.getMessage());
    }

    @Test
    public void inputfileNull() throws IOException {
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> songUserServicePA4.loadUsersFromCSV(null));
        util_FormatOutput.printError(e.getMessage());
    }

    @Test
    void test_ProcessUserData_Add() throws IOException {
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        songUserServicePA4.processUserData("song1", "user1", 2.0);
        assertTrue(songUserServicePA4.getUserList().containsKey("user1"));
        Cs214User currUser = songUserServicePA4.getUserList().get("user1");
        assertEquals(1, currUser.getuserCount());
    }

    @Test
    public void test_OutputFileSuccess() throws IOException {
        String outputFileName = "output.csv";
        File file = new File(outputFileName);
        if (file.exists()) { // Delete file if it exists
            file.delete();
        }
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        songUserServicePA4.outputUsersSongsRatingsCSV("database/files/PA4_file4.csv");
        songUserServicePA4.dataCleaner();
        songUserServicePA4.outputUsersSongsRatingsCSV(outputFileName);
        assertEquals(true, file.exists(), "test_OutputFileSuccess did not return expected exception.");
    }

    @Test
    public void test_OutputFileSuccess2() throws IOException {
        String outputFileName = "output.csv";
        File file = new File(outputFileName);
        if (file.exists()) { // Delete file if it exists
            file.delete();
        }
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        songUserServicePA4.outputUsersSongsRatingPredictionsCSV("database/files/PA4_file4.csv");
        songUserServicePA4.dataCleaner();
        songUserServicePA4.outputUsersSongsRatingPredictionsCSV(outputFileName);
        assertEquals(true, file.exists(), "test_OutputFileSuccess did not return expected exception.");
    }

    @Test
    public void test_DataCleaner() throws IOException {
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        songUserServicePA4.processUserData("song1", "invalidUser", 3);
        songUserServicePA4.processUserData("song2", "invalidUser", 3);
        songUserServicePA4.processUserData("song3", "validUser", 2);
        songUserServicePA4.processUserData("song4", "validUser", 5);
        songUserServicePA4.dataCleaner();

        assertFalse(songUserServicePA4.getUserList().containsKey("invalidUser"));
        assertTrue(songUserServicePA4.getUserList().containsKey("validUser"));
    }

    @Test
    public void test_calcSimilarity() throws IOException {
        SongUserServicePA4 songUserServicePA4 = new SongUserServicePA4();
        // songUserServicePA4.processSongData("song1", 5);
        // songUserServicePA4.processSongData("song2", 2);
        songUserServicePA4.processUserData("song1", "user1", 5);
        songUserServicePA4.processUserData("song2", "user1", 2);
        songUserServicePA4.processUserData("song1", "user2", 1);
        songUserServicePA4.processUserData("song2", "user2", 4);
        songUserServicePA4.getMasterSongNameList().add("song1");
        songUserServicePA4.getMasterSongNameList().add("song2");
        songUserServicePA4.calcSimilarity();
        Double[][] exampleMatrix = songUserServicePA4.getUserSimilarityMatrix();
        assertEquals(2, exampleMatrix.length);
        assertEquals(2, exampleMatrix[0].length);
        assertNotNull(exampleMatrix[0][1]);
    }
}
