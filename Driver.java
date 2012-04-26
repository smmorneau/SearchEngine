import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Main for the web crawler. Takes a seed file as input. Crawls that page and
 * all pages that site links to until it runs out of links or crawls 30. Returns
 * search results.
 * 
 * @author Steely Morneau
 * 
 */
public class Driver {

	/**
	 * Runs web crawler.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger(Driver.class.getName());

		PropertyConfigurator.configure("log4j.properties");

		ArgumentParser p = new ArgumentParser(args);

		WorkQueue workers = WorkQueue.getInstance();

		// parse command line args and get seed
		try {
			String seed = p.getValue("-s");

			URLParser parser = new URLParser(seed);
			
			// url is valid crawl seed
			if (parser.isValid()) {
				// add "/" to avoid 301 requests
				seed = HTMLParser.addSlashIfNeeded(seed);
				log.info("Seed: " + seed);
				WebCrawler.addSeed(seed);
				workers.execute(new WebCrawler(seed));

			} else {
				log.fatal("File is not a directory.");
				System.exit(-1);
			}

			ServerThread sThread = new ServerThread();
			sThread.start();
			log.info("Server started.");

			// wait until servlet notifies for shutdown of server
			synchronized (sThread.quitter) {
				sThread.quitter.wait();
			}
			sThread.shutdown();
			log.info("Server shutdown.");

			// wait while the work queue not done
			synchronized (workers.messenger) {
				while (!workers.isDone()) {
					workers.messenger.wait();
				}
			}

			log.debug("Before queue shutdown.");
			workers.stopWorkers();
			log.debug("After queue shutdown.");

			InvertedIndex index = InvertedIndex.getInstance();
			index.printIndex();

		} catch (BadArgumentException be) {
			log.fatal("Bad command line arguments.");
			System.exit(-1);
		} catch (InterruptedException ie) {
			log.fatal("InterruptedException");
			System.exit(-1);
		} catch (IOException e) {
			log.error("Index can't be printed");
		}

		log.debug("The total number of crawled sites is "
				+ WebCrawler.getPageCount());

		log.info("Program is complete.");

	}

}
