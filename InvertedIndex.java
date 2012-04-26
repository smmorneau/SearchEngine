import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A threadsafe inverted index that maps words to sites to the occurrences of
 * that word in those sites.
 * 
 * @author Steely Morneau
 * 
 */
public class InvertedIndex {

	private static Logger log = Logger.getLogger(InvertedIndex.class.getName());

	private HashMap<String, HashMap<String, ArrayList<Integer>>> wordMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
	private Lock lock;
	private static InvertedIndex index = null;

	private InvertedIndex() {
		lock = new Lock();
		log.info("Building InvertedIndex...");
	}

	/**
	 * Inserts a word with the site in which it occurs and the position in that
	 * site into the inverted index.
	 * 
	 * @param word
	 * @param fileName
	 * @param position
	 */
	public void insert(String word, String fileName, int position) {
		lock.acquireWriteLock();
		log.debug("Adding " + word + " to index.");
		if (wordMap.get(word) == null) {
			wordMap.put(word, new HashMap<String, ArrayList<Integer>>());
		}
		HashMap<String, ArrayList<Integer>> fileMap = wordMap.get(word);
		if (fileMap.get(fileName) == null) {
			fileMap.put(fileName, new ArrayList<Integer>());
		}
		ArrayList<Integer> occurrence = fileMap.get(fileName);
		occurrence.add(position);
		lock.releaseWriteLock();
	}

	/**
	 * Writes a file that contains each word with all the sites in which they
	 * occur and their word numbers in the sites.
	 * 
	 * Only used for debugging in final version.
	 * 
	 * @throws IOException
	 */
	public void printIndex() throws IOException {
		lock.acquireReadLock();
		FileWriter stream = new FileWriter("invertedindex.txt");
		PrintWriter out = new PrintWriter(stream);
		for (String word : wordMap.keySet()) {
			out.println(word);
			HashMap<String, ArrayList<Integer>> map = wordMap.get(word);
			for (String fileName : map.keySet()) {
				out.print("\"" + fileName + "\"");
				ArrayList<Integer> occurrences = map.get(fileName);
				for (Integer i : occurrences) {
					out.print(", " + i);
				}
				out.println();
			}
			out.println();
		}
		out.println();
		out.println();
		out.close();
		lock.releaseReadLock();
	}

	/**
	 * Gets the HashMap's key set of the inverted index.
	 * 
	 * @return keySet
	 */
	public Set<String> getMapsWords() {
		lock.acquireReadLock();
		Set<String> keys = wordMap.keySet();
		lock.releaseReadLock();
		return keys;
	}

	/**
	 * Gets a set of URLs for a given word in the index.
	 * 
	 * @param word
	 * @return
	 */
	public Set<String> getWordsUrls(String word) {
		lock.acquireReadLock();
		Set<String> sites = wordMap.get(word).keySet();
		lock.releaseReadLock();
		return sites;
	}

	/**
	 * Get a url's rank for a word in the index.
	 * 
	 * @param word
	 * @param fileName
	 * @return
	 */
	public Integer getSitesRankforWord(String word, String fileName) {
		lock.acquireReadLock();
		Integer rank = wordMap.get(word).get(fileName).size();
		lock.releaseReadLock();
		return rank;
	}

	/**
	 * Uses a singleton pattern to get an instance of the InvertedIndex
	 * 
	 * @return index
	 */
	public static InvertedIndex getInstance() {
		if (index == null) {
			synchronized (InvertedIndex.class) {
				if (index == null) {
					index = new InvertedIndex();
				}
			}
		}
		return index;
	}

}
