import org.apache.log4j.Logger;

/**
 * Crafts an request to get HTML from a url.
 * 
 * @author Sophie Engle
 *
 */
public class HTMLFetcher extends Fetcher
{
	Logger log = Logger.getLogger(HTMLFetcher.class.getName());
	
	public HTMLFetcher(String url) throws Exception 
	{
		super(url);
	}

	/**
	 * Crafts an request to get HTML from a url.
	 * 
	 * @return request
	 */
	protected String craftRequest()
	{
		StringBuffer output = new StringBuffer();
		output.append("GET " + url.resource + " HTTP/1.1\n");
		output.append("Host: " + url.domain + "\n");
		output.append("Connection: close\n");
		output.append("\r\n");
		
		return output.toString();
	}
	
}