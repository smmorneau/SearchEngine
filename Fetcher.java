import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Connects to a website for requests.
 * 
 * @author Sophie Engle & Steely Morenau
 * 
 */
public abstract class Fetcher {
	protected static final int PORT = 80;
	protected URLParser url;
	protected String domain;
	protected String resource;
	protected String html;
	public static String error = "unknown";

	Logger log = Logger.getLogger(Fetcher.class.getName());

	public Fetcher(String url) throws Exception {
		this.url = new URLParser(url);
		fetch();
	}

	protected abstract String craftRequest();

	/**
	 * Fetches html from url.
	 * 
	 * @throws Exception
	 */
	public void fetch() throws Exception {
		if (url.resource == null || url.domain == null) {
			log.error("There is no domain or resource to fetch.\n");
			return;
		}

		log.debug(url.domain + ":" + PORT);

		Socket socket = new Socket(url.domain, PORT);

		PrintWriter writer = new PrintWriter(socket.getOutputStream());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		String request = craftRequest();
		log.debug(request);

		writer.println(request);
		writer.flush();

		String line = reader.readLine();

		// if HTTP 1.1
		if (line != null && line.startsWith("HTTP")) {

			// check if 200 okay
			if (!line.contains("200 OK")) {
				reader.close();
				writer.close();
				socket.close();
				error = line.substring(9, line.length());
				log.debug("Bad request: " + error + "; " + url);
				return;
			}

			while (line != null && !line.trim().isEmpty()) {
				line = reader.readLine();
			}
			// consume blank line required in request
			line = reader.readLine();
		}

		StringBuffer sb = new StringBuffer();
		while (line != null) {
			log.debug("[" + line.trim() + "]");
			sb = sb.append(line.trim() + "///n");

			line = reader.readLine();
		}

		html = sb.toString();

		reader.close();
		writer.close();
		socket.close();

		log.debug("Fetching done.");

	}

	/**
	 * Gets html from page
	 * 
	 * @return html
	 */
	protected String getHTML() {
		return html;
	}

	/**
	 * Gets the error
	 * 
	 * @return error
	 */
	protected String getError() {
		return error;
	}

}