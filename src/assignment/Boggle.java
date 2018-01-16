package assignment;

import javax.swing.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.event.*;
import javax.xml.stream.events.EndDocument;

import assignment.BoggleGame.SearchTactic;
import jdk.nashorn.internal.ir.BreakableNode;

//the UI

public class Boggle extends JComponent {

	public JButton submit, nextPlayer;
	public JTextField input;
	public JLabel[][] gameBoard = new JLabel[4][4];
	public JLabel[] scoreOfEach;
	public String userAnswer;
	public int numOfPlayers, currentPlayer;
	public static int flag = 0;
	public static int flag2 = 0;

	/**
	 * @param frame
	 * @param gm
	 * @param players
	 *            Initialize the game with GUI set up
	 */
	public Boggle(JFrame frame, GameManager gm, int players) {
		numOfPlayers = players;
		scoreOfEach = new JLabel[numOfPlayers];
		currentPlayer = 0;
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout(100, 0));
		JPanel matrix = new JPanel(new GridLayout(4, 4));
		JPanel scores = new JPanel(new GridLayout(3, 1));

		input = new JTextField();
		submit = new JButton("Submit answer");
		nextPlayer = new JButton("End Game and Switch to Next Player");

		input.setSize(5, 5);
		// init player scores
		for (int i = 0; i < gm.getScores().length; i++) {
			scoreOfEach[i] = new JLabel();
			scoreOfEach[i].setFont(new Font("Times New Roman", Font.PLAIN, 15));
			scores.add(scoreOfEach[i]);
		}

		updateScore(scoreOfEach, gm.getScores(), scores);
		updateGameBoard(gameBoard, gm.getBoard(), matrix);

		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userAnswer = input.getText();
				userAnswer = userAnswer.toLowerCase();
				userAnswer = userAnswer.trim();

				if (currentPlayer < numOfPlayers && gm.addWord(userAnswer, currentPlayer) != 0) {
					try {
						// Highlight the words
						boldWord(gameBoard, gm.allwords.get(userAnswer));
						updateScore(scoreOfEach, gm.getScores(), scores);
						if (userAnswer.length() == 4) {
							JOptionPane.showMessageDialog(null, "Congratulations! You got 1 point.");
						} else {
							JOptionPane.showMessageDialog(null,
									"Congratulations! You got " + (userAnswer.length() - 3) + " points.");
						}
						input.setMinimumSize(new Dimension(300, 500));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} else {
					if (gm.userWordSet.get(currentPlayer).contains(userAnswer)) {
						JOptionPane.showMessageDialog(null, "You have already used this word.");
					} else {
						JOptionPane.showMessageDialog(null, "Invalid word!");
					}
				}
				input.setText("");
			}
		});

		nextPlayer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int missedscore = 0;
				JOptionPane.showMessageDialog(null, "End of Turn for player " + (currentPlayer + 1)
						+ ". \n Your final score is " + gm.getScores()[currentPlayer] + ".");
				System.out.println("\nThe words missed by player " + (currentPlayer + 1) + " were:");
				for (String nf : gm.getAllWords()) {
					if (!gm.userWordSet.get(currentPlayer).contains(nf)) {
						System.out.println(nf + "    score:" + (nf.length() - 3));
						missedscore += (nf.length() - 3);
					}
				}
				JOptionPane.showMessageDialog(null, "You could have scored " + missedscore
						+ " more points than you did.\nCheck the console for all words.");

				for (JLabel[] x : gameBoard) {
					for (JLabel y : x)
						y.setForeground(Color.BLACK);
				}

				// start for new players
				if (currentPlayer <= numOfPlayers - 1) {
					currentPlayer++;
				}
				if (currentPlayer >= numOfPlayers) {
					submit.setEnabled(false);
					nextPlayer.setEnabled(false);

				}
			}

		});

		pane.add(scores, BorderLayout.LINE_START);
		pane.add(matrix, BorderLayout.CENTER);
		pane.add(input, BorderLayout.PAGE_END);
		pane.add(submit, BorderLayout.LINE_END);
		pane.add(nextPlayer, BorderLayout.PAGE_START);

	}

	public static void main(String[] args) throws IOException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		GameDictionary dict = new GameDictionary();
		dict.loadDictionary("words.txt");
		int noofplayers = 0;

		while (noofplayers < 1) {
			String playerInput = JOptionPane.showInputDialog("Enter the number of players: ");

			try {
				noofplayers = Integer.parseInt(playerInput);
			} catch (Exception e) {
				System.err.println("Enter a valid number of players.");
			}
		}

		GameManager gManager = new GameManager();
		gManager.newGame(4, noofplayers, "cubes.txt", dict);
		// ADD BUTTON
		String[] chioces = { "Board search", "Dictionary search" };
		int searchMethod = JOptionPane.showOptionDialog(null, "Set search method.", "Search method",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, chioces, "Board search");
		if (searchMethod == 0)
			gManager.setSearchTactic(SearchTactic.SEARCH_BOARD);
		else {
			if (searchMethod == 1)
				gManager.setSearchTactic(SearchTactic.SEARCH_DICT);
			else
				gManager.setSearchTactic(SearchTactic.SEARCH_BOARD);
		}
		System.out.println(">>>Setting up the game...");
		gManager.executeSearch();

		// UI Related Stuff
		JFrame frame = new JFrame("Boggle Game");
		Boggle boggle = new Boggle(frame, gManager, noofplayers);
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		if (flag == 1) {
		}
	}

	/**
	 * @param gameBoard
	 * @param board
	 * @param matrix
	 *            Use this method to update the gameboard in UI
	 */
	public void updateGameBoard(JLabel[][] gameBoard, char[][] board, JPanel matrix) {
		for (int i = 0; i < gameBoard.length; i++)
			for (int j = 0; j < gameBoard[0].length; j++) {
				gameBoard[i][j] = new JLabel(Character.toString(board[i][j]));
				gameBoard[i][j].setFont(new Font("Helvetica", Font.PLAIN, 30));
				matrix.add(gameBoard[i][j]);
			}
	}

	/**
	 * @param scoreOfEach
	 * @param score
	 * @param scores
	 *            Iterate through player's scores and updates the JLabel
	 */
	public void updateScore(JLabel[] scoreOfEach, int[] score, JPanel scores) {
		for (int i = 0; i < score.length; i++) {
			scoreOfEach[i].setText("Player " + (i + 1) + ":" + score[i]);
		}
	}

	/**
	 * @param gameboard
	 * @param words
	 * @throws InterruptedException
	 *             Change the color of the words
	 */
	public void boldWord(JLabel[][] gameboard, List<Point> words) throws InterruptedException {
		for (JLabel[] x : gameboard) {
			for (JLabel y : x)
				y.setForeground(Color.BLACK);
		}

		for (Point x : words) {
			gameboard[(int) x.getX()][(int) x.getY()].setForeground(Color.GREEN);
		}

	}

	public static void printpoints(List<Point> points) {
		for (int i = 0; i < points.size(); i++) {
			System.out.println(points.get(i).getX() + "  " + points.get(i).getY());
		}
	}
}