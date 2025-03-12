import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Beckett
 */
public class Autocorrect {

    private final int threshold;
    private final String[] dictionary;
    public static final int RADIX = 16;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold)
    {
        this.threshold = threshold;
        this.dictionary = words;

        runTest("");

    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed)
    {
        ArrayList<String> ar = new ArrayList<>();

        for (String s : dictionary)
        {
            if (findEditDistance(s, typed, 0) <= threshold)
            {
                ar.add(s);
            }
        }

        String[] potentialWords = new String[ar.size()];

        ar.toArray(potentialWords);

        return potentialWords;
    }

//    private String[] getPossibleCandidates(int numCandidatesToGet)
//    {
//
//        ArrayList<Long> ar = new ArrayList<>();
//
//        // Arraylist (dictionary size) of Arraylists (n -gram)
//
//
//        return
//    }
//
// Helper method that hashes a single string using the Rabin-Karp algorithm.
//    private long hashSingleString(String str)
//    {
//        int hash = 0;
//
//        for (int i = 0; i < str.length(); i++)
//        {
//            hash = (hash * RADIX + str.charAt(i)) % 1610612741;
//        }
//        return hash;
//    }

    // We are trying to turn word 1 into word 2
    private int findEditDistance(String word1, String word2, int index)
    {
        if (index >= word1.length() || index >= word2.length())
        {
            return Math.abs(word1.length() - word2.length());
        }

        char char1 = word1.charAt(index);
        char char2 = word2.charAt(index);

        // We have three possible choices, we can add a letter, remove a letter, or replace a letter.

        // First, given a char, we have to see which options we can do.

        if (char1 != char2)
        {
            // REPLACE
            String replaceString = word1.substring(0, index) + char2 + word1.substring(index + 1);

            int replaceCount = findEditDistance(replaceString, word2, index + 1);

            // ADD
            String addString = word1.substring(0, index) + char2 + word1.substring(index);

            int addCount = findEditDistance(addString, word2, index + 1);

            // REMOVE
            String removeString = word1.substring(0, index) + word1.substring(index + 1);

            int removeCount = findEditDistance(removeString, word2, index + 1);


            // Return the lowest we've found.
            return 1 + Math.min(Math.min(replaceCount, addCount), removeCount);
        }
        else
        {
            return findEditDistance(word1, word2, index + 1);
        }
    }

















    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries' directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries' directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}