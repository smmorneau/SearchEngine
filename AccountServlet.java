import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Handles the resource /account. Allows logged in users to do account
 * maintenance - changing their passwords, clearing their search and visited
 * histories, or turning on private searching.
 * 
 * @author Steely Morneau
 * 
 */
public class AccountServlet extends BaseServlet {

	Logger log = Logger.getLogger(AccountServlet.class.getName());

	/**
	 * Output the account settings forms, and any error messages from previous
	 * redirection. Checks for the login cookie. If found, displays a welcome
	 * message. Otherwise, redirects to the {@link LoginServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		PrintWriter out = null;
		try {

			if (!isLoggedIn(cookies)) {

				// if the user is not logged in, redirect to login
				response.sendRedirect("/login");
			} else {

				prepareResponse("Seeker: Account", response);
				out = response.getWriter();

				// display welcome message
				String user = cookies.get("name");
				out.println("<div align=\"right\">");
				out.println("<p> hello <b class=\"grey\">" + user + "</b>!<br>");

				// add link back to search
				out.println("<a href=\"/search\">(search)</a><br>");

				if (db.isAdmin(user)) {
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

				out.println("<h1></h1>\n");
				out.println("<h4>Change Account Settings</h4>");

				String error = request.getParameter("error");
				String change = request.getParameter("change");

				/*
				 * Avoid using any user input directly in the HTML output, to
				 * avoid cross-side scripting attacks.
				 */
				if (error != null) {
					// gets error message from the status enum name
					String errorMessage = getStatusMessage(error);

					// safe to output, since we provide the error messages
					out.println("<center><p style=\"color: red;\">"
							+ errorMessage + "</p></center>");
				}

				if (change != null) {
					String statusMessage = getStatusMessage(change);
					out.println("<center><p style=\"color: blue;\">"
							+ statusMessage + "</p></center>");
				}

				printForm(out, cookies);
				finishResponse(response);

			}
		} catch (IOException e) {

		}

	}

	/**
	 * Processes the account settings forms, redirecting to the necessary pages.
	 * Checks for the login cookie. If found, displays a welcome message.
	 * Otherwise, redirects to the {@link LoginServlet}.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		if (!isLoggedIn(cookies)) {

			// if the user is not logged in, redirect to login
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {

			}
		} else {
			// user is logged in

			cookies = getCookieMap(request);

			String user = cookies.get("name");

			// get new password from form
			String currPass = request.getParameter("currPass");
			String newPass = request.getParameter("newPass");
			String clearHistory = request.getParameter("clearHistory");
			String clearVisited = request.getParameter("clearVisited");
			String privateOn = request.getParameter("privateOn");
			String privateOff = request.getParameter("privateOff");

			Status status = null;

			// clear search history
			if (clearHistory != null) {
				db.clearHistory(user);
				try {
					status = Status.CLEARED_HISTORY;
					response.sendRedirect(response
							.encodeRedirectURL("/history?cleared="
									+ status.name()));
				} catch (IOException e) {
					log.error("Unable to redirect after query history was cleared.");
				}
			}

			// clear visited history
			else if (clearVisited != null) {
				db.clearVisited(user);
				try {
					status = Status.CLEARED_VISITED;
					response.sendRedirect(response
							.encodeRedirectURL("/visited?cleared="
									+ status.name()));
				} catch (IOException e) {
					log.error("Unable to redirect after visited history was cleared.");
				}
			}

			// turn on private search
			else if (privateOn != null) {
				try {
					status = Status.PRIV_SEARCH_ON;
					response.addCookie(new Cookie("private", "on"));
					response.sendRedirect(response
							.encodeRedirectURL("/search?searchmode="
									+ status.name()));
				} catch (IOException e) {
				}

			}

			// turn off private search
			else if (privateOff != null) {
				try {
					status = Status.PRIV_SEARCH_OFF;
					response.addCookie(new Cookie("private", "off"));
					response.sendRedirect(response
							.encodeRedirectURL("/search?searchmode="
									+ status.name()));
				} catch (IOException e) {
				}

			}

			// change user's password
			else if (currPass != null && !currPass.equals("")
					&& newPass != null && !newPass.equals("")) {

				// if user's entered correct password and a new password
				if (db.verifyLogin(user, currPass) == Status.OK) {

					if (!db.isNewPassword(user, newPass)) {
						db.changePassword(user, newPass);
						status = Status.PASS_CHANGED;
						try {
							response.sendRedirect(response
									.encodeRedirectURL("/account?change="
											+ status.name()));
						} catch (IOException e) {
							log.error("Unable to redirect after password change.");
						}
					} else {
						status = Status.SAME_PASS;
						try {
							String s = response
									.encodeRedirectURL("/account?error="
											+ status.name());
							response.sendRedirect(s);
						} catch (IOException ioe) {
							log.error("Unable to redirect after incorrect password change.");
						}
					}

				} else {
					// if a user did not enter the correct current password
					status = Status.INCORRECT_PASSWORD;
					try {
						String s = response.encodeRedirectURL("/account?error="
								+ status.name());
						response.sendRedirect(s);
					} catch (IOException e) {
						log.error("Unable to redirect after wrong password was entered..");
					}

				}
			} else {
				// user left one of the password fields blank
				status = Status.INVALID_ACCT_CHANGE;
				try {
					// String s = response.encodeRedirectURL("/account?error=" +
					// status.name());
					String s = response.encodeRedirectURL("/account");
					response.sendRedirect(s);
				} catch (IOException e) {
					log.error("Unable to redirect after null password(s) was entered..");
				}
			}
		}

	}

	/**
	 * Prints in html the new password form, the clear history button, and the
	 * turn private browsing on/off button.
	 * 
	 * @param out
	 *            PrintWriter from HTTP response
	 * @param cookies
	 */
	private void printForm(PrintWriter out, Map<String, String> cookies) {
		assert out != null;

		// new password form
		out.println("<form action=\"/account\" method=\"post\">");
		out.println("<center>");
		out.println("<p>Old password:&nbsp");
		out.println("<input type=\"password\" name=\"currPass\" size=\"30\"></p>");
		out.println("<p>New password:");
		out.println("<input type=\"password\" name=\"newPass\" size=\"30\"></p>");
		out.println("<p><input type=\"submit\" value=\"Change password\"></p>");
		out.println("</form></center>");

		out.println("<center class=\"green\">OR<br></center>");

		// clear history button
		out.println("<form action=\"/account\" method=\"post\">");
		out.println("<center>");
		out.println("<p><input type=\"submit\" name=\"clearHistory\" value=\"Clear search history\"> or ");
		out.println("<input type=\"submit\" name=\"clearVisited\" value=\"Clear visited history\"></p>");
		out.println("</form></center>");

		out.println("<center class=\"green\">OR<br></center>");

		if (isPrivate(cookies)) {

			// turn off private search
			out.println("<form action=\"/account\" method=\"post\">");
			out.println("<center>");
			out.println("<p>Private mode is on. <input type=\"submit\" name=\"privateOff\" value=\"Turn off private search\"></p>");
			out.println("</form></center>");

		} else {

			// turn on private search
			out.println("<form action=\"/account\" method=\"post\">");
			out.println("<center>");
			out.println("<p>Private mode is off. <input type=\"submit\" name=\"privateOn\" value=\"Turn on private search\"></p>");
			out.println("</form></center>");
		}

	}

}
