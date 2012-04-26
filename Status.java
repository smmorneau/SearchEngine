import java.util.HashSet;

/**
 * A enum used for providing user-friendly error messages when something
 * goes wrong. Part of the {@link LServer} example.
 * 
 * <p>
 * See <a href="http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html">
 * http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html</a> and
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Enum.html">
 * http://docs.oracle.com/javase/6/docs/api/java/lang/Enum.html</a> for
 * more on enums.
 */
public enum Status 
{
	// create different status types in NAME(code, message) format
	OK               	( 0, "No errors occured"),
	ERROR            	( 1, "Unknown error occured"),
	INVALID_CONFIG   	( 2, "Invalid database configuration file"),
	NO_CONFIG        	( 3, "No database configuration file found"),
	NO_DRIVER        	( 4, "Unable to load JDBC driver"),
	CONNECTION_FAILED	( 5, "Failed to establish database connection"),
	SQL_ERROR        	( 6, "Encountered unknown SQL error"),
	DUPLICATE_USER   	( 7, "User with that name already exists"),
	PASSWORD_LENGTH  	( 8, "Invalid password length"),
	INCORRECT_LOGIN  	( 9, "Login credentials incorrect"),
	NULL_VALUES      	(10, "Must provide both username and password"),
	NULL_QUERY 			(11, "Need to provide a query to search"),
	INVALID_ADMIN    	(12, "New seed or shutdown could not be completed"),
	INVALID_ACCT_CHANGE (13, "Need to enter a value"),
	CLEARED_HISTORY		(14, "Your query history has been cleared"),
	PASS_CHANGED		(15, "Your password has been changed"),
	PRIV_SEARCH_ON		(16, "Private search mode on"),
	PRIV_SEARCH_OFF		(17, "Private search mode off"),
	INVALID_URL			(18, "The url is invalid"),
	INCORRECT_PASSWORD	(19, "Incorrect current password. Unable to change password"),
	CLEARED_VISITED		(20, "Your visited history has been cleared"),
	SAME_PASS			(21, "New password must be different from old password.");
	
	// private members
	private final String message;
	private int code;
	
	// private enum constructor
	private Status(int code, String message)
	{
		this.code = code;
		this.message = message;
	}
	
	/**
	 * 
	 * @return associated error code
	 */
	public int code()
	{
		return this.code;
	}
	
	/**
	 * Returns String representation of status type.
	 */
	public String toString()
	{
		HashSet<Integer> codes = new HashSet<Integer>();
		codes.add(14);
		codes.add(15);
		codes.add(16);
		codes.add(17);
		codes.add(20);
		
		if(codes.contains(code)) {
			return String.format("%s.", message);
		}
		
		return String.format("%s (error code %d).", message, code);
	}
}
