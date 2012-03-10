package replicatorg.app.gcode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeCommand {

	// These are the letter codes that we understand
	static protected char[] codes = { 
		'A', 'B', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
		'M', 'P', 'Q', 'R', 'S', 'T', 'X', 'Y', 'Z' };
	
	// pattern matchers.
	static Pattern parenPattern  = Pattern.compile("\\((.*)\\)");
	static Pattern semiPattern = Pattern.compile(";(.*)");
	static Pattern deleteBlockPattern = Pattern.compile("^(\\.*)");
	
	
	// The actual GCode command string
	private String command;

	// Parsed out comment
	private String comment = new String();

	private class gCodeParameter {
		final public char code;
		final public Double value;
		gCodeParameter(char code, Double value) {
			this.code = code;
			this.value = value;
		}
	}
	
	// The set of parameters in this GCode
	private List<gCodeParameter> parameters;

	public GCodeCommand(String command) {
		// Copy over the command
		this.command = new String(command);
		
		// Initialize the present and value tables
		this.parameters = new ArrayList<gCodeParameter>();
		
		// Parse (and strip) any comments out into a comment string
		parseComments();

		// Parse any codes out into the code tables
		parseCodes();
	}
	
	// Find any comments, store them, then remove them from the command 
	private void parseComments() {
		Matcher parenMatcher = parenPattern.matcher(command);
		Matcher semiMatcher = semiPattern.matcher(command);

		// Note that we only support one style of comments, and only one comment per row. 
		if (parenMatcher.find())
			comment = parenMatcher.group(1);

		if (semiMatcher.find())
			comment = semiMatcher.group(1);

		// clean it up.
		comment = comment.trim();
		comment = comment.replace('|', '\n');

		// Finally, remove the comments from the command string
		command = parenMatcher.replaceAll("");
		
		semiMatcher = semiPattern.matcher(command);
		command = semiMatcher.replaceAll("");
	}

	// Find any codes, and store them
	private void parseCodes() {
		for (char code : codes) {
			Pattern myPattern = Pattern.compile(code + "([0-9.+-]+)");
			Matcher myMatcher = myPattern.matcher(command);

			if (command.indexOf(code) >= 0) {
				double value = 0;
				
				if (myMatcher.find()) {
					String match = myMatcher.group(1);
					value = Double.parseDouble(match);
				}
				
				parameters.add(new gCodeParameter(code, value));
			}
		}
	}

	public String getCommand() {
		// TODO: Note that this is the command minus any comments.
		return new String(command);
	}
	
	public String getComment() {
		return new String(comment);
	}
	
	/**
	 * Checks whether this command contains a parameter with the given code,
	 * e.g. 'T' or 'X'.  This determines whether a subsequent call to 
	 * {@link #getCodeValue} will succeed.
	 * 
	 * @param searchCode desired code (case sensitive)
	 * @return {@code true} if the command includes a parameter with the given
	 *     code, {@code false} otherwise.
	 */
	public boolean hasCode(char searchCode) {
		for (gCodeParameter parameter : parameters) {
			if (parameter.code == searchCode) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Gets the value of a code, which must exist.  Callers should check
	 * that the code exists using {@link #hasCode(char)}.  If this method
	 * is used to request a nonexistent code, it throws.
	 * 
	 * @param searchCode desired code (case sensitive)
	 * @return value of the code.
	 * @throws IllegalStateException if called for a code that does not exist.
	 *     This indicates a bug in the calling program.
	 */
	public double getCodeValue(char searchCode) {
		for (gCodeParameter parameter : parameters) {
			if (parameter.code == searchCode) {
				return parameter.value;
			}
		}
		
		throw new IllegalStateException(
				"getCodeValue called for nonexistent code " + searchCode
				+ "; caller should have checked hasCode first!");
	}
	
	/**
	 * Version of {@link getCodeValue(char)} that returns the magic number
	 * {@code fallback} instead of throwing when the code is missing.  This
	 * can be used to replace the common pattern:
	 * <code>
	 *   double x = DEFAULT_VALUE;
	 *   if (command.hasCode('X')) x = command.getCodeValue('X');
	 * </code>
	 * Long-time readers will note that this is equivalent to the original
	 * getCodeValue method, which would silently return -1 for missing
	 * codes.  To emulate its old behavior (at your own risk) use:
	 * {@code getCodeValue(code, -1)}.
	 * 
	 * @param searchCode desired code (case sensitive)
	 * @param fallback magic number to return if the code isn't found.
	 * @return parameter with given code, if present on the command; {@code
	 *     fallback} otherwise.
	 */
	public double getCodeValue(char searchCode, double fallback) {
		for (gCodeParameter parameter : parameters) {
			if (parameter.code == searchCode) {
				return parameter.value;
			}
		}
		
		return fallback;
	}
	
	/**
	 * Gets the value of a code, which must exist.  Callers should check
	 * that the code exists using {@link #hasCode(char)}.  If this method
	 * is used to request a nonexistent code, it throws.
	 * 
	 * @param searchCode desired code (case sensitive)
	 * @return value of the code, cast to an int.
	 * @throws IllegalStateException if called for a code that does not exist.
	 *     This indicates a bug in the calling program.
	 */
	public int getCodeValueInt(char searchCode) {
		for (gCodeParameter parameter : parameters) {
			if (parameter.code == searchCode) {
				return parameter.value.intValue();
			}
		}
		
		throw new IllegalStateException(
				"getCodeValueInt called for nonexistent code " + searchCode
				+ "; caller should have checked hasCode first!");
	}
	
	/**
	 * Version of {@link getCodeValueInt(char)} that returns the magic number
	 * {@code fallback} instead of throwing when the code is missing.  This
	 * can be used to replace the common pattern:
	 * <code>
	 *   int x = DEFAULT_VALUE;
	 *   if (command.hasCode('X')) x = command.getCodeValueInt('X');
	 * </code>
	 * 
	 * @param searchCode desired code (case sensitive)
	 * @param fallback magic number to return if the code isn't found.
	 * @return parameter with given code, if present on the command; {@code
	 *     fallback} otherwise.
	 */
	public int getCodeValueInt(char searchCode, int fallback) {
		return (int) getCodeValue(searchCode, fallback);
	}
	

//	public Double removeCode(Character searchCode) {
//		for (Iterator<gCodeParameter> i = parameters.iterator(); i.hasNext();)
//		{
//			gCodeParameter gcp = i.next();
//			if(gcp.code == searchCode)
//			{
//				i.remove();
//				return gcp.value;
//			}
//		}
//		return null;
//	}
//	
//	public void addCode(Character code, Double value)
//	{
//		parameters.add(new gCodeParameter(code, value));
//		command += " ";
//		command = command.concat(code.toString()).concat(value.toString());
//	}
//	
//	public void addCode(Character code, Integer value)
//	{
//		parameters.add(new gCodeParameter(code, ((Number)value).doubleValue()));
//		command += " ";
//		command = command.concat(code.toString()).concat(value.toString());
//	}
}