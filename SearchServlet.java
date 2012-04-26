import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SearchServlet extends BaseServlet {

	protected static DatabaseHandler db = DatabaseHandler.getInstance();
	
	/**
	 * Checks for the login cookie. If found, displays a welcome message.
	 * Otherwise, redirects to the {@link LoginServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		try {

			if (!isLoggedIn(cookies)) {

				// if the user is not logged in, redirect to login
				response.sendRedirect("/login");
			}

			prepareResponse("Seeker", response);
			PrintWriter out = response.getWriter();
			
			// display welcome message
			String user = cookies.get("name");
			out.println("<div align=\"right\">");
			out.println("<p> hello <b class=\"grey\">" + user + "</b>!<br>");
			
			// add link back to search
			out.println("<a href=\"/search\">(search)</a><br>");
			
			// add link to account settings
			out.println("<a href=\"/account\">(account settings)</a><br>");
			
			if(db.isAdmin(user)) {
				// add link to admin page
				out.println("<a href=\"/admin\">(administrator)</a><br>");
			}
			
			// add link to search history
			out.println("<a href=\"/history\">(search history)</a><br>");
			
			// add link to visited pages
			out.println("<a href=\"/visited\">(visited pages)</a><br>");
			
			// add link allowing user to logout
			out.println("<a href=\"/login?logout\">(logout)</a></p>");
			out.println("</div>");
			
			out.println("<h1> </h1>\n");
			
			String error = request.getParameter("error");
			
			if (error != null) {
				// gets error message from the status enum name
				String errorMessage = getStatusMessage(error);

				// safe to output, since we provide the error messages
				out.println("<center><p style=\"color: red;\">" + errorMessage + "</p></center>");
			}

			printForm(out, cookies);

			finishResponse(response);

		} catch (IOException ex) {
			log.warn("Unable to write response body.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}


	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> cookies = getCookieMap(request);
		String user = cookies.get("name");
		
		if (!isLoggedIn(cookies)) {

			// if the user is not logged in, redirect to login
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {
				
			}
		}

		prepareResponse("Seeker: Search Results", response);
		
		Status status = Status.OK;

		String query = request.getParameter("query");
	
		String partialSearch = request.getParameter("partialSearch");
		if(partialSearch == null) {
			partialSearch = "on";
		}
				
		if(!isPrivate(cookies)) {
			// get status from database handler saveQuery attempt
			status = db.saveQuery(user, query);
		}
		
		if(query.equals("")) {
			status = Status.NULL_QUERY;
			// redirect to search page with error

		}
		
		// ignore capitalization & remove whitespace and non-alphanumeric
		// characters
		String strippedQuery = query.toLowerCase().trim()
				.replaceAll("[^a-zA-Z0-9 ]", "");

		try {
			if (status == Status.OK) {
				// if everything went okay, search
				log.debug("Searching for queries.");
				ArrayList<SiteRanker> ranksList = null;
				
				long start = -1;
				long elapsed = -1;
				
					try {
						start = System.currentTimeMillis();
						if(partialSearch.equals("off")) {
							ranksList = Searcher.noPartialSearch(strippedQuery);
						} else {
							ranksList = Searcher.partialSearch(strippedQuery);
						}
						elapsed = (System.currentTimeMillis() - start);
					} catch (IOException e) {
						log.error("IOException.");
						e.printStackTrace();
					}

				try {
					PrintWriter out = response.getWriter();
					
					// display welcome message
					out.println("<div align=\"right\">");
					out.println("<p> hello <b class=\"grey\">" + user + "</b>!<br>");
					
					// add link back to search
					out.println("<a href=\"/search\">(search)</a><br>");
					
					// add link to account settings
					out.println("<a href=\"/account\">(account settings)</a><br>");
					
					if(db.isAdmin(user)) {
						// add link to admin page
						out.println("<a href=\"/admin\">(administrator)</a><br>");
					}
					
					// add link to search history
					out.println("<a href=\"/history\">(search history)</a><br>");
					
					// add link to visited pages
					out.println("<a href=\"/visited\">(visited pages)</a><br>");
					
					// add link allowing user to logout
					out.println("<a href=\"/login?logout\">(logout)</a><br>");
					out.println("</div>");
					
					// write search results to html file
					
					int numResults = ranksList.size();
					
					if(numResults == 0) {
						out.println("<h2>There are no search results for <b>");
					} else if (numResults == 1) {
						out.println("<h2>There is " + ranksList.size() + " search result for <b>");
					}else {
						out.println("<h2>There are " + ranksList.size() + " search results for <b>");
					}
					
					out.println(query + "</b></h2>\n");
					
					out.println("<center class=\"green\">(About " + elapsed/1000.0 + " seconds.)</center><br><br>");
					
					for (SiteRanker r : ranksList) {
						out.println("<a href = \"redirect?url=" + r.getSiteName() + "\">"
								+ r.getSiteName() + "</a> <br>");
						URLParser parser = new URLParser(r.getSiteName());
						out.println("<h3>" + parser.getDomain()
								+ "</h3><br>");
						String snippet = db.getSnippet(r.getSiteName());
						out.println(snippet + "<br><br>");
					}
					out.println();

					out.println("<br><br><br>");

				} catch (IOException e) {
					log.error("Unable to print results.");
					e.printStackTrace();
				}

			} else {
				// include status name in url to provide user-friendly
				// error message later
				String url = "/search?error=" + status.name();

				// encode url properly (see
				// http://www.w3schools.com/tags/ref_urlencode.asp)
				url = response.encodeRedirectURL(url);

				// make user try to search again by redirecting back
				// to search servlet
				response.sendRedirect(url);
			}
		} catch (IOException ex) {
			log.warn("Unable to save query. " + status, ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		finishResponse(response);
	}

	/**
	 * Prints search form using supplied PrintWriter.
	 * 
	 * @param out
	 *            PrintWriter from HTTP response
	 */
	private void printForm(PrintWriter out, Map<String, String> cookies) {
		assert out != null;
		
		String privSearch = "on";
		
		if(!isPrivate(cookies)) {
			privSearch = "off";
		}

		out.println("<form action=\"/search\" method=\"post\">");
		out.println("\t<center>");
		out.println("\t\t<input type=\"text\" name=\"query\" size=\"100\">");
		out.println("<p><input type=\"checkbox\" name=\"partialSearch\" value=\"off\" /> Turn off partial search<br /></p>");
		out.println("<p>Private search mode is <a href=\"/account\">" + privSearch + "</a>.</p>");
		out.println("<p><input type=\"submit\" value=\"Search\" style=\"height: 50px; width: 75px\" ></p>");
		out.println("</form>");
	}


}
