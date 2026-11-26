package il.ac.tau.cs.software1.bigram;

import java.io.IOException;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class BigramModel {
    public static final int MAX_VOCABULARY_SIZE = 14500;
    public static final String VOC_FILE_SUFFIX = ".voc";
    public static final String COUNTS_FILE_SUFFIX = ".counts";
    public static final String SOME_NUM = "some_num";
    public static final int ELEMENT_NOT_FOUND = -1;

    String[] mVocabulary;
    int[][] mBigramCounts;

    // DO NOT CHANGE THIS !!!
    public void initModel(String fileName) throws IOException {
        mVocabulary = buildVocabularyIndex(fileName);
        mBigramCounts = buildCountsArray(fileName, mVocabulary);
    }

    //////////////////////////////////////////////////////////////////////////
    // Helper methods - already implemented for you. Use them!
    //////////////////////////////////////////////////////////////////////////

    /*
     * Converts a single token (a whitespace-free piece of text from the file)
     * into the form in which it is stored in the vocabulary:
     * - a token with at least one English letter -> the token in lowercase
     * - a token made only of digits (an integer) -> SOME_NUM
     * - anything else is not a legal word -> null
     *
     * @pre: token != null
     */
    public static String normalizeWord(String token) {
        boolean hasLetter = false;
        boolean allDigits = !token.isEmpty();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            }
            if (c < '0' || c > '9') {
                allDigits = false;
            }
        }
        if (hasLetter) {
            return token.toLowerCase(Locale.ROOT);
        }
        if (allDigits) {
            return SOME_NUM;
        }
        return null;
    }

    /*
     * @pre: vocabulary != null
     * 
     * @post: $ret = the index of word in vocabulary, or ELEMENT_NOT_FOUND if
     * word is null or does not appear in it
     */
    public static int findIndex(String[] vocabulary, String word) {
        if (word == null) {
            return ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < vocabulary.length; i++) {
            if (word.equals(vocabulary[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    //////////////////////////////////////////////////////////////////////////
    // Questions 1 - 5: implement these
    //////////////////////////////////////////////////////////////////////////

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * 
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public String[] buildVocabularyIndex(String fileName) throws IOException { // Question 1
        String[] tempVocab = new String[MAX_VOCABULARY_SIZE];
        int count = 0;
        FileReader fromFile = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fromFile);
        String line = br.readLine();
        while (line != null && count < MAX_VOCABULARY_SIZE) {
            String[] tokens = line.split("\\s+");
            for (int t = 0; t < tokens.length && count < MAX_VOCABULARY_SIZE; t++) {
                String normalized = normalizeWord(tokens[t]);
                if (normalized != null) {
                    boolean exists = false;
                    for (int i = 0; i < count && !exists; i++) {
                        if (tempVocab[i].equals(normalized)) {
                            exists = true;
                        }

                    }
                    if (!exists) {
                        tempVocab[count] = normalized;
                        count++;
                    }
                }
            }
            line = br.readLine();
        }
        br.close();
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = tempVocab[i];
        }
        return result;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * 
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int[][] counts = new int[vocabulary.length][vocabulary.length];
        FileReader fromFile = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fromFile);
        String line = br.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            int prevIdx = ELEMENT_NOT_FOUND;

            for (int t = 0; t < tokens.length; t++) {
                String normalized = normalizeWord(tokens[t]);
                int currIdx = findIndex(vocabulary, normalized);

                if (prevIdx != ELEMENT_NOT_FOUND && currIdx != ELEMENT_NOT_FOUND) {
                    counts[prevIdx][currIdx]++;
                }

                prevIdx = currIdx;
            }

            line = br.readLine();
        }
        br.close();
        return counts;

    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        FileWriter vocFile = new FileWriter(fileName + VOC_FILE_SUFFIX);
        BufferedWriter vocWriter = new BufferedWriter(vocFile);

        vocWriter.write(mVocabulary.length + " words");
        vocWriter.newLine();

        for (int i = 0; i < mVocabulary.length; i++) {
            vocWriter.write(i + "," + mVocabulary[i]);
            vocWriter.newLine();
        }
        vocWriter.close();

        FileWriter countsFile = new FileWriter(fileName + COUNTS_FILE_SUFFIX);
        BufferedWriter countsWriter = new BufferedWriter(countsFile);

        for (int i = 0; i < mBigramCounts.length; i++) {
            for (int j = 0; j < mBigramCounts[i].length; j++) {
                if (mBigramCounts[i][j] > 0) {
                    countsWriter.write(i + "," + j + ":" + mBigramCounts[i][j]);
                    countsWriter.newLine();
                }
            }
        }
        countsWriter.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        FileReader vocFile = new FileReader(fileName + VOC_FILE_SUFFIX);
        BufferedReader vocReader = new BufferedReader(vocFile);

        String firstLine = vocReader.readLine();
        String[] headerParts = firstLine.split("\\s+");
        int numWords = Integer.parseInt(headerParts[0]);

        mVocabulary = new String[numWords];

        String line = vocReader.readLine();
        while (line != null) {
            String[] parts = line.split(",", 2);
            int idx = Integer.parseInt(parts[0]);
            mVocabulary[idx] = parts[1];
            line = vocReader.readLine();
        }
        vocReader.close();

        // 2. קריאת קובץ הספירות (.counts)
        mBigramCounts = new int[numWords][numWords];

        FileReader countsFile = new FileReader(fileName + COUNTS_FILE_SUFFIX);
        BufferedReader countsReader = new BufferedReader(countsFile);

        line = countsReader.readLine();
        while (line != null) {
            String[] parts = line.split(":");
            String[] indices = parts[0].split(",");

            int firstIdx = Integer.parseInt(indices[0]);
            int secondIdx = Integer.parseInt(indices[1]);
            int count = Integer.parseInt(parts[1]);

            mBigramCounts[firstIdx][secondIdx] = count;
            line = countsReader.readLine();
        }
        countsReader.close();
    }

    /*
     * @pre: sentence is in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @pre: each two words in the sentence are separated with a single space
     * 
     * @post: if sentence is probable, according to the model, $ret = true, else,
     * $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        if (sentence.length() == 0) {
            return true;
        }

        String[] words = sentence.split(" ");

        if (words.length == 1) {
            return findIndex(mVocabulary, words[0]) != ELEMENT_NOT_FOUND;
        }

        for (int i = 0; i < words.length - 1; i++) {
            int firstIdx = findIndex(mVocabulary, words[i]);
            int secondIdx = findIndex(mVocabulary, words[i + 1]);

            if (firstIdx == ELEMENT_NOT_FOUND || secondIdx == ELEMENT_NOT_FOUND) {
                return false;
            }

            if (mBigramCounts[firstIdx][secondIdx] == 0) {
                return false;
            }
        }

        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    // Queries on the model - already implemented for you.
    // They start working once Questions 1 - 2 are done.
    //////////////////////////////////////////////////////////////////////////

    /*
     * @pre: word is in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = -1 if word is not in vocabulary, otherwise $ret = the index of
     * word in vocabulary
     */
    public int getWordIndex(String word) {
        return findIndex(mVocabulary, word);
    }

    /*
     * @pre: word1, word2 are in lowercase
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = the count for the bigram <word1, word2>. if one of the words
     * does not
     * exist in the vocabulary, $ret = 0
     */
    public int getBigramCount(String word1, String word2) {
        int index1 = getWordIndex(word1);
        int index2 = getWordIndex(word2);
        if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
            return 0;
        }
        return mBigramCounts[index1][index2];
    }

    /*
     * @pre: word is in lowercase, and is in mVocabulary
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = the word with the lowest vocabulary index that appears most
     * frequently after word
     * (if a bigram starting with word was never seen, $ret will be null)
     */
    public String getMostFrequentProceeding(String word) {
        int[] row = mBigramCounts[getWordIndex(word)];
        int best = ELEMENT_NOT_FOUND;
        for (int i = 0; i < row.length; i++) {
            if (row[i] > 0 && (best == ELEMENT_NOT_FOUND || row[i] > row[best])) {
                best = i;
            }
        }
        return best == ELEMENT_NOT_FOUND ? null : mVocabulary[best];
    }

    /*
     * @pre: arr1.length = arr2.length
     * 
     * @post: if arr1 or arr2 are only filled with zeros, $ret = -1, otherwise
     * calculates CosineSim
     */
    public static double calcCosineSim(int[] arr1, int[] arr2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < arr1.length; i++) {
            dot += (double) arr1[i] * arr2[i];
            norm1 += (double) arr1[i] * arr1[i];
            norm2 += (double) arr2[i] * arr2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return -1;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /*
     * @pre: word is in vocabulary
     * 
     * @pre: the method initModel was called (the language model is initialized)
     * 
     * @post: $ret = w implies that w is the word with the largest
     * cosineSimilarity(vector for word, vector for w)
     * among all the other words in vocabulary (the lowest index wins a tie)
     */
    public String getClosestWord(String word) {
        int wordIndex = getWordIndex(word);
        int best = ELEMENT_NOT_FOUND;
        double bestSim = 0;
        for (int i = 0; i < mVocabulary.length; i++) {
            if (i == wordIndex) {
                continue;
            }
            double sim = calcCosineSim(mBigramCounts[wordIndex], mBigramCounts[i]);
            if (best == ELEMENT_NOT_FOUND || sim > bestSim) {
                best = i;
                bestSim = sim;
            }
        }
        // A vocabulary with a single word: the closest word is the word itself
        return best == ELEMENT_NOT_FOUND ? word : mVocabulary[best];
    }
}
