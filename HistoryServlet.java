import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the resouce /history. Displays the logged in user's search history.
 * 
 * @author Steely Morneau
 * 
 */
public class HistoryServlet extends BaseServlet {

	/**
	 * Outputs the user's search history, and any error messages from previous
	 * redirection. Redirects to login page if user is not logged in.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		if (!isLoggedIn(cookies)) {

			// if the user is not logged in, redirect to login
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {

			}
		} else {
			String user = cookies.get("name");

			// write search results
			try {

				prepareResponse("Seeker: Search History", response);
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

				// add link to visited pages
				out.println("<a href=\"/visited\">(visited pages)</a><br>");

				// add link allowing user to logout
				out.println("<a href=\"/login?logout\">(logout)</a><br>");
				out.println("</div>");

				out.println("<h2>Search History<b></b></h2><br>\n");

				// output success message if search history was cleared
				if (request.getParameter("cleared") != null) {
					out.println("<center><p style=\"color: blue;\">Search history was cleared!</p><center>");
				}

				String query = null;
				String time = null;

				// get saved queries
				ResultSet results = db.getHistory(user);

				out.println("<table>");

				// print results
				while (results.next()) {
					query = results.getString("query");
					time = results.getString("time");

					out.println("<tr><td class=\"grey\">" + time + "</td><td>"
							+ query + "</td></tr>");
				}
				out.println("</table>");
				finishResponse(response);

			} catch (IOException e) {
				log.error("Unable to print results.");
				e.printStackTrace();
			} catch (SQLException e) {
				log.error("SQL error with history query.");
			}
		}
	}
}
