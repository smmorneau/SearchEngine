import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that redirects all requests to the {@link LServlet}. Part of
 * the {@link LServer} example. Handles requests to resource: "/"
 */
@SuppressWarnings("serial")
public class RedirectServlet extends BaseServlet {
	/**
	 * Redirects GET requests to the {@link LServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.sendRedirect("/login");
		} catch (IOException ex) {
			log.debug("Unable to redirect to /login.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Redirects any POST requests to the
	 * {@link #doGet(HttpServletRequest, HttpServletResponse)} method.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
}
