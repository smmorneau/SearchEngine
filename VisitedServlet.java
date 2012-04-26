import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the resource /visited. Allows logged in users to see a history of
 * his/her visited sites.
 * 
 * @author Steely Morneau
 * 
 */
public class VisitedServlet extends BaseServlet {

	/**
	 * Outputs the user's visited sites. Checks for the login cookie. If found,
	 * displays a welcome message. Otherwise, redirects to the
	 * {@link LoginServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		if (!isLoggedIn(cookies)) {

			// if the user is not logged in, redirect to login
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {

			}
		}

		String user = cookies.get("name");

		prepareResponse("Seeker: Visited Pages", response);

		// write search results to html file
		try {
			PrintWriter out = response.getWriter();

			// display welcome message
			out.println("<div align=\"right\">");
			out.println("<p> hello <b class=\"grey\">" + user + "</b>!<br>");

			// add link back to search
			out.println("<a href=\"/search\">(search)</a><br>");

			// add link to account settings
			out.println("<a href=\"/account\">(account settings)</a><br>");

			if (db.isAdmin(user)) {
				// add link to admin page
				out.println("<a href=\"/admin\">(administrator)</a><br>");
			}

			// add link to search history
			out.println("<a href=\"/history\">(search history)</a><br>");

			// add link allowing user to logout
			out.println("<a href=\"/login?logout\">(logout)</a><br>");
			out.println("</div>");

			out.println("<h2>Visited Pages<b></b></h2><br>\n");

			// output success message if visited history was cleared
			if (request.getParameter("cleared") != null) {
				out.println("<center><p style=\"color: blue;\">Visited history was cleared!</p><center>");
			}

			String url = null;
			String time = null;

			ResultSet results = db.getVisitedPages(user);

			out.println("<table>");

			while (results.next()) {
				url = results.getString("url");
				time = results.getString("time");

				out.println("<tr><td class=\"grey\">" + time
						+ "</td><td><a href=\"" + url + "\">" + url
						+ "</a></td></tr>");
			}
			out.println("</table>");

		} catch (IOException e) {
			log.error("Unable to print results.");
		} catch (SQLException e) {
			log.error("SQL error with getVisitedPages.");
		}

		finishResponse(response);
	}
}
