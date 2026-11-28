import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

// ******************************************************************
// ******************************************************************
// TERMINAL COMMANDS TO RUN PROGRAM
// ******************************************************************
// gradle run -q --args="'database/files/file2.csv' 'output.csv'"gradle run -q --args="'database/files/file2.csv' 'output.csv'"
// ******************************************************************

/**
 * 
 * Cs214Project Main
 */
public class Cs214Project {
    /**
     * Runs program for each PA
     * 
     * @param args user inputs
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException {
        // Open the project in Codio's integrated Visual Studio Code!
        // See Hints or README.md for more information
        try { // graceful exit (no red error meassage)
            validateArguments(args);
            processPA(args);
        } catch (Exception e) {
            util_FormatOutput.printError(e.getMessage());
        }
    }

    private static void processPA(String[] args) throws IOException {
        String input = args[0]; // the input file
        String output = args[1]; // the output file

        String PASelector = args.length >= 3 ? args[2] : ""; // Which PA?

        switch (PASelector) {
            case "-a" -> {
                // PA2 PA service factory

                SongUserServicePA2 SongUserServicePA2 = new SongUserServicePA2();
                SongUserServicePA2.loadAndProcessFileData(input);
                SongUserServicePA2.outputUsersSongsRatingsCSV(output);
            }
            case "-u" -> {
                // PA3
                SongUserServicePA3V2 SongUserServicePA3 = new SongUserServicePA3V2();
                SongUserServicePA3.loadAndProcessData(input);
                SongUserServicePA3.outputUsersSongsRatingsCSV(output);
            }
            // case "-u" -> {
            // // PA4
            // SongUserServicePA4 SongUserServicePA4 = new SongUserServicePA4();
            // SongUserServicePA4.loadAndProcessDataUserSimilarity(input);
            // SongUserServicePA4.outputUsersSongsRatingsCSV(output);
            // }
            case "-p" -> {
                SongUserServicePA4V2 SongUserServicePA4V2 = new SongUserServicePA4V2();
                SongUserServicePA4V2.loadAndProcessDataPrediction(input, true);
                SongUserServicePA4V2.outputUsersSongsRatingPredictionsCSV(output);
            }
            case "-r" -> {
                String[] userSongSelection = new String[args.length - 3];

                // https://www.geeksforgeeks.org/java/arrays-copyof-in-java-with-examples/
                System.arraycopy(args, 3, userSongSelection, 0, args.length - 3); // copy args to userSongSelection

                SongUserServicePA5 SongUserServicePA5 = new SongUserServicePA5();
                SongUserServicePA5.loadAndProcessDataUserClustering(input, userSongSelection);
                SongUserServicePA5.outputUserChoiceRecommendations(output);
            }
            case "-s" -> {
                int kNumOfCentroids = Integer.parseInt(args[3]);

                String[] userSongSelection = new String[args.length - 4];

                // https://www.geeksforgeeks.org/java/arrays-copyof-in-java-with-examples/
                System.arraycopy(args, 4, userSongSelection, 0, args.length - 4); // copy args to userSongSelection

                SongUserServicePA6 SongUserServicePA6 = new SongUserServicePA6();

                SongUserServicePA6.loadAndProcessDataUserClustering(input, userSongSelection, kNumOfCentroids);
                SongUserServicePA6.outputUserChoiceRecommendations(output);
            }
            default -> {
                // PA1
                SongUserServicePA1 SongUserServicePA1 = new SongUserServicePA1();
                TreeMap<String, data_Song> songList = SongUserServicePA1.loadSongsandUsersFromCSV(input);
                SongUserServicePA1.outputUsersSongsRatingsCSV(output, songList);
            }
        }
    }

    /**
     * Calls methods to validate arguments passed in by user,
     * 
     * @param args User input.
     */
    public static void validateArguments(String[] args) {
        validateFirstTwoArguments(args);

        if (args.length == 3) {
            validateThreeArgMenu(args);
        } else if (args.length > 3) {
            validateMoreThanThreeArgMenu(args);
        }
    }

    public static void validateThreeArgMenu(String[] args) {
        if (args[2].equals("-r")) {
            throw new IllegalArgumentException("must select at least one song for recommendations");
        }

        ArrayList<String> commands = new ArrayList<>(Arrays.asList("-a", "-u", "-p", ""));
        if (!commands.contains(args[2])) {
            throw new IllegalArgumentException("\"-a\" or \"-u\" or \"-p\"  required for PA2 or PA4.");
        }
    }

    public static void validateMoreThanThreeArgMenu(String[] args) {
        ArrayList<String> commands = new ArrayList<>(Arrays.asList("-r", "-s"));
        if (!commands.contains(args[2])) {
            throw new IllegalArgumentException("\"-r\" or \"-s\" required for PA5 or PA6.");
        }

        if (args[2].equals("-s") && args.length < 5) {
            throw new IllegalArgumentException("-s requires minimum of 5 arguments." + args.length + " passed in.");
        }
    }

    /**
     * Checks if there are at least 2 or three arguments given.
     * 
     * @param args User input.
     */
    public static void validateFirstTwoArguments(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(args.length + " is out of bounds. Requires at least two arguments.");
        }

        if (util_FormatOutput.isNullOrEmpty(args[0])) {
            throw new IllegalArgumentException("Arguments 1 is required. It must include file input path.");
        }

        if (util_FormatOutput.isNullOrEmpty(args[1])) {
            throw new IllegalArgumentException("Arguments 2 is required. It must include output file.");
        }
    }

}
