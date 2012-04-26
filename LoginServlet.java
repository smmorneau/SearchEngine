import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Handles /login resource. Servlet for handling login. requests. Part of the
 * {@link LServer} example.
 */
@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet {

	Logger log = Logger.getLogger(LoginServlet.class.getName());

	/**
	 * Output the login form, and any error messages from the previous login
	 * attempt.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		// if user is logged in
		if (isLoggedIn(cookies)) {

			try {

				// logout
				if (request.getParameter("logout") != null) {

					prepareResponse("Seeker: Login", response);

					PrintWriter out = response.getWriter();
					out.println("<h1></h1>\n");

					// erase session cookies
					eraseCookies(request, response);
					cookies.clear();
					out.println("<center><p style=\"color: blue;\">Successfully logged out.</p><center>");

					out.println("<h4>Login</h4>");
					out.println("");
					out.println("");

					// print login form
					printForm(out);

					finishResponse(response);

				} else {
					// redirect to search
					try {
						response.sendRedirect(response
								.encodeRedirectURL("/search"));
					} catch (IOException e) {
						log.error("User tried to relogin, but unable to redirect.");
					}
				}

			} catch (IOException ex) {

			}

		} else {

			prepareResponse("Seeker: Login", response);

			try {
				PrintWriter out = response.getWriter();
				String error = request.getParameter("error");

				out.println("<h1></h1>\n");

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

				// output success message if redirected from successful
				// registration
				if (request.getParameter("newuser") != null) {
					out.println("<center><p style=\"color: blue;\">Registration was successful!");
					out.println("Login with your new username and password below.</p><center>");
				}

				out.println("<h4>Login</h4>");
				out.println("");
				out.println("");

				// print login form
				printForm(out);

				finishResponse(response);

			} catch (IOException e1) {
				log.debug("Unable to prepare response body.", e1);
			}

		}
	}

	/**
	 * Processes the login form, and passes any errors back to the login servlet
	 * GET response.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		try {

			// if user is logged in
			if (isLoggedIn(cookies)) {
				// redirect to search
				response.sendRedirect("/search");

			}

		} catch (IOException io) {
			log.error("User is already logged in. Unable to redirect from login page.");
		}

		String user = request.getParameter("user");
		String pass = request.getParameter("pass");

		Status status = db.verifyLogin(user, pass);

		try {
			if (status == Status.OK) {

				// add cookies to indicate user successfully logged in
				response.addCookie(new Cookie("login", "true"));
				response.addCookie(new Cookie("name", user));

				String admin = "false";
				if (db.isAdmin(user)) {
					admin = "true";
					response.addCookie(new Cookie("admin", admin));
					response.sendRedirect("/admin");
				} else {
					response.sendRedirect("/search");
				}

			} else {
				// make sure any old login cookies are cleared
				response.addCookie(new Cookie("login", "false"));
				response.addCookie(new Cookie("name", ""));
				response.addCookie(new Cookie("admin", "false"));

				// let user try again
				response.sendRedirect(response
						.encodeRedirectURL("/login?error=" + status.name()));
			}
		} catch (Exception ex) {
			log.error("Unable to process login form.", ex);
		}
	}

	/**
	 * Prints login form using supplied PrintWriter.
	 * 
	 * @param out
	 *            PrintWriter from HTTP response
	 */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/login\" method=\"post\">");
		out.println("<center>");
		out.println("<p>Usename:");
		out.println("<input type=\"text\" name=\"user\" size=\"30\"></p>");
		out.println("<p>Password:");
		out.println("<input type=\"password\" name=\"pass\" size=\"30\"></p>");
		out.println("<center>");
		out.println("<p><input type=\"submit\" value=\"Login\"></p>");
		out.println("</form></center>");

		out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");
	}
}
