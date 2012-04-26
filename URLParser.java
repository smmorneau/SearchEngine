import java.util.regex.*;

/* URL Standards
 * http://labs.apache.org/webarch/uri/rfc/rfc3986.html
 * 
 * Uniform Resource Identifier (URI)
 * "A simple and extensible means for identifying a resource."
 * 
 * Uniform Resource Locator (URL)
 * "A subset of URIs that, in addition to identifying a resource,
 * provides a means of locating the resource."
 * 
 * Summary:
 * URLs are a type of URIs that identify a resource and its location.
 * 
 * Format:
 * protocol://domain/path?query#reference
 * 
 * Protocol:
 * Begins with letter followed by letters, digits, plus (+), period (.)
 * or hyphen (-).
 * 
 * Regex Quantifiers:
 * ? once or not at all
 * * zero or more times
 * + one  or more times
 * 
 * Boundary Matchers:
 * ^ beginning of line
 * $ end of line
 * 
 * Groups:
 * ( .... ) specifies group
 * (?: ...) specifies non-capturing group
 * group #0 entire expression
 */

/**
 * Uses regex to determine if urls are valid and returns different parts of a
 * link.
 * 
 * @author Sophie Engle
 * 
 */
public class URLParser {
	/*
	 * Example Regular Expressions
	 * 
	 * From http://labs.apache.org/webarch/uri/rfc/rfc3986.html
	 * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
	 * 
	 * protocol: ^([a-zA-Z][\\w+-.]*):// required, starts with letter domain:
	 * ([^/?#]+) required, does not contain /?# resource: (/[^?#]*)? starts with
	 * /, does not contain ?# query: (?:\\?([^#]*))? starts with ?, does not
	 * contain # fragment: (?:#(.*))?$ starts with #, everything until end
	 */
	private static final String pregex = "^([a-zA-Z][a-zA-Z0-9+.-]*)://";
	private static final String dregex = "([^/?#]+)";
	private static final String rregex = "(/[^?#]*)?";
	private static final String qregex = "(?:\\?([^#]*))?";
	private static final String fregex = "(?:#(.*))?$";

	private static final String regex = pregex + dregex + rregex + qregex
			+ fregex;

	public final String url; // http://docs.python.org/library/string.html?highlight=string#module-string
	public final String protocol; // (req) http
	public final String domain; // (req) docs.python.org
	public final String resource; // (opt) /library/string.html
	public final String query; // (opt) highlight=string
	public final String fragment; // (opt) module-string

	public final boolean valid;

	public URLParser(String url) {
		this.url = url;

		Pattern p = Pattern.compile(regex);
		Matcher m = null;

		if (url != null)
			m = p.matcher(url);

		if (m == null || !m.matches() || m.groupCount() < 2) {
			valid = false;

			protocol = null;
			domain = null;
			resource = null;
			query = null;
			fragment = null;

			return;
		}

		// assumptions
		assert url != null;
		assert m.groupCount() >= 2 : m.groupCount();
		assert m.groupCount() <= 5 : m.groupCount();

		valid = true;

		protocol = m.group(1);
		domain = m.group(2);
		resource = m.group(3) == null ? "/" : m.group(3);
		query = m.group(4);
		fragment = m.group(5);

		// postcondition
		assert resource != null : url;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getDomain() {
		return domain;
	}

	public String getResource() {
		return resource;
	}

	public String getQuery() {
		return query;
	}

	public String getFragment() {
		return fragment;
	}

	public String toString() {
		return url;
	}

	public boolean isValid() {
		return valid;
	}

	public void print() {
		String format = "%-8s : %s\n";
		System.out.printf(format, "url", url);
		System.out.printf(format, "valid", valid);

		System.out.printf(format, "protocol", protocol);
		System.out.printf(format, "domain", domain);
		System.out.printf(format, "resource", resource);
		System.out.printf(format, "query", query);
		System.out.printf(format, "fragment", fragment);

		System.out.printf("\n");
	}

}
