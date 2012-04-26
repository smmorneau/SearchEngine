import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Searches the inverted index for queries.
 * 
 * @author Steely Morneau
 *
 */
public class Searcher {

	private static Logger log = Logger.getLogger(Searcher.class.getName());


	/**
	 * Searches the index for any word that begins with query.
	 * 
	 * @param query
	 * @return a list of sites and their ranks
	 * @throws IOException
	 */
	public static ArrayList<SiteRanker> partialSearch(String query)
			throws IOException {

		InvertedIndex index = InvertedIndex.getInstance();

		ArrayList<String> sortedKeys = new ArrayList<String>();
		for (String key : index.getMapsWords()) {
			sortedKeys.add(key);
		}

		Collections.sort(sortedKeys);
		log.debug(sortedKeys);

		String[] queryList = query.split(" ");

		// fake set to deny duplicates of filenames
		HashMap<String, Integer> resultsMap = new HashMap<String, Integer>();

		// for a query: list of rankable filenames
		ArrayList<SiteRanker> ranksList = new ArrayList<SiteRanker>();

		// for every individual query word
		for (String queryWord : queryList) {

			log.debug("Currently processing " + queryWord + " of " + query);

			char firstChar = queryWord.charAt(0);
			String letter = Character.toString(firstChar);

			// find starting point
			int start = Collections.binarySearch(sortedKeys, letter);

			// if negative, convert to insertion point
			if (start < 0)
				start = -(start + 1);

			int end = start;

			// find end point
			for (int i = start; i < sortedKeys.size(); i++) {
				if (!sortedKeys.get(i).startsWith(letter))
					break;

				end++;
			}

			if (end >= sortedKeys.size()) {
				end = sortedKeys.size() - 1;
			}

			log.debug("startIndex: " + start);
			log.debug("endIndex: " + end);

			while (start <= end) {

				String wordFromIndex = sortedKeys.get(start);
				if (wordFromIndex.startsWith(queryWord)) {
					log.debug(wordFromIndex + " starts with " + queryWord);

					// map of filenames/occurrences of query
					Set<String> queryMap = index.getWordsUrls(wordFromIndex);

					// for every file containing word
					for (String fileName : queryMap) {

						// if the file isn't already in resultsMap, add it
						if (!resultsMap.containsKey(fileName)) {
							resultsMap.put(fileName, index.getSitesRankforWord(
									wordFromIndex, fileName));
							log.debug("filename: " + fileName + "; rank: "
									+ resultsMap.get(fileName));

						} else { // if the file is already in resultsMap
							log.debug("old rank: "
									+ resultsMap.get(fileName)
									+ "; curr rank: "
									+ index.getSitesRankforWord(wordFromIndex,
											fileName));

							// add current rank to new rank
							Integer newRank = resultsMap.get(fileName)
									+ index.getSitesRankforWord(wordFromIndex,
											fileName);

							// update occurrences for file
							resultsMap.put(fileName, newRank);
							log.debug("new rank: " + resultsMap.get(fileName));

						}
					}

				}
				start++;
			}

		}

		// add files from resultsMap to ranksList
		for (String fileName : resultsMap.keySet()) {
			ranksList.add(new SiteRanker(fileName, resultsMap.get(fileName)));
			log.debug("Add " + fileName + "'s fileRanker object to ranksList.");
		}

		log.debug("Sort files.");
		Collections.sort(ranksList);

		return ranksList;

	}

	/**
	 * Searches the index for the query.
	 * 
	 * @param query
	 * @return
	 */
	public static ArrayList<SiteRanker> noPartialSearch(String query)
			throws IOException {
		InvertedIndex index = InvertedIndex.getInstance();

		ArrayList<String> sortedKeys = new ArrayList<String>();
		for (String key : index.getMapsWords()) {
			sortedKeys.add(key);
		}

		Collections.sort(sortedKeys);
		log.debug(sortedKeys);

		String[] queryList = query.split(" ");

		// fake set to deny duplicates of filenames
		HashMap<String, Integer> resultsMap = new HashMap<String, Integer>();

		// for a query: list of rankable filenames
		ArrayList<SiteRanker> ranksList = new ArrayList<SiteRanker>();

		// for every individual query word
		for (String queryWord : queryList) {

			log.debug("Currently processing " + queryWord + " of " + query);

			char firstChar = queryWord.charAt(0);
			String letter = Character.toString(firstChar);

			// find starting point
			int start = Collections.binarySearch(sortedKeys, letter);

			// if negative, convert to insertion point
			if (start < 0)
				start = -(start + 1);

			int end = start;

			// find end point
			for (int i = start; i < sortedKeys.size(); i++) {
				if (!sortedKeys.get(i).startsWith(letter))
					break;

				end++;
			}

			if (end >= sortedKeys.size()) {
				end = sortedKeys.size() - 1;
			}

			log.debug("startIndex: " + start);
			log.debug("endIndex: " + end);

			while (start <= end) {

				String wordFromIndex = sortedKeys.get(start);
				if (wordFromIndex.equals(queryWord)) {
					log.debug(wordFromIndex + " is " + queryWord);

					// map of filenames/occurrences of query
					Set<String> queryMap = index.getWordsUrls(wordFromIndex);

					// for every file containing word
					for (String fileName : queryMap) {

						// if the file isn't already in resultsMap, add it
						if (!resultsMap.containsKey(fileName)) {
							resultsMap.put(fileName, index.getSitesRankforWord(
									wordFromIndex, fileName));
							log.debug("filename: " + fileName + "; rank: "
									+ resultsMap.get(fileName));

						} else { // if the file is already in resultsMap
							log.debug("old rank: "
									+ resultsMap.get(fileName)
									+ "; curr rank: "
									+ index.getSitesRankforWord(wordFromIndex,
											fileName));

							// add current rank to new rank
							Integer newRank = resultsMap.get(fileName)
									+ index.getSitesRankforWord(wordFromIndex,
											fileName);

							// update occurrences for file
							resultsMap.put(fileName, newRank);
							log.debug("new rank: " + resultsMap.get(fileName));

						}
					}

				}
				start++;
			}

		}

		// add files from resultsMap to ranksList
		for (String fileName : resultsMap.keySet()) {
			ranksList.add(new SiteRanker(fileName, resultsMap.get(fileName)));
			log.debug("Add " + fileName + "'s fileRanker object to ranksList.");
		}

		log.debug("Sort files.");
		Collections.sort(ranksList);

		return ranksList;

	}

}