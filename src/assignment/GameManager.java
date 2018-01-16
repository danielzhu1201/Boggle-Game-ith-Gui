package assignment;

import java.awt.Point;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;

import com.sun.org.apache.regexp.internal.recompile;

//Gameengine
public class GameManager implements BoggleGame {

	int boardSize;
	int playerNum;
	int currentplayer;
	SearchTactic currentTactic;
	ArrayList<String> cubes;
	ArrayList<HashSet<String>> userWordSet;
	BoggleDictionary dictionary;
	List<Point> pointlist; // Stores a list of coordinates of the current word.
	HashMap<String, List<Point>> allwords = new HashMap<String, List<Point>>();
	ArrayList<String> allwordsinDict = new ArrayList<String>();
	String lastword = null; // Stores the last word inputed by the user.
	String currentword = ""; // Used in dictionary search
	char[][] currentBoard;
	int[] score;
	int flag = 0;// 0 flag indicates that a search hasn't been called yet.
	private boolean init;

	/* (non-Javadoc)
	 * @see assignment.BoggleGame#newGame(int, int, java.lang.String, assignment.BoggleDictionary)
	 * Initialize the game; load the cube file
	 */
	@Override
	public void newGame(int size, int numPlayers, String cubeFile, BoggleDictionary dict) throws IOException {
		boardSize = size;
		playerNum = numPlayers;
		if (playerNum < 1)
			throw new IllegalArgumentException("Must at least have 1 player");
		dictionary = dict;
		userWordSet = new ArrayList<HashSet<String>>(playerNum);
		score = new int[playerNum];
		cubes = new ArrayList<String>();
		currentTactic = SEARCH_DEFAULT;

		Scanner filein = new Scanner(new FileReader(cubeFile));
		// reading cubeFile
		try {
			while (filein.hasNextLine()) {
				String oneline = filein.nextLine();
				cubes.add(oneline);
			}
			filein.close();
		}

		catch (Exception e) {
			System.err.println("Reading Error");
			throw new IOException("Reading Error");
		} 

		if (!(cubes.size() == (size * size)) || cubes.size() == 0 || size == 0) {
			throw new IllegalArgumentException("Invalid Board size for the given Cube file.");
		}

		java.util.Collections.shuffle(cubes); // shuffles the cubes
		currentBoard = new char[boardSize][boardSize];
		int cubeCount = 0;
		for (int i = 0; i < boardSize; i++)
			for (int j = 0; j < boardSize; j++) {
				currentBoard[i][j] = cubes.get(cubeCount % 16).charAt((int) (cubes.get(cubeCount % 16).length() * Math.random()));
				cubeCount++;
			}
		for (int i = 0; i < playerNum; i++) {
			HashSet<String> h = new HashSet<String>();
			userWordSet.add(h);
		}
		
		init = true;

	}

	@Override
	public char[][] getBoard() {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		
		return currentBoard;
	}

	/* (non-Javadoc)
	 * @see assignment.BoggleGame#addWord(java.lang.String, int)
	 * Determine the score of each word added for a specific player
	 */
	@Override
	public int addWord(String word, int player) {
		if(init!=true) throw new IllegalStateException("Initialize game first");

		if (player >= playerNum || player < 0) {
			throw new IllegalArgumentException("Invalid player number");
		}

		executeSearch();
		if (dictionary.contains(word) && getAllWords().contains(word)) {
			if (userWordSet.get(player).contains(word)) {
				return 0;
			} else {
				if (word.length() > 3) {
					userWordSet.get(player).add(word);
					score[player] += word.length() - 3;
					lastword = word;
					return word.length() - 3;
				} else {
					return 0;
				}
			}
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see assignment.BoggleGame#getLastAddedWord()
	 * Return a point array of the last added word
	 */
	@Override
	public List<Point> getLastAddedWord() {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		
		if (lastword == null)
			return null;
		
		return allwords.get(lastword);
	}

	/* (non-Javadoc)
	 * @see assignment.BoggleGame#setGame(char[][])
	 * Reset a game with a given board
	 */
	@Override
	public void setGame(char[][] board) {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		if (board.length != board[0].length)
			throw new IllegalArgumentException("Non-square board");
		else {
			currentBoard = new char[board.length][board.length];
			currentBoard = board;
			boardSize = currentBoard.length;
			userWordSet = new ArrayList<HashSet<String>>(playerNum);
			for (int i = 0; i < playerNum; i++) {
				HashSet<String> h = new HashSet<String>();
				userWordSet.add(h);
			}
			score = new int[playerNum];
			lastword = null;
			allwords = new HashMap<String,List<Point>>();
			flag =0;
			executeSearch();
		}
	}

	@Override
	public Collection<String> getAllWords() {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		executeSearch();
		return allwords.keySet();
	}

	/**
	 * Perform board search
	 */
	public void boardSearch() {

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				pointlist = new ArrayList<Point>();
				String cur = "";
				boolean[][] visited = new boolean[boardSize][boardSize];
				boardSearchRecursive(visited, i, j, cur);
			}
		}
	}
	
	/**
	 * Perform board search - recursive part
	 */

	public void boardSearchRecursive(boolean visited[][], int i, int j, String cur) {
		visited[i][j] = true;
		cur = cur + Character.toLowerCase(currentBoard[i][j]);
		Point point = new Point(i, j);
		pointlist.add(point);

		if (dictionary.contains(cur) && cur.length() > 3) {
			if (!allwords.containsKey(cur)) {
				ArrayList<Point> newlist = new ArrayList<Point>();
				newlist = copylist(pointlist, newlist);
				allwords.put(cur, newlist);
			}
		}

		if (dictionary.isPrefix(cur)) {

			for (int row = i - 1; row <= i + 1 && row < boardSize; row++) {
				for (int col = j - 1; col <= j + 1 && col < boardSize; col++) {
					if (row >= 0 && col >= 0 && visited[row][col] == false) {
						boardSearchRecursive(visited, row, col, cur);
					}
				}
			}
		}
		cur = cur.substring(0, cur.length() - 1);
		visited[i][j] = false;
		pointlist.remove(pointlist.size() - 1);
	}

	/**
	 * Perform dictionary search
	 */
	public void dictionarySearch() {

		// Store all words in the arrayList allwordsinDict
		Iterator<String> iterator = dictionary.iterator();
		while (iterator.hasNext()) {
			allwordsinDict.add(iterator.next());
		}

		// Finds if each word in allwordsinDict is in the board
		for (int i = 0; i < allwordsinDict.size(); i++) {
			currentword = allwordsinDict.get(i);
			if (currentword.length() > 3) {
				for (int j = 0; j < boardSize; j++) {
					for (int k = 0; k < boardSize; k++) {
						if (allwordsinDict.get(i).charAt(0) == Character.toLowerCase(currentBoard[j][k])) {
							pointlist = new ArrayList<Point>();
							boolean[][] visited = new boolean[boardSize][boardSize];
							dictionarySearchRecursive(visited, currentword, j, k);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Perform dictionary search - recursive part
	 */

	public void dictionarySearchRecursive(boolean visited[][], String word, int i, int j) {
		visited[i][j] = true;
		Point point = new Point(i, j);
		pointlist.add(point);
		if (word.length() == 1 && word.charAt(0) == Character.toLowerCase(currentBoard[i][j])) {
			if (!allwords.containsKey(currentword)) {
				ArrayList<Point> newlist = new ArrayList<Point>();
				newlist = copylist(pointlist, newlist);
				allwords.put(currentword, newlist);
			}
			return;
		}

		else {
			String currentstring = word.substring(1);
			for (int row = i - 1; row <= i + 1 && row < boardSize; row++) {
				for (int col = j - 1; col <= j + 1 && col < boardSize; col++) {
					if (row >= 0 && col >= 0 && visited[row][col] == false
							&& currentstring.charAt(0) == Character.toLowerCase(currentBoard[row][col])) {
						dictionarySearchRecursive(visited, currentstring, row, col);
					}
				}
			}
		}
		visited[i][j] = false;
		pointlist.remove(pointlist.size() - 1);
	}

	@Override
	public void setSearchTactic(SearchTactic tactic) {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		if (tactic == SearchTactic.SEARCH_BOARD)
			currentTactic = SearchTactic.SEARCH_BOARD;
		else if (tactic == SearchTactic.SEARCH_DICT)
			currentTactic = SearchTactic.SEARCH_DICT;
		else
			currentTactic = SEARCH_DEFAULT;
		executeSearch2();
	}

	@Override
	public int[] getScores() {
		if(init!=true) throw new IllegalStateException("Initialize game first");
		return score;
	}

	public static ArrayList<Point> copylist(List<Point> a, ArrayList<Point> b) {
		for (int i = 0; i < a.size(); i++) {
			b.add(a.get(i));
		}
		return b;
	}

	public static void printpoints(List<Point> points) {
		for (int i = 0; i < points.size(); i++) {
			System.out.println(points.get(i).getX() + "  " + points.get(i).getY());
		}
	}

	public void executeSearch() {
		if (flag != 1) {
			flag = 1;
			if (currentTactic == SEARCH_DEFAULT || currentTactic == SearchTactic.SEARCH_BOARD)
				boardSearch();
			else
				dictionarySearch();
		}
	}

	public void executeSearch2() {
		if (currentTactic == SEARCH_DEFAULT || currentTactic == SearchTactic.SEARCH_BOARD)
			boardSearch();
		else
			dictionarySearch();
	}
}