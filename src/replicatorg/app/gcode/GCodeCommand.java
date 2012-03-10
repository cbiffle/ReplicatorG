package replicatorg.app.gcode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCodeCommand {

	// These are the letter codes that we understand
	private static final char[] codes = { 
		'A', 'B', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
		'M', 'P', 'Q', 'R', 'S', 'T', 'X', 'Y', 'Z' };
	
	// pattern matchers.
	private static final Pattern parenPattern  = Pattern.compile("\\((.*)\\)");
	private static final Pattern semiPattern = Pattern.compile(";(.*)");
	
	
	// The actual GCode command string
	private final String command;

	// Parsed out comment
	private final String comment;
	
	private final Map<Character, Double> parameters;

	/*
	 * Note: this constructor takes ownership of the map, rather than copying
	 * it.  This is okay for the static parse method, but be careful when
	 * adding new ways to obtain a GCodeCommand.
	 */
	private GCodeCommand(String command, String comment,
			Map<Character, Double> parameters) {
		this.command = command;
		this.comment = comment;
		this.parameters = parameters;
	}
	
	public static GCodeCommand parse(String command) {
		String comment = parseComments(command);
		command = filterOutComments(command);
		
		Map<Character, Double> parameters = parseCodes(command);
		
		return new GCodeCommand(command, comment, parameters);
	}
	
	// Find any comments, store them, then remove them from the command 
	private static String parseComments(String command) {
		Matcher parenMatcher = parenPattern.matcher(command);
		Matcher semiMatcher = semiPattern.matcher(command);

		String comment = "";
		
		// Note that we only support one style of comments, and only one comment per row. 
		if (parenMatcher.find())
			comment = parenMatcher.group(1);

		if (semiMatcher.find())
			comment = semiMatcher.group(1);

		// clean it up.
		comment = comment.trim();
		comment = comment.replace('|', '\n');

		return comment;
	}
	
	private static String filterOutComments(String command) {
		command = parenPattern.matcher(command).replaceAll("");
		command = semiPattern.matcher(command).replaceAll("");
		return command;
	}

	// Find any codes, and store them
	private static Map<Character, Double> parseCodes(String command) {
		Map<Character, Double> parameters = new HashMap<Character, Double>();
		for (char code : codes) {
			Pattern myPattern = Pattern.compile(code + "([0-9.+-]+)");
			Matcher myMatcher = myPattern.matcher(command);

			if (command.indexOf(code) >= 0) {
				double value = 0;
				
				if (myMatcher.find()) {
					String match = myMatcher.group(1);
					value = Double.parseDouble(match);
				}
				
				parameters.put(code, value);
			}
		}
		// Freeze the map to prevent accidental modifications later.
		return Collections.unmodifiableMap(parameters);
	}

	public String getCommand() {
		// TODO: Note that this is the command minus any comments.
		return command;
	}
	
	public String getComment() {
		return comment;
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
		return parameters.containsKey(searchCode);
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
		if (parameters.containsKey(searchCode)) {
			return parameters.get(searchCode);
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
		return parameters.containsKey(searchCode) ? parameters.get(searchCode) : fallback;
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
		return (int) getCodeValue(searchCode);
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
	
}