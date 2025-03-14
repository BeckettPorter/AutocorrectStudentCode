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
    public static String targetWord;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold)
    {
        this.threshold = threshold;
        this.dictionary = words;

        this.targetWord = "Algorithmm";

//        runTest("");
    }

    public static void main(String[] args)
    {

    }

    private String[] getPossibleCandidates(int numCandidatesToGet)
    {

        ArrayList<Long>[] array = new ArrayList[dictionary.length];
        int nGramSize = 2;

        // Arraylist (dictionary size) of Arraylists (n -gram)

        for (int i = 0; i < dictionary.length; i++)
        {
            String currentWord = dictionary[i];

            array[i] = new ArrayList<>();

            for (int j = 0; j < currentWord.length(); j += nGramSize)
            {
                String currentSubstring = currentWord.substring(j, j + nGramSize);

                array[i].add(hashSingleString(currentSubstring));
            }

        }

        for (int i = 0; i < dictionary.length; i++)
        {

            if ()
            array[i]
        }

        return
    }

// Helper method that hashes a single string using the Rabin-Karp algorithm.
    private long hashSingleString(String str)
    {
        int hash = 0;

        for (int i = 0; i < str.length(); i++)
        {
            hash = (hash * RADIX + str.charAt(i)) % 1610612741;
        }
        return hash;
    }

    // We are trying to turn word 1 into word 2
    private int findEditDistance(String word1, String word2, int index1, int index2)
    {
        // Base cases that check if we have reached the end of either word. If we have, then we add
        // the difference between the other word's length and what index it is on to the returned edit distance.
        if (index1 == word1.length())
        {
            return word2.length() - index2;
        }
        if (index2 == word2.length())
        {
            return word1.length() - index1;
        }

        char char1 = word1.charAt(index1);
        char char2 = word2.charAt(index2);

        // We have three possible choices, we can add a letter, remove a letter, or replace a letter.
        if (char1 != char2)
        {
            // replace
            int replaceCount = 1 + findEditDistance(word1, word2, index1 + 1, index2 + 1);

            // add
            int addCount = 1 + findEditDistance(word1, word2, index1, index2 + 1);

            // remove
            int removeCount = 1 + findEditDistance(word1, word2, index1 + 1, index2);

            // Return the lowest we've found.
            return Math.min(Math.min(replaceCount, addCount), removeCount);
        }
        else
        {
            // Otherwise if the two chars are equal, just increment both indexes.
            return findEditDistance(word1, word2, index1 + 1, index2 + 1);
        }
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        ArrayList<String> potentialWordsAr = new ArrayList<>();

        // Go through each word in the dictionary and add it to the arrayList if it is within
        // the threshold for edit distance.
        for (String s : dictionary)
        {
            if (findEditDistance(s, typed, 0, 0) <= threshold)
            {
                potentialWordsAr.add(s);
            }
        }

        // Code I got online that sorts my arraylist by edit distance and alphabetically.
        potentialWordsAr.sort((s1, s2) -> {
            int distance1 = findEditDistance(s1, typed, 0, 0);
            int distance2 = findEditDistance(s2, typed, 0, 0);
            return (distance1 != distance2) ? Integer.compare(distance1, distance2) : s1.compareTo(s2);
        });


        // Convert the arrayList to a normal string array and return it.
        String[] potentialWords = new String[potentialWordsAr.size()];

        potentialWordsAr.toArray(potentialWords);

        return potentialWords;
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