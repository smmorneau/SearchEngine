import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Parses html page and grabs links
 * 
 * @author smmorneau
 *
 */
public class HTMLParser {

	private static Logger log = Logger.getLogger(HTMLParser.class.getName());

	// grab link: "<a | 1+ spaces | href | 0+ spaces | = | 0+ spaces | () | "
	private static final String rlregex = "<[aA]\\s+.*?[hH][rR][eE][fF]\\s*=\\s*\"([^\"]+?)\"";
	private static final String lregex = "<[aA]\\s+.*?[hH][rR][eE][fF]\\s*=\\s*\"(http://.*?)\"";
	
	// ampersand html code
	private static final String amp = "(&[a-zA-Z0-9#]+;)";
	private static final String tag = "<[^<>]+?>";
	private static final String script = "<[sS][cC][rR][iI][pP][tT](.+?)</[sS][cC][rR][iI][pP][tT]>";
	private static final String style = "<[sS][tT][yY][lL][eE](.+?)</[sS][tT][yY][lL][eE]>";

	/**
	 * Scans the html file and puts content into a string.
	 * 
	 * @param file
	 * @return html
	 */
	public static String makeString(File file) {
		Scanner scan = null;
		StringBuffer sb = null;
		try {
			scan = new Scanner(file);
			sb = new StringBuffer();
			while (scan.hasNext()) {
				String line = scan.nextLine();
				log.debug("[" + line + "]");
				sb = sb.append(line + "///n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return sb.toString();

	}
	
	/**
	 * Grabs all links with http protocol using regexes. Makes relative links
	 * absolute when necessary.
	 * 
	 * @param text
	 *            the html to parse
	 * @param url
	 *            the url of the page that is being parsed
	 * @return the arraylist of links on that page
	 */
	public static ArrayList<String> grabLinks(String text, String url) {

		// grab absolute links
		
		String link = null;

		Pattern p = Pattern.compile(lregex);
		Matcher m = null;

		if (text != null) {
			m = p.matcher(text);
		}

		ArrayList<String> links = new ArrayList<String>();
		while (m.find()) {
			link = m.group(1);
			if(checkLinkValidity(link)) {
				links.add(link);
				log.debug("VALID LINK: " + link);
			} else {
				log.debug("INVALID LINK: " + link);
			}
		}
		
		// grab relative links
		
		link = null;

		p = Pattern.compile(rlregex);
		m = null;

		if (text != null) {
			m = p.matcher(text);
		}

		while (m.find()) {
			link = m.group(1);
			URLParser parser = new URLParser(link);
			if(parser.getProtocol() == null) {
				String absUrl = makeAbsolute(url, link);
				
				if(absUrl != null && checkLinkValidity(absUrl)) {
					links.add(absUrl);
					log.debug("REL LINK: " + link + "; LINK: " + absUrl);	
				} else {
					log.debug(absUrl + "is not a valid link.");
				}	
			}
		}
		
		return links;
	}

	/**
	 * Checks if the url has no extension or .html or .htm.
	 * 
	 * @param link
	 * @return true if link is valid
	 */
	public static boolean checkLinkValidity(String link) {

		URLParser parser = new URLParser(link);
		
		if(!parser.isValid()) {
			return false;
		}
		
		if(!parser.getResource().contains(".")) {
			return true;
		} else if(parser.getResource().endsWith(".html") || parser.getResource().endsWith(".htm")) {
			return true;
		}
		return false;

	}
	
	/**
	 * Adds a "/" to urls with resources without extensions when necessary.
	 * 
	 * @param url
	 * @return
	 */
	public static String addSlashIfNeeded(String url) {
		
		String newLink = url;
		
		URLParser parser = new URLParser(url);
		String resource = parser.getResource();
		if(parser.isValid() && !resource.contains(".") && !resource.endsWith("/")) {
			newLink = url + "/";
			log.debug(url + " -> " + newLink);
		}
		return newLink;
	}
	
	/**
	 * Removes script tags and anything between them.
	 * 
	 * @param text
	 * @return stripped text
	 */
	private static String stripScript(String text) {
		String noScript = text.replaceAll(script, " ").trim();
		log.debug("NO SCRIPT: " + noScript);
		return noScript;
	}
	
	/**
	 * Removes style tags and anything between them.
	 * 
	 * @param text
	 * @return stripped text
	 */
	private static String stripStyle(String text) {
		String noStyle = text.replaceAll(style, " ").trim();
		log.debug("NO STYLE: " + noStyle);
		return noStyle;
	}
	
	/**
	 * Removes html tags.
	 * 
	 * @param text
	 * @return stripped text
	 */
	private static String stripTags(String text) {
		String noTag = text.replaceAll(tag, " ").trim();
		log.debug("NO TAG: " + noTag);
		return noTag;
	}
	
	/*
	 * Method: 	stripAmp
	 * Purpose: 
	 * Input:	the html string
	 * Returns: a string without html escape characters
	 */
	
	/**
	 * Removes html escape characters.
	 * 
	 * @param text
	 * @return stripped text
	 */
	private static String stripAmp(String text) {
		String noAmp = text.replaceAll(amp, " ").trim();
		log.debug("NO TAG: " + noAmp);
		return noAmp;
	}
	
	/**
	 * Removes html tags, script, style, HTML escape characters
	 * 
	 * @param text
	 * @return stripped text
	 */
	public static String stripAll(String text) {
		String strippedText = stripScript(text);
		strippedText = stripStyle(strippedText);
		strippedText = stripTags(strippedText);
		strippedText = stripAmp(strippedText);
		
		strippedText = strippedText.replaceAll("///n", "\n");
		
		return strippedText; // insert only non-empty strings in index
	}

	/**
	 * Makes relative URLs absolute.
	 * 
	 * @param main
	 * @param relative URL
	 * @return absolute URL
	 */
	private static String makeAbsolute(String main, String relUrl) {
		URLParser parser = new URLParser(main);
		String protocol = parser.getProtocol();
		String domain = parser.getDomain();
		String resource = parser.getResource();
		
		String base = null;
		if(resource != null) {
			base = protocol + "://" + domain + resource;
		} else {
			base = protocol + "://" + domain;
		}

		URL absLink = null;
		try {
			URL urlBase = new URL(base);
			absLink = new URL(urlBase, relUrl);
		} catch (MalformedURLException e) {
			log.error("MalformedURLException: " + base + relUrl);
		}

		if(absLink == null) {
			return null;
		}
		
		return absLink.toString();
	}
	
}
