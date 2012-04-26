import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * AdminServlet handles the resource /admin. It allows logged in admin users to
 * add a new seed or shutdown the server.
 * 
 * @author Steely Morneau
 * 
 */
public class AdminServlet extends BaseServlet {

	Logger log = Logger.getLogger(AdminServlet.class.getName());

	/**
	 * Outputs the admin settings forms, and any error messages from previous
	 * redirection. Checks for the login and admin cookies. If both found,
	 * displays a welcome message. Otherwise, redirects to the
	 * {@link LoginServlet} or {@link SearchServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		// if the user is not logged in, redirect to login
		if (!isLoggedIn(cookies)) {
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {

			}
		} else {

			// if user is not admin
			if (cookies.get("name") != null && !db.isAdmin(cookies.get("name"))) {

				// redirect to search page
				try {
					response.sendRedirect("/search");
				} catch (IOException e) {
					log.error("User is not admin but cannot redirect to search.");
				}
			} else {
				try {
					prepareResponse("Seeker: Admin", response);
					PrintWriter out = response.getWriter();

					// display welcome message
					String user = cookies.get("name");
					out.println("<div align=\"right\">");
					out.println("<p> hello <b class=\"grey\">" + user
							+ "</b>!<br>");

					// add link back to search
					out.println("<a href=\"/search\">(search)</a><br>");

					// add link to account settings
					out.println("<a href=\"/account\">(account settings)</a><br>");

					// add link to search history
					out.println("<a href=\"/history\">(search history)</a><br>");

					// add link to visited pages
					out.println("<a href=\"/visited\">(visited pages)</a><br>");

					// add link allowing user to logout
					out.println("<a href=\"/login?logout\">(logout)</a></p>");
					out.println("</div>");

					out.println("<h1></h1>\n");
					out.println("<h4>Administrator Settings</h4>");
					out.println("");
					printForm(out);
					String error = request.getParameter("error");

					/*
					 * Avoid using any user input directly in the HTML output,
					 * to avoid cross-side scripting attacks.
					 */
					if (error != null) {
						// gets error message from the status enum name
						String errorMessage = getStatusMessage(error);

						// safe to output, since we provide the error messages
						out.println("<center><p style=\"color: red;\">"
								+ errorMessage + "</p><center>");
					}

					finishResponse(response);

				} catch (IOException ex) {
					log.debug("Unable to prepare response body.", ex);
				}
			}
		}
	}

	/**
	 * Processes the admin settings forms, adding a new seed or shutting down
	 * the server.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		// if the user is not logged in, redirect to login
		if (!isLoggedIn(cookies)) {
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {
			}
		} else { // user is logged in

			// get new seed and shutdown command from form
			String newSeed = request.getParameter("seed");
			String shutdown = request.getParameter("shutdown");

			Status status = Status.OK;

			// if user entered a new seed
			if (newSeed != null && !newSeed.equals("")) {
				newSeed = HTMLParser.addSlashIfNeeded(newSeed);

				// if new seed is valid
				if (HTMLParser.checkLinkValidity(newSeed)) {
					// start new seed
					WebCrawler.addSeed(newSeed);
					WorkQueue.getInstance().execute(new WebCrawler(newSeed));

					try {
						response.sendRedirect(response
								.encodeRedirectURL("/search"));
					} catch (IOException e) {
						log.debug("Cannot redirect to /search.");
					}

				} else { // if invalid seed, redirect to admin error
					log.debug("Admin entered an invalid seed.");

					try {
						status = Status.INVALID_URL;
						response.sendRedirect(response
								.encodeRedirectURL("/admin/error=?")
								+ status.name());
					} catch (IOException e) {
						log.debug("Cannot redirect to /search.");
					}
				}

			} else if (shutdown != null && !shutdown.equals("")) {
				// shutdown server

				try {
					response.sendRedirect(response
							.encodeRedirectURL("/admin?shutdown"));
				} catch (IOException e) {
					log.debug("Cannot redirect to admin?shutdown.");
				}

				synchronized (ServerThread.quitter) {
					ServerThread.quitter.notifyAll();
				}
			} else { // if new seed field is blank and user did not shutdown
						// server
				status = Status.INVALID_ADMIN;
				try {
					response.sendRedirect(response.encodeRedirectURL("/admin"));
				} catch (IOException e) {
					log.debug("Admin did not enter a new seed. Unable to redirect to /admin.");
				}
			}
		}
	}

	/**
	 * Prints admin forms using supplied PrintWriter.
	 * 
	 * @param out
	 *            PrintWriter from HTTP response
	 */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/admin\" method=\"post\">");
		out.println("\t<tr>");
		out.println("\t\t<td>New crawl seed:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"seed\" size=\"45\"> <input type=\"submit\" value=\"Start new seed\"></td>");
		out.println("\t</tr>");
		out.println("</form><br>");
		out.println("<center class=\"green\">OR<br></center>");
		out.println("<form action=\"/admin\" method=\"post\">");
		out.println("<p><input type=\"submit\" name=\"shutdown\" value=\"Shutdown\"></p>");
		out.println("</form>");
	}

}
