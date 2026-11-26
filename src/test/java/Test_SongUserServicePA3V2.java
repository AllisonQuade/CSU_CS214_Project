// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.io.IOException;

// import org.junit.jupiter.api.Test;

// public class Test_SongUserServicePA3V2 {
//     @Test
//     public void inputfileEmpty() throws IOException {
//         SongUserServicePA3V2 songUserServicePA3V2 = new SongUserServicePA3V2();
//         IllegalArgumentException e = assertThrows(
//                 IllegalArgumentException.class,
//                 () -> songUserServicePA3V2.loadSongsandUsersFromCSV(""));
//         util_FormatOutput.printError(e.getMessage());
//     }

//     @Test
//     public void inputfileNull() throws IOException {
//         SongUserServicePA3V2 songUserServicePA3V2 = new SongUserServicePA3V2();
//         IllegalArgumentException e = assertThrows(
//                 IllegalArgumentException.class,
//                 () -> songUserServicePA3V2.loadSongsandUsersFromCSV(null));
//         util_FormatOutput.printError(e.getMessage());
//     }

//         @Test
//     public void test_DataCleaner() throws IOException{
//         SongUserServicePA3V2 songUserServicePA3V2 = new SongUserServicePA3V2();
//         songUserServicePA3V2.processSongData("song1",3);
//         songUserServicePA3V2.processSongData("song2",3);
//         songUserServicePA3V2.processUserData("song1", "invalidUser", 3);
//         songUserServicePA3V2.processUserData("song2", "invalidUser", 3);
//         songUserServicePA3V2.processSongData("song3", 2);
//         songUserServicePA3V2.processSongData("song4", 5);
//         songUserServicePA3V2.processUserData("song3", "validUser", 2);
//         songUserServicePA3V2.processUserData("song4", "validUser", 5);
//         songUserServicePA3V2.dataCleaner();

//         assertFalse(songUserServicePA3V2.getUserList().containsKey("invalidUser"));
//         assertTrue(songUserServicePA3V2.getUserList().containsKey("validUser"));
//     }
// }
