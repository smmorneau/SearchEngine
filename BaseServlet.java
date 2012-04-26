import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * The base servlet class, extended by the other servlet classes in this
 * example. Provides several utility functions, and a reference to the database
 * handler. Part of the {@link LServer} example.
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	/** A log4j logger associated with the {@link LServer} class. */
	protected static Logger log = Logger.getLogger(BaseServlet.class);

	/**
	 * {@link DatabaseHandler} instance. There should only be one active at a
	 * time.
	 */
	protected static DatabaseHandler db = DatabaseHandler.getInstance();

	/**
	 * Gets the cookies associated with the HTTP request, and returns the
	 * cookies as a map of (name, value) pairs.
	 * 
	 * @param request
	 *            HTTP request
	 * @return map of cookies as (name, value) pairs
	 */
	protected Map<String, String> getCookieMap(HttpServletRequest request) {
		HashMap<String, String> map = new HashMap<String, String>();
		Cookie[] cookies = request.getCookies();

		if (cookies != null)
			for (Cookie cookie : cookies)
				map.put(cookie.getName(), cookie.getValue());

		return map;
	}

	/**
	 * Retrieves the cookies associated with the HTTP request, and indicates
	 * they should be deleted in the HTTP response.
	 * 
	 * @param request
	 *            HTTP request
	 * @param response
	 *            HTTP response
	 */
	protected void eraseCookies(HttpServletRequest request,
			HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();

		for (Cookie cookie : cookies) {
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 * Given the {@link Status} name, returns the error message. Helps simplify
	 * error handling.
	 * 
	 * @param errorName
	 *            name from {@link Status} enum
	 * @return error message from {@link Status} enum
	 */
	protected String getStatusMessage(String errorName) {
		Status status = null;

		try {
			// try to get associated status enum if possible
			status = Status.valueOf(errorName);
		} catch (IllegalArgumentException ex) {
			// if name is not one of the status enums,
			// return generic error type
			log.warn("Could not determine Status enum type.", ex);
			status = Status.ERROR;
		} catch (NullPointerException ex) {
			// if name is not provided,
			// return generic error type
			log.warn("No Status name provided.", ex);
			status = Status.ERROR;
		}

		// return String representation of Status enum
		return status.toString();
	}

	/**
	 * Prepares the HTTP response with the provided title.
	 * 
	 * @param title
	 *            web page title
	 * @param response
	 *            HTTP response
	 */
	protected void prepareResponse(String title, HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");

			out.println("<!DOCTYPE html>\n");
			out.println("<html>");
			out.println("");
			out.println("<head>");
			out.println("\t<title>" + title + "</title>");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">");
			out.println("<meta name=\"author\" content=\"Steely Morneau\">");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://cs.usfca.edu/~smmorneau/cs212/homework05/style.css\" >");
			out.println("</head>");
			out.println("");
			out.println("<body>");
			out.println("");
		} catch (IOException ex) {
			log.warn("Exception encountered preparing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Finishes the HTTP response. Should be called after
	 * {@link #prepareResponse(String, HttpServletResponse)}.
	 * 
	 * @param response
	 *            HTTP response
	 */
	protected void finishResponse(HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();

			out.println("");
			out.println("</body>");
			out.println("</html>");
			out.flush();

			response.flushBuffer();
		} catch (IOException ex) {
			log.warn("Exception encountered finishing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Checks if user is logged in.
	 * 
	 * @param cookies
	 * @return true if user is logged in
	 */
	public static boolean isLoggedIn(Map<String, String> cookies) {

		String login = cookies.get("login");
		String user = cookies.get("name");

		if (login != null && login.equals("true") && user != null) {
			return true;
		}
		return false;

	}

	/**
	 * Checks whether private search mode is on.
	 * 
	 * @param cookies
	 * @return true if private is on
	 */
	public static boolean isPrivate(Map<String, String> cookies) {

		String login = cookies.get("login");
		String user = cookies.get("name");
		String privateMode = cookies.get("private");

		if (login != null && login.equals("true") && user != null
				&& privateMode != null && privateMode.equals("on")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if user is an administrator.
	 * 
	 * @param cookies
	 * @return true if user is admin
	 */
	public static boolean isAdmin(Map<String, String> cookies) {
		String admin = cookies.get("admin");
		if (admin != null && admin.equals("true")) {
			return true;
		}
		return false;
	}
}