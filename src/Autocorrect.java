import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
    private static String[] dictionary;
    public static final int RADIX = 16;
    public static String misspelledWord;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold)
    {
        this.threshold = threshold;
        dictionary = words;

        misspelledWord = "Alphabte";
    }

    public static void main(String[] args)
    {
        Autocorrect auto = new Autocorrect(loadDictionary("large"), 5);
        auto.run();
    }

    private static void run()
    {
        while (true)
        {
            Scanner s = new Scanner(System.in);

            System.out.println("Enter a word plz: ");
            misspelledWord = s.nextLine();
            String[] candidates = getPossibleCandidates(100);

            int lowestEditDistance = Integer.MAX_VALUE;
            String mostSimilarWord = "";

            for (String candidate : candidates)
            {
                int l = findEditDistance(candidate, misspelledWord, 0, 0);
                if (l < lowestEditDistance)
                {
                    lowestEditDistance = l;
                    mostSimilarWord = candidate;
                }
            }

            System.out.println(mostSimilarWord);

        }
    }

    private static String[] getPossibleCandidates(int numCandidatesToGet)
    {

        HashMap<Long, ArrayList<Integer>> hashMap = new HashMap<>();
        int nGramSize = 2;

        // Arraylist (dictionary size) of Arraylists (n -gram)

        // This goes through and hashes all the n-grams in the dictionary words. It adds the
        // dictionary index of the word that the hash came from to the hash's spot in the array.
        for (int i = 0; i < dictionary.length; i++)
        {
            String currentWord = dictionary[i];

            for (int j = 0; j < currentWord.length(); j += nGramSize)
            {
                if (j + nGramSize <= currentWord.length())
                {
                    String currentSubstring = currentWord.substring(j, j + nGramSize);

                    long currentSubstringHash = hashSingleString(currentSubstring);

                    // If that spot in the hashmap doesn't have anything in it yet, initialize the arraylist.
                    hashMap.putIfAbsent(currentSubstringHash, new ArrayList<>());

                    // Add the index of the currentWord to the hash location's arraylist in the hashSet.
                    hashMap.get(currentSubstringHash).add(i);
                }
            }
        }


        // Now I need to hash the mistyped word and get its ngram hashes. Then I need to get num of
        // appearances of a given dictionary word with similar hashes.


        // Make this n-gram hasher modular in the future

        ArrayList<Long> misspelledWordHashes = new ArrayList<>();
        for (int j = 0; j < misspelledWord.length(); j += nGramSize)
        {
            if (j + nGramSize <= misspelledWord.length())
            {
                String currentSubstring = misspelledWord.substring(j, j + nGramSize);
                misspelledWordHashes.add(hashSingleString(currentSubstring));
            }
        }


        // 1st int = word index, 2nd int = num appearances.
        HashMap<Integer, Integer> appearances = new HashMap<>();
        for (Long hash : misspelledWordHashes)
        {
            if (hashMap.get(hash) != null)
            {
                for (int i = 0; i < hashMap.get(hash).size(); i++)
                {
                    // I want th
                    int currentSpot = hashMap.get(hash).get(i);

                    // Increment the num appearances at the spot

                    appearances.putIfAbsent(currentSpot, 1);

                    appearances.put(currentSpot, appearances.get(currentSpot) + 1);
                }
            }
        }


        // I Found this online, needed a way to sort the hashmap by values (highest to lowest # of appearances)
        // Sort keys based on their values (frequencies) in descending order

        ArrayList<Integer> mostSimilarWords = new ArrayList<>(appearances.keySet());
        mostSimilarWords.sort((idx1, idx2) -> appearances.get(idx2).compareTo(appearances.get(idx1)));

        String[] mostSimilarWordsArrays = new String[100];
        for (int i = 0; i < numCandidatesToGet; i++)
        {
            mostSimilarWordsArrays[i] = dictionary[mostSimilarWords.get(i)];
        }

        return mostSimilarWordsArrays;
    }

// Helper method that hashes a single string using the Rabin-Karp algorithm.
    private static long hashSingleString(String str)
    {
        int hash = 0;

        for (int i = 0; i < str.length(); i++)
        {
            hash = (hash * RADIX + str.charAt(i)) % 1610612741;
        }
        return hash;
    }

    // We are trying to turn word 1 into word 2
    private static int findEditDistance(String word1, String word2, int index1, int index2)
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