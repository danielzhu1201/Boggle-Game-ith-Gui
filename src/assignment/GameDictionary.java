package assignment;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import java.util.*;

public class GameDictionary implements BoggleDictionary {

	HashSet<String> hset = new HashSet<String>();
	ArrayList<String> arrayList = new ArrayList<String>();

	/**
	 * @author zhaosong
	 * Trie Node class
	 *
	 */
	class TrieNode {
		char c;
		LinkedHashMap<Character, TrieNode> children = new LinkedHashMap<Character, TrieNode>();
		public boolean isword;

		public TrieNode() {
		}

		public TrieNode(char c) {
			this.c = c;
			isword = false;
		}
	}

	/**
	 * @author zhaosong
	 * Trie implementations 
	 */
	class Trie {
		private TrieNode root;
		public int height = 0;

		public Trie() {
			root = new TrieNode();
		}

		public TrieNode getRoot() {
			return root;
		}

		public void insert(String word) {
			TrieNode current = root;
			for (int i = 0; i < word.length(); i++) {
				char c = word.charAt(i);
				LinkedHashMap<Character, TrieNode> h = current.children;
				if (h.containsKey(c)) {
					current = h.get(c);
				} else {
					TrieNode tr = new TrieNode(c);
					h.put(c, tr);
					current = tr;
				}
			}
			current.isword = true;
		}

	}

	Trie t = new Trie();

	/* (non-Javadoc)
	 * @see assignment.BoggleDictionary#loadDictionary(java.lang.String)
	 * Load the dictionary
	 */
	@Override
	public void loadDictionary(String filename) throws IOException {
		Scanner filein = null;
		try {
			t = new Trie();
			filein = new Scanner(new FileReader(filename));
			while (filein.hasNextLine()) {
				String oneline = filein.nextLine();
				oneline = oneline.toLowerCase();
				oneline = oneline.trim();
				if (oneline.length() == 0)
					continue;
				if (this.contains(oneline)) {
					continue;
				}
				hset.add(oneline);
				t.insert(oneline);
			}
			filein.close();
		} catch (IOException e) {
			System.err.println("Warning: IOException");
			throw new IOException("Error when loading dictionary!");
		}
	}

	/* (non-Javadoc)
	 * @see assignment.BoggleDictionary#isPrefix(java.lang.String)
	 * Check if a prefix is valid with using HashMap from Trie
	 */
	@Override
	public boolean isPrefix(String prefix) {
		if (prefix == null || t.root.children.isEmpty())
			return false;
		prefix = prefix.toLowerCase();
		prefix = prefix.trim();
		TrieNode current = t.getRoot();
		for (int i = 0; i < prefix.length(); i++) {
			char c = prefix.charAt(i);
			LinkedHashMap<Character, TrieNode> h = current.children;
			if (h.containsKey(c)) {
				current = h.get(c);
			} else {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see assignment.BoggleDictionary#contains(java.lang.String)
	 * Check if a word is in the dictionary with using HashMap from Trie
	 */
	@Override
	public boolean contains(String word) {
		if (word == null || word.length() == 0 || t.root.children.isEmpty())
			return false;
		word = word.toLowerCase();
		word = word.trim();
		TrieNode current = t.getRoot();
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			LinkedHashMap<Character, TrieNode> h = current.children;
			if (h.containsKey(c)) {
				current = h.get(c);
			} else {
				return false;
			}
		}
		if (current.isword == false) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 * Iterator 
	 */
	@Override
	public Iterator<String> iterator() {
		TrieIterator tIterator = new TrieIterator(t.root);
		return tIterator;
	}

	public class TrieIterator implements Iterator<String> {
		ArrayList<String> allwords = new ArrayList<String>();
		String temp = "";
		int currentindex = -1;

		public TrieIterator(TrieNode root) {
			TrieIteratorRecursive(root);
		}

		public void TrieIteratorRecursive(TrieNode tn) {
			temp += tn.c;
			if (tn.isword) {
				allwords.add(temp.substring(1));
			}

			for (TrieNode ch : tn.children.values()) {
				TrieIteratorRecursive(ch);
			}
			temp = temp.substring(0, temp.length() - 1);
		}

		public String next() throws NoSuchElementException {
			currentindex++;
			if (currentindex < allwords.size())
				return allwords.get(currentindex);
			else
				throw new NoSuchElementException("End of list");
		}

		public boolean hasNext() {
			if (currentindex + 1 == allwords.size())
				return false;
			return true;
		}

	}
}