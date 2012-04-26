import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling registration requests. Part of the {@link LServer}
 * example.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {
	/**
	 * Output the registration form, and any error messages from the previous
	 * registration attempt.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, String> cookies = getCookieMap(request);
		
		if (isLoggedIn(cookies)) {

			// if the user is logged in, redirect to account
			try {
				response.sendRedirect("/account");
			} catch (IOException e) {
				
			}
		}
		
		try {
			prepareResponse("Seeker: Register New User", response);

			PrintWriter out = response.getWriter();
			out.println("<h1></h1>");
			out.println("<h4>Register</h4>");

			String error = request.getParameter("error");

			/*
			 * Avoid using any user input directly in the HTML output, to avoid
			 * cross-side scripting attacks.
			 */
			if (error != null) {
				// gets error message from the status enum name
				String errorMessage = getStatusMessage(error);

				// safe to output, since we provide the error messages
				out.println("<center><p style=\"color: red;\">" + errorMessage + "</p><center>");
			}

			printForm(out);
			finishResponse(response);
		} catch (IOException ex) {
			log.debug("Unable to prepare response properly.", ex);
		}
	}

	/**
	 * Processes the registration form, and passes any errors back to the
	 * registration servlet GET response.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, String> cookies = getCookieMap(request);
		
		if (isLoggedIn(cookies)) {

			// if the user is logged in, redirect to account
			try {
				response.sendRedirect("/account");
			} catch (IOException e) {
				
			}
		}
		
		prepareResponse("Seeker: Register New User", response);

		// get username and password from form
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");

		Status status;

		if (!newuser.equals("") && !newpass.equals("")) {
			// get status from database handler registration attempt
			status = db.registerUser(newuser, newpass);
		} else {
			status = Status.NULL_VALUES;
		}

		try {
			if (status == Status.OK) {
				// if everything went okay, let the new user login
				response.sendRedirect(response
						.encodeRedirectURL("/login?newuser=true"));
			} else {
				// include status name in url to provide user-friendly
				// error message later
				String url = "/register?error=" + status.name();

				// encode url properly (see
				// http://www.w3schools.com/tags/ref_urlencode.asp)
				url = response.encodeRedirectURL(url);

				// make user try to register again by redirecting back
				// to registration servlet
				response.sendRedirect(url);
			}
		} catch (IOException ex) {
			log.warn("Unable to redirect user. " + status, ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		finishResponse(response);
	}

	/**
	 * Prints registration form using supplied PrintWriter.
	 * 
	 * @param out
	 *            PrintWriter from HTTP response
	 */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/register\" method=\"post\">");
		out.println("<center>");
		out.println("<p>Usename:");
		out.println("<input type=\"text\" name=\"user\" size=\"30\"></p>");
		out.println("<p>Password:");
		out.println("<input type=\"password\" name=\"pass\" size=\"30\"></p>");
		out.println("<center>");
		out.println("<p><input type=\"submit\" value=\"Register\"></p>");
		out.println("</form></center>");

	}
}
