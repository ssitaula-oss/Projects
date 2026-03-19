import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 * @author Siddhartha Sitaula
 * This code implements the evil hangman where the computer takes time
 * giving the secret word
 * To prevent letters from being revealed, the program
 * continually changes word families from a list of all possible
 *  valid words.
 *this makes the game a lot harder
 *
 * This class implements the HangmanInterface.
 */
public class EvilHangman implements HangmanInterface {

    private Set<String> words = new HashSet<>();
    private Set<Character> guessedLetters = new TreeSet<>();
    private int guessesRemaining;
    private String pattern;
    private String finalWord = null;
    /**
     * Is constructing a  a new EvilHangman game from a dictionary file.
     *
     * @param filename the name of the dictionary file
     */
    public EvilHangman(String filename) {
        loadDictionary(filename);
    }
    /**
     * Is loading all words from a dictionary file.
     *
     * @param filename dictionary file
     */
    private void loadDictionary(String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            while(input.hasNext()) {
                words.add(input.next().toLowerCase());
            }
        } catch(FileNotFoundException e) {
            System.err.println("Dictionary file not found.");
        }
    }

    @Override
    /**
     * Is staring a new game
     *
     * @param guesses number of incorrect guesses allowed
     */
    public void initGame(int guesses) {
        guessesRemaining = guesses;
        guessedLetters.clear();

        Iterator<String> it = words.iterator();
        int length = it.next().length();
        words.removeIf(word -> word.length() != length);

        pattern = "-".repeat(length);
        finalWord = null;
    }

    /**
     * Is updating the game state based on the given guess.
     * instead of revelaing the guessed correct letters it takes the guess letter
     * and pulls it into the family to make the game harder for player.
     * @param letter the character guessed by the player
     * @return true if the letter appears as the player wants
     * esle false
     */
    @Override
    public boolean updateWithGuess(char letter) {
        letter = Character.toLowerCase(letter);

        if(guessedLetters.contains(letter)) return true;
        guessedLetters.add(letter);

        Map<String, Set<String>> families = new HashMap<>();

        for(String w : words) {
            String key = makePattern(w, letter);
            families.putIfAbsent(key, new HashSet<>());
            families.get(key).add(w);
        }

        String bestPattern = pattern;
        int max = -1;
        for(String key : families.keySet()) {
            int size = families.get(key).size();
            if(size > max) {
                max = size;
                bestPattern = key;
            }
        }

        boolean found = !bestPattern.equals(pattern);
        pattern = bestPattern;
        words = families.get(pattern);

        if(pattern.indexOf(letter) == -1) guessesRemaining--;

        if(words.size() == 1 && (isComplete() || guessesRemaining == 0)) {
            finalWord = words.iterator().next();
        }
        return found;
    }
    /**
     * Builds a pattern word using the guessed letter and also
     * uses old pattern
     * @param w      a candidate word from the dictionary
     * @param letter the guessed letter
     * @return the pattern of the owrd
     */

    private String makePattern(String w, char letter) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < w.length(); i++) {
            if(w.charAt(i) == letter) sb.append(letter);
            else sb.append(pattern.charAt(i));
        }
        return sb.toString();
    }

    @Override
    public boolean isComplete() {
        return pattern.indexOf('-') == -1;
    }

    @Override
    public boolean isGameOver() {
        return guessesRemaining <= 0 || isComplete();
    }

    @Override
    public String getPuzzle() {
        return pattern;
    }

    @Override
    public String getSecretWord() {
        if(finalWord != null) return finalWord;
        return words.iterator().next();
    }

    @Override
    public Set<Character> getGuessedLetters() {
        return guessedLetters;
    }

    @Override
    public int getGuessesRemaining() {
        return guessesRemaining;
    }
}
