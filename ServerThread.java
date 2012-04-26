import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * Starts server on its own thread on port 8080.
 * 
 * @author Steely Morneau
 * 
 */
public class ServerThread extends Thread {

	private Logger log = Logger.getLogger(ServerThread.class.getName());

	public final static int PORT = 8080;

	public static Object quitter = new Object();

	private Server server;

	/**
	 * Starts the Jetty server on port 8080 and configures the servlet handlers.
	 */
	public void run() {
		server = new Server(PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(LoginServlet.class, "/login");
		handler.addServletWithMapping(RegisterServlet.class, "/register");
		handler.addServletWithMapping(SearchServlet.class, "/search");
		handler.addServletWithMapping(AdminServlet.class, "/admin");
		handler.addServletWithMapping(AccountServlet.class, "/account");
		handler.addServletWithMapping(HistoryServlet.class, "/history");
		handler.addServletWithMapping(SaveVisitedServlet.class, "/redirect");
		handler.addServletWithMapping(VisitedServlet.class, "/visited");
		handler.addServletWithMapping(RedirectServlet.class, "/");

		try {
			log.info("Server started.");

			server.start();
			server.join();

			log.debug("Exiting...");
		} catch (Exception ex) {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		}
	}

	/**
	 * Shuts down the server gracefully.
	 */
	public void shutdown() {
		try {
			log.info("Server has been shutdown.");
			server.stop();
		} catch (Exception e) {
			log.error("Server can't shutdown.");
		}
	}

}
