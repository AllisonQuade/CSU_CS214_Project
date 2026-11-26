// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.io.File;
// import java.io.IOException;

// import org.junit.jupiter.api.Test;

// public class Test_SongUserServicePA4V2 {

//     @Test
//     void test_ProcessUserData_Add() throws IOException {
//         SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
//         SongUserServicePA4V2.processUserData("song1", "user1", 2.0);
//         assertTrue(SongUserServicePA4V2.getUserList().containsKey("user1"));
//         data_User currUser = SongUserServicePA4V2.getUserList().get("user1");
//         assertEquals(1, currUser.getuserCount());
//     }

//     @Test
//     public void test_OutputFileSuccess() throws IOException {
//         String outputFileName = "output.csv";
//         File file = new File(outputFileName);
//         if (file.exists()) { // Delete file if it exists
//             file.delete();
//         }
//         SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
//         SongUserServicePA4V2.outputUsersSongsRatingsCSV("database/files/PA4_file4.csv");
//         SongUserServicePA4V2.dataCleaner();
//         SongUserServicePA4V2.outputUsersSongsRatingsCSV(outputFileName);
//         assertEquals(true, file.exists(), "test_OutputFileSuccess did not return expected exception.");
//     }

//     @Test
//     public void test_OutputFileSuccess2() throws IOException {
//         String outputFileName = "output.csv";
//         File file = new File(outputFileName);
//         if (file.exists()) { // Delete file if it exists
//             file.delete();
//         }
//         SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
//         SongUserServicePA4V2.outputUsersSongsRatingPredictionsCSV("database/files/PA4_file4.csv");
//         SongUserServicePA4V2.dataCleaner();
//         SongUserServicePA4V2.outputUsersSongsRatingPredictionsCSV(outputFileName);
//         assertEquals(true, file.exists(), "test_OutputFileSuccess did not return expected exception.");
//     }

//     @Test
//     public void test_DataCleaner() throws IOException {
//         SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
//         SongUserServicePA4V2.processUserData("song1", "invalidUser", 3);
//         SongUserServicePA4V2.processUserData("song2", "invalidUser", 3);
//         SongUserServicePA4V2.processUserData("song3", "validUser", 2);
//         SongUserServicePA4V2.processUserData("song4", "validUser", 5);
//         SongUserServicePA4V2.dataCleaner();

//         assertFalse(SongUserServicePA4V2.getUserList().containsKey("invalidUser"));
//         assertTrue(SongUserServicePA4V2.getUserList().containsKey("validUser"));
//     }

//     @Test
//     public void test_calcSimilarity() throws IOException {
//         SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
//         // SongUserServicePA4V2.processSongData("song1", 5);
//         // SongUserServicePA4V2.processSongData("song2", 2);
//         SongUserServicePA4V2.processUserData("song1", "user1", 5);
//         SongUserServicePA4V2.processUserData("song2", "user1", 2);
//         SongUserServicePA4V2.processUserData("song1", "user2", 1);
//         SongUserServicePA4V2.processUserData("song2", "user2", 4);
//         SongUserServicePA4V2.getMasterSongNameList().add("song1");
//         SongUserServicePA4V2.getMasterSongNameList().add("song2");
//         SongUserServicePA4V2.calcUserSimilarity();
//         Double[][] exampleMatrix = SongUserServicePA4V2.getUserSimilarityMatrix();
//         assertEquals(2, exampleMatrix.length);
//         assertEquals(2, exampleMatrix[0].length);
//         assertNotNull(exampleMatrix[0][1]);
//     }
// }
