import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Work for the queue. Crawls each website, creating more work for each new link found.
 * 
 * @author Steely Morneau
 *
 */
public class WebCrawler implements Runnable {

	private static Logger log = Logger.getLogger(WebCrawler.class
			.getName());
	private String url;
	private WorkQueue workers;
	private static AtomicInteger pageCount = new AtomicInteger(); // default constructor sets it to 0
	private static Lock lock = new Lock();;
	private static ArrayList<String> visitedSites = new ArrayList<String>(); // shared
	private static final int MAXSITES = 30;
	protected static DatabaseHandler db = DatabaseHandler.getInstance();

	/**
	 * Makes executable work from a URL.
	 * 
	 * @param url
	 */
	public WebCrawler(String url) {

		this.url = HTMLParser.addSlashIfNeeded(url);
	}

	/**
	 * Fetches and parses the html, adding words to the inverted index.
	 */
	public void run() {
		
		InvertedIndex index = InvertedIndex.getInstance();
		
		log.debug(Thread.currentThread().getName() + " starting on "
				+ url);
		
		HTMLFetcher fetcher = null;
		
		// fetches HTML
		try {
			fetcher = new HTMLFetcher(url);
		} catch (Exception e) {
		}
		
		workers = WorkQueue.getInstance();
	
		String html = fetcher.getHTML();
		String error = fetcher.getError();
		
		// fetcher returned from bad request
		if(html == null) {
			log.debug(url + " not valid request: " + error + ". Decrement page count.");
			pageCount.decrementAndGet();
			lock.acquireWriteLock();
			visitedSites.remove(url);
			lock.releaseWriteLock();
			return;				
		}
		
		
		ArrayList<String> links = HTMLParser.grabLinks(html, url);
 		Set<String> uniqueLinks = new HashSet<String>(links);
		
		lock.acquireWriteLock();
		
		// adds more work for every link found
		for(String link : uniqueLinks) {
			if(pageCount.get() < MAXSITES && !visitedSites.contains(link)) {
				
				pageCount.incrementAndGet();
				log.info("#" + pageCount + " " + link);
				
				visitedSites.add(link);
				
				workers.execute(new WebCrawler(link));
			}
		}
		
		lock.releaseWriteLock();
		
		log.debug("Stripping html tags from " + url);
		String words = HTMLParser.stripAll(html);
		
		String snippet = null;
		
		// gets page snippet
		if(words.length() > 0) {
			if (words.length() < 255) {
				snippet = words.substring(0, words.length() - 1);
			} else {
				snippet = words.substring(0, 254);
			}
		}

		// save decoded HTML snippet
		if(snippet != null) {
			db.saveSnippet(url, StringEscapeUtils.unescapeHtml(snippet));
		}
		
		Scanner scan = new Scanner(words);

		// word number in the file
		int count = 0;

		String w;
		while (scan.hasNext()) {
			// ignore whitespace and capitalization
			w = scan.next().toLowerCase().trim();
			// remove all non-alphanumeric characters
			String word = w.replaceAll("[^a-zA-Z0-9]", "");
			log.debug("Current word: " + word);
			// insert only non-empty strings
			if (!word.isEmpty()) {
				index.insert(word, url, count);
				log.debug("Number in file: " + count);
				count++;
			}
		}

	}
	
	/**
	 * Add seed url to list of sites and update site count.
	 * 
	 * @param url
	 */
	public static void addSeed(String url) {
		pageCount.incrementAndGet();
		visitedSites.add(url);
		log.debug("#" + pageCount + " " + url + " (seed)");
	}
	
	/**
	 * Gets list of visited sites.
	 * 
	 * @return visited sites
	 */
	public static ArrayList<String> getVisitedSites() {
		return visitedSites;
	}
	
	/**
	 * Gets the number of sites craweled.
	 * 
	 * @return site count
	 */
	public static AtomicInteger getPageCount() {
		return pageCount;
	}

}
