/**
 * Parses command line arguments.
 * 
 * @author Steely Morneau
 * 
 */
public class ArgumentParser {

	private String[] args;

	/**
	 * Sets its arguments to the command line arguments.
	 * 
	 * @param args
	 */
	public ArgumentParser(String[] args) {
		this.args = args;
	}

	/**
	 * Gets the number of command line arguments.
	 * 
	 * @return args.length
	 */
	public int numParam() {
		return args.length;
	}

	/**
	 * Gets the number of flags in the command line arguments.
	 * 
	 * @return the number of flag
	 */
	public int numFlag() {
		int flags = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				flags++;
			}
		}
		return flags;
	}

	/**
	 * Checks whether a specific flag is called as an argument.
	 * 
	 * @param flag
	 * @return true if flag exists
	 */
	public boolean hasFlag(String flag) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareTo(flag) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether or not a specific flag has a value in the command line
	 * arguments.
	 * 
	 * @param flag
	 * @return true if flag has a value
	 */
	public boolean hasValue(String flag) {
		if (hasFlag(flag)) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].compareTo(flag) == 0) {
					// if there is a next argument and it is not a flag
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Gets the value after a specified flag.
	 * 
	 * @param flag
	 * @return the value of the falg
	 * @throws BadArgumentException
	 */
	public String getValue(String flag) throws BadArgumentException {
		if (hasFlag(flag)) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].compareTo(flag) == 0) {
					// if the next argument is not a flag
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						return args[i + 1];
					} else {
						throw new BadArgumentException();
					}
				}
			}
		}
		throw new BadArgumentException();
	}

	/**
	 * Gets the command line arguments as a string.
	 * 
	 * @return String of command line arguments
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			s.append(args[i] + " ");
		}
		return new String(s);
	}

}