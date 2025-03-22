import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Beckett Porter
 */
public class Autocorrect {

    private static int threshold;
    private static String[] dictionary;
    private static final int RADIX = 16;
    private static final int MAX_N_GRAM_TO_CHECK = 3;
    private static final int MIN_N_GRAM_TO_CHECK = 2;
    private static final int MAX_MATCHES_TO_PRINT = 5;
    private static final int EDIT_DISTANCE_THRESHOLD = 3;
    private static final int MAX_CANDIDATE_LENGTH_DIFFERENCE = 3;
    private static final int NUM_POSSIBLE_CANDIDATES_TO_FIND = 400;
    private static final String DICTIONARY_TO_USE = "large";
    private static String misspelledWord;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold)
    {
        Autocorrect.threshold = threshold;
        dictionary = words;
    }

    public static void main(String[] args)
    {
        new Autocorrect(loadDictionary(DICTIONARY_TO_USE), EDIT_DISTANCE_THRESHOLD);
        run();
    }

    private static void run()
    {
        while (true)
        {
            // Get user input for their word, exit if they type 'exit'.
            Scanner s = new Scanner(System.in);

            System.out.println("Enter a word: ");
            misspelledWord = s.nextLine();

            if (misspelledWord.equals("exit"))
            {
                return;
            }


            // Also, this profiler is a tool I made (with the help of chatgpt) that tells me the time it takes for a
            // given segment of code to run. I used this to find the bottleneck that was slowing down the algorithm.
            // Feel free to uncomment both the start profiler and end profiler lines to see it in action.
//            Profiler.start("GetCandidates");

            // Get similar dictionary words to the user's misspelled word.
            String[] candidates = getPossibleCandidates();
//            Profiler.end("GetCandidates");

//            Profiler.start("EditDistances");
            // Go through these possibly similar dictionary words and add the ones with an edit distance below
            // the threshold to a Hashmap. This Hashmap has the String as the key and the value as an
            // Integer representing the editDistance for that string.
            HashMap<String, Integer> confirmedMatchesEditDistances = new HashMap<>();
            for (String candidate : candidates)
            {
                int editDistance = findEditDistance(candidate, misspelledWord, 0, 0);
                if (editDistance < threshold)
                {
                    confirmedMatchesEditDistances.put(candidate, editDistance);
                }
            }
//            Profiler.end("EditDistances");

            // Using the confirmedMatchesEditDistances Hashmap, we can now sort the strings from the
            // lowest edit distance to highest. This allows us to show the user the most likely words they
            // meant to say first before less likely ones.

            // Found this online, sorts a hashMap by their values from lowest to highest.
            ArrayList<String> sortedMatches = new ArrayList<>(confirmedMatchesEditDistances.keySet());
            sortedMatches.sort(Comparator.comparing(confirmedMatchesEditDistances::get));

            // Go through and print out the sorted matches if we have any, otherwise print that we didn't find any.
            if (!sortedMatches.isEmpty())
            {
                // Check to make sure the first match (which is going to be most similar to the target word) is equal
                // to the target word. This would mean that the word is not misspelled.
                if (!sortedMatches.getFirst().equals(misspelledWord))
                {
                    // Print out MAX_MATCHES_TO_PRINT number of the most similar matches.
                    int numMatchesPrinted = 0;
                    for (String sortedMatch : sortedMatches)
                    {
                        if (numMatchesPrinted++ < MAX_MATCHES_TO_PRINT)
                        {
                            System.out.println(sortedMatch);
                        }
                    }
                }
            }
            else
            {
                System.out.println("No matches found.");
            }

            System.out.println("----------------------");
        }
    }

    // Return the most similar dictionary words to the misspelled word.
    // NUM_POSSIBLE_CANDIDATES_TO_FIND determines the number of words we return.
    private static String[] getPossibleCandidates()
    {
        // Create a hashMap where keys are longs representing hashed nGrams, the values are arrayLists of
        // integers that correspond to the index in the dictionary array of the word that the nGram came from.
        HashMap<Long, ArrayList<Integer>> dictionaryWordNGramHashes = new HashMap<>();

        // This goes through and hashes all the n-grams in the dictionary words. It adds the
        // dictionary index of the word that the hash came from as the value associated with the hash's key.
        for (int i = 0; i < dictionary.length; i++)
        {
            String currentWord = dictionary[i];

            // Generate hashes for the minimum nGram length to the maximum nGram length.
            for (int nGramSize = MIN_N_GRAM_TO_CHECK; nGramSize < MAX_N_GRAM_TO_CHECK; nGramSize++)
            {
                // Go through the currentWord in chunks of nGramSize to hash these nGrams.
                for (int j = 0; j < currentWord.length(); j += nGramSize)
                {
                    String currentSubstring;
                    long currentSubstringHash;

                    if (j + nGramSize <= currentWord.length())
                    {
                        currentSubstring = currentWord.substring(j, j + nGramSize);
                    }
                    else
                    {
                        currentSubstring = currentWord.substring(j);
                    }

                    currentSubstringHash = hashSingleString(currentSubstring);

                    // If that spot in the hashmap doesn't have anything in it yet, initialize the arraylist.
                    dictionaryWordNGramHashes.putIfAbsent(currentSubstringHash, new ArrayList<>());

                    // Add the index of the currentWord to the hash location's arraylist in the hashSet.
                    dictionaryWordNGramHashes.get(currentSubstringHash).add(i);
                }
            }
        }

        // Make an arrayList of hashes for nGrams that make up the misspelled word.
        ArrayList<Long> misspelledWordHashes = new ArrayList<>();

        // Then go through and hash nGrams of the misspelled word, these will later
        // be used to compare to the hashes we found from dictionary word nGrams.
        for (int nGramSize = MIN_N_GRAM_TO_CHECK; nGramSize < MAX_N_GRAM_TO_CHECK; nGramSize++)
        {
            for (int j = 0; j < misspelledWord.length(); j += nGramSize)
            {
                String currentSubstring;
                if (j + nGramSize <= misspelledWord.length())
                {
                    currentSubstring = misspelledWord.substring(j, j + nGramSize);
                }
                else
                {
                    currentSubstring = misspelledWord.substring(j);
                }
                misspelledWordHashes.add(hashSingleString(currentSubstring));
            }
        }

        // Now, make a hashMap with the keys being the word's index in the dictionary and the values being the
        // number of matching nGram hashes to the misspelled word's nGram hashes.
        HashMap<Integer, Integer> appearances = new HashMap<>();
        for (Long misspelledWordHash : misspelledWordHashes)
        {
            // If the given hash has any dictionary words that have similar nGrams.
            if (dictionaryWordNGramHashes.get(misspelledWordHash) != null)
            {
                // Get the arrayList of integers representing the index of words in the dictionary that had the same
                // hashed nGram values as the misspelled word's nGrams (that's a mouthful ik sorry).
                for (int i = 0; i < dictionaryWordNGramHashes.get(misspelledWordHash).size(); i++)
                {
                    int dictionaryWord = dictionaryWordNGramHashes.get(misspelledWordHash).get(i);

                    // Increment the num appearances at the dictionary word. If there haven't been any appearances
                    // at that spot yet, we need to put a 1 there to start the count.
                    if (appearances.putIfAbsent(dictionaryWord, 1) != null)
                    {
                        appearances.put(dictionaryWord, appearances.get(dictionaryWord) + 1);
                    }
                }
            }
        }

        // I found this online, I needed a way to sort the hashmap by values (Highest to lowest # of appearances)
        ArrayList<Integer> mostSimilarWords = new ArrayList<>(appearances.keySet());
        mostSimilarWords.sort((idx1, idx2) -> appearances.get(idx2).compareTo(appearances.get(idx1)));

        // Make a String array that we will fill with the found candidates.
        String[] mostSimilarWordsArrays = new String[Autocorrect.NUM_POSSIBLE_CANDIDATES_TO_FIND];
        for (int i = 0; i < Autocorrect.NUM_POSSIBLE_CANDIDATES_TO_FIND; i++)
        {
            mostSimilarWordsArrays[i] = dictionary[mostSimilarWords.get(i)];
        }

        // Cull words that are too different in length and return the similar words that pass this check. This
        // drastically speeds up the time it takes for longer words to get autocorrected.
        return cullVastlyDifferentSizedWords(mostSimilarWordsArrays, misspelledWord.length(),
                MAX_CANDIDATE_LENGTH_DIFFERENCE);
    }

    // Helper method that returns an array with only words in it that have a length distance
    // of maxDifference or less to the targetWordLength.
    private static String[] cullVastlyDifferentSizedWords(String[] inputAr, int targetWordLength, int maxDifference)
    {
        ArrayList<String> matchesArrayList = new ArrayList<>();
        for (String s : inputAr)
        {
            if (Math.abs(s.length() - targetWordLength) < maxDifference)
            {
                matchesArrayList.add(s);
            }
        }
        String[] matchesArray = new String[matchesArrayList.size()];
        return matchesArrayList.toArray(matchesArray);
    }

    // Helper method that hashes a single string using the Rabin-Karp algorithm.
    private static long hashSingleString(String str)
    {
        int hash = 0;

        for (int i = 0; i < str.length(); i++)
        {
            hash = (hash * RADIX + str.charAt(i));
        }
        return hash;
    }

    // Find the minimum number of edits to turn word1 into word2 (or vice versa).
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