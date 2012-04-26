import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Saves visited links and redirects to the requested site.
 * 
 * @author Steely Morneau
 * 
 */
public class SaveVisitedServlet extends BaseServlet {

	/**
	 * Saves the url of a clicked link. Checks for the login cookie. If found,
	 * redirects to the url of the clicked link. Otherwise, redirects to the
	 * {@link LoginServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> cookies = getCookieMap(request);

		String user = cookies.get("name");

		if (!isLoggedIn(cookies)) {

			// if the user is not logged in, redirect to login
			try {
				response.sendRedirect("/login");
			} catch (IOException e) {

			}
		} else {

			assert user != null; // isLoggedIn should do this check

			try {

				String url = request.getParameter("url");

				if (url != null && !url.equals("")) {
					log.debug("Redirecting to " + url);

					Status status = Status.OK;

					if (!isPrivate(cookies)) {
						// if private mode is off, save visited
						status = db.saveVisited(user, url);
					}

					if (status == Status.OK) {
						log.debug("Saved " + url + "as visited.");
						// redirect to requested URL
						response.sendRedirect(response.encodeRedirectURL(url));
					} else {
						log.warn("Visited page could not be saved.");
					}

				} else {
					url = "";
					// redirect back to /visited
					response.sendRedirect("/visited");
				}

			} catch (IOException e) {
				log.error("Unable to print results.");
			}
		}
	}
}
