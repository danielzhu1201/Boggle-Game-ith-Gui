package assignment;

import static org.junit.Assert.*;

import java.awt.Point;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.xml.internal.ws.policy.spi.AssertionCreationException;

import assignment.BoggleDictionary;
import assignment.BoggleGame;
import assignment.GameDictionary;
import assignment.GameManager;
import assignment.BoggleGame.SearchTactic;

/**
 * A simple set of sanity tests to verify that your code 'kinda works'.
 * 
 * You may use this code as a reference when designing your own tests; however,
 * these tests by themselves are very basic and do not constitute effective
 * testing by themselves.
 */
public class BoggleTests {

	/**
	 * Our collection of test words, which will also become our dictionary.
	 */
	public static final TreeSet<String> WORDS = new TreeSet<>(
			Arrays.asList("hallowed", "hand", "handed", "hard", "hello", "help", "hurt"));

	/**
	 * The name of the generated dictionary file.
	 */
	public static final String DICTIONARY_NAME = "sanity_words.txt";

	/**
	 * An artifical board used for testing.
	 */
	public static final char[][] BOARD = new char[][] { { 'H', 'E', 'L', 'P' }, { 'A', 'A', 'R', 'P' },
			{ 'L', 'M', 'N', 'D' }, { 'L', 'O', 'W', 'E' } };
	public static final char[][] BOARD2 = new char[][] { { 'H', 'E', 'L', 'P', 'P' }, { 'A', 'A', 'R', 'P', 'P' },
			{ 'L', 'M', 'N', 'D', 'P' }, { 'L', 'O', 'W', 'E', 'P' }, { 'L', 'O', 'W', 'E', 'P' } };

	/**
	 * The actual words found on the board.
	 */
	public static final TreeSet<String> BOARD_WORDS = new TreeSet<>(Arrays.asList("hallowed", "hand", "hard", "help"));

	/**
	 * Creates a dictionary file from our words for testing purposes.
	 */
	@BeforeClass
	public static void createDictionaryFile() throws IOException {
		File file = new File(BoggleTests.DICTIONARY_NAME);

		try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
			for (String word : WORDS)
				writer.println(word);
		}
	}

	/**
	 * Cleans up the dictionary file we created earlier.
	 */
	@AfterClass
	public static void deleteDictionaryFile() throws IOException {
		File file = new File(BoggleTests.DICTIONARY_NAME);
		file.delete();
	}

	/**
	 * Tests that you can successfully load a dictionary and do some basic
	 * operations on it.
	 */
	@Test
	public void testFullDictionary() throws IOException {
		// loading dictionary; load 2nd time
		GameDictionary dict = new GameDictionary();
		dict.loadDictionary("words.txt");

		// edge cases for dict parameters
		assertFalse(dict.contains(""));
		assertTrue(dict.isPrefix(""));
		assertFalse(dict.contains(null));
		assertFalse(dict.isPrefix(null));

		// check contains with various inputs
		assertTrue(dict.contains("abas"));
		assertTrue(dict.contains("abAse"));
		assertTrue(dict.contains("abasED"));
		assertTrue(dict.contains("Abasedly   "));

		// check isPrefix with various inputs
		assertTrue(dict.isPrefix("a"));
		assertTrue(dict.isPrefix("ab "));
		assertTrue(dict.isPrefix("abA"));
		assertTrue(dict.isPrefix("abas "));
		assertTrue(dict.isPrefix("  aBase"));
		assertTrue(dict.isPrefix("abaSed"));
		assertTrue(dict.isPrefix("abasedl"));
		assertTrue(dict.isPrefix("abaSedly"));
		assertFalse(dict.contains("dict"));

		// loading SanityTest dict

		dict.loadDictionary(BoggleTests.DICTIONARY_NAME);

		// And check that it has 7 words in it, as expected.
		// Note that there is no gauruntee of the case of the words in the
		// dictionary, so we
		// manually lowercase them.
		Set<String> results = new TreeSet<>();
		for (String str : dict)
			results.add(str.toLowerCase());
		assertEquals(WORDS, results);

		// Test that contains appears to work, and ignores case.
		assertTrue(dict.contains("hallowed"));
		assertTrue(dict.contains("hAlLoWeD"));
		assertTrue(dict.contains("hello"));
		assertFalse(dict.contains("goodbye"));
		assertFalse(dict.contains("hel"));

		// Test that isPrefix appears to work, and ignores case.
		assertTrue(dict.isPrefix("h"));
		assertTrue(dict.isPrefix("HA"));
		assertTrue(dict.isPrefix("hEl"));
		assertFalse(dict.isPrefix("p"));
		assertFalse(dict.isPrefix("pa"));
		assertFalse(dict.isPrefix("rt"));

		// Edge Case: A word is considered a prefix of itself.
		assertTrue(dict.isPrefix("hallowed"));
		assertTrue(dict.isPrefix("help"));

		// load back the same big dictionary

		dict.loadDictionary("words.txt");

		// checking iterator
		Iterator<String> iterator = dict.iterator();

		// loading dict into an array
		ArrayList<String> allWords = new ArrayList<String>();
		Scanner filein = null;
		try {
			filein = new Scanner(new FileReader("words.txt"));
			while (filein.hasNextLine()) {
				String oneline = filein.nextLine();
				oneline = oneline.toLowerCase();
				oneline = oneline.trim();
				if (oneline.length() == 0)
					continue;
				allWords.add(oneline);
			}
			filein.close();
		} catch (IOException e) {
		}

		// iterate all words into a hashset
		HashSet<String> set = new HashSet<String>();
		while (iterator.hasNext())
			set.add(iterator.next());

		// check if all word in dict is here
		for (String x : allWords)
			assertTrue(set.contains(x));

		// loading an empty dictionary
		GameDictionary dict2 = new GameDictionary();
		dict2.loadDictionary("emptyWords.txt");
		assertFalse(dict2.contains("abc"));
		assertFalse(dict2.isPrefix("abc"));

		// loading an dne dictionary
		GameDictionary dict3 = new GameDictionary();
		try {
			dict3.loadDictionary("emptyWords2.txt");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Error when loading dictionary!");
		}

		// not loading an dictionary
		GameDictionary dict4 = new GameDictionary();
		assertFalse(dict4.contains("abc"));
		assertFalse(dict4.isPrefix("abc"));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testGame() throws IOException {
		// Attempt to load our simple, small dictionary again.
		BoggleDictionary dict = new GameDictionary();
		dict.loadDictionary(BoggleTests.DICTIONARY_NAME);

		// Create a new game and set the board to an artifical board.
		// The cubes file doesn't actually matter, we'll be setting our own
		// board.
		BoggleGame game = new GameManager();
		game.newGame(4, 2, "cubes.txt", dict);
		game.setGame(BoggleTests.BOARD);

		// At the beginning, last word should be null;
		assertEquals(null, game.getLastAddedWord());

		// player score should also be 0
		int[] playerInitScore = new int[2];
		assertArrayEquals(playerInitScore, game.getScores());

		// Check if the board is what it looks like.
		for (int row = 0; row < BoggleTests.BOARD.length; row++)
			assertArrayEquals(BoggleTests.BOARD[row], game.getBoard()[row]);

		// other board size
		game.setGame(BOARD2);
		for (int row = 0; row < BoggleTests.BOARD2.length; row++)
			assertArrayEquals(BoggleTests.BOARD2[row], game.getBoard()[row]);

		game.setGame(BoggleTests.BOARD);

		// Try to get all the words off the board, using both search tactics.
		game.setSearchTactic(BoggleGame.SearchTactic.SEARCH_BOARD);
		Set<String> boardWords = new TreeSet<>();
		for (String word : game.getAllWords())
			boardWords.add(word.toLowerCase());

		assertEquals(BoggleTests.BOARD_WORDS, boardWords);

		game.setSearchTactic(BoggleGame.SearchTactic.SEARCH_DICT);
		Set<String> dictWords = new TreeSet<>();
		for (String word : game.getAllWords())
			dictWords.add(word.toLowerCase());

		assertEquals(BoggleTests.BOARD_WORDS, dictWords);

		game.setSearchTactic(SearchTactic.SEARCH_DICT);
		Collection<String> c1 = game.getAllWords();
		game.setSearchTactic(SearchTactic.SEARCH_BOARD);
		Collection<String> c2 = game.getAllWords();
		assertEquals(c1.size(), c2.size());
		game.setSearchTactic(null);
		Collection<String> c3 = game.getAllWords();
		assertEquals(c1.size(), c3.size());
		assertEquals(c3.size(), c2.size());

		// Try to play a valid word and an invalid word:
		assertEquals(5, game.addWord("hallowed", 0));
		assertEquals(0, game.addWord("goodbye", 1));
		// test invalid player #
		try {
			game.addWord("carts", -5);
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Invalid player number");
		}

		// Check that the scores updated appropriately for each player.
		assertArrayEquals(new int[] { 5, 0 }, game.getScores());

		// The last successfully added word is HALLOWED, so we should get the
		// coordinates for that:
		// Clarification: Points should be (row, column), i.e.,
		// point.x = row and point.y = column. It's silly, yes.
		List<Point> expectedPoints = Arrays.asList(new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0),
				new Point(3, 1), new Point(3, 2), new Point(3, 3), new Point(2, 3));

		assertEquals(expectedPoints, game.getLastAddedWord());

		// a blank cube file
		BoggleGame game1 = new GameManager();
		try {
			game1.newGame(4, 2, "emptyCubes.txt", dict);
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Invalid Board size for the given Cube file.");
		}

		// cube file of not a perfect square
		BoggleGame game2 = new GameManager();
		game2.newGame(4, 2, "cubes2.txt", dict);
		for (char[] x : game2.getBoard())
			for (char y : x) {
				assertNotEquals("", y);
				assertNotEquals(null, y);
			}

		// invalid player number
		BoggleGame game3 = new GameManager();
		try {
			game3.newGame(4, -2, "cubes2.txt", dict);
		} catch (Exception e) {
			assertEquals("Must at least have 1 player", e.getMessage());
		}

		BoggleGame game4 = new GameManager();
		try {
			game4.addWord("word", 5);
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Initialize game first");
		}

	}
}