import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 * @author Siddhartha Sitaula
 * A classic game of hangman where the game is fair
 * the game proceeds as usaul in the hangman game where each wrong guesses
 * starts making a form of the hangman.
 * this class implements the HangmanInterface
 * */
public class FairHangman implements HangmanInterface {

    protected Set<String> dictionary = new HashSet<>();
    protected Set<Character> guessedLetters = new TreeSet<>();
    protected int guessesRemaining;
    protected String secretWord;
    protected StringBuilder puzzle;
    /**
     * Constructing a FairHangman game and
     * loading the dictionary from a file.
     *
     * @param filename path of dictionary file
     */
    public FairHangman(String filename) {
        loadDictionary(filename);
    }
    /**
     * This helps load all words from dictionary file inside
     * the   dictionary set.
     *
     * @param filename  dictionar y file to load
     */
    private void loadDictionary(String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            while(input.hasNext()) {
                dictionary.add(input.next().toLowerCase());
            }
        } catch(FileNotFoundException e) {
            System.err.println("Dictionary file not found");
        }
    }

    @Override
    /**
     * Is starting a new game.
     * A random word is chosen from the dictionary and
     * the puzzle is being reset.
     *
     * @param guesses the number of incorrect guesses which is
     *               allowed
     */
    public void initGame(int guesses) {
        guessesRemaining = guesses;
        guessedLetters.clear();

        List<String> words = new ArrayList<>(dictionary);
        secretWord = words.get(new Random().nextInt(words.size()));

        puzzle = new StringBuilder();
        for(int i = 0; i < secretWord.length(); i++)
            puzzle.append("-");
    }

    @Override
    /**
     * Is updating the puzzle
     * as per gamer guess
     * if the letter is in the word the position is also shown.
     * if not the hangman starts and guesses decreases.
     *
     * @param letter the letter guessed by the player
     * @return true if the letter is in the word
     * esle false
     */
    public boolean updateWithGuess(char letter) {
        letter = Character.toLowerCase(letter);
        if(guessedLetters.contains(letter)) {
            return true;
        }
        guessedLetters.add(letter);

        boolean found = false;
        for(int i = 0; i < secretWord.length(); i++) {
            if(secretWord.charAt(i) == letter) {
                puzzle.setCharAt(i, letter);
                found = true;
            }
        }
        if(!found) guessesRemaining--;
        return found;
    }

    @Override
    /**
     * gives true if the player has successfully
     * guessed the word
     *
     * @return true if the puzzle is complete,
     * else false
     */
    public boolean isComplete() {
        return puzzle.toString().equals(secretWord);
    }

    @Override
    /**
     * IS checking whether the game is over (either win or lose).
     *
     * @return true if no guesses remain or the puzzle is complete
     */
    public boolean isGameOver() {
        return guessesRemaining <= 0 || isComplete();
    }

    @Override
    /**
     * Gets the current state of the puzzle with
     * dashes for non hguessed letters.
     *
     * @return the puzzle string
     */
    public String getPuzzle() {
        return puzzle.toString();
    }

    @Override
    /**
     * IS returning the secret word for this game.
     * @return the secret word
     */
    public String getSecretWord() {
        return secretWord;
    }

    @Override
    /**
     * IS returning all letters that have been guessed as
     * per game.
     *
     * @return  guessed characters
     */
    public Set<Character> getGuessedLetters() {
        return guessedLetters;
    }

    @Override
    /**
     * Is returning the number of
     * left guesses
     *
     * @return the left guesses
     */
    public int getGuessesRemaining() {
        return guessesRemaining;
    }
}
