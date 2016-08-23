package tensorflow.yarn;

/**
 * Constants used in both Client and Application Master
 */
public class TFConstants {

	public static final String SCRIPT_PATH = "ExecScript";

	/**
	 * Environment key name pointing to the shell script's location
	 */
	public static final String TENSORFLOW_SCRIPT_LOCATION = "TENSORFLOW_SCRIPT_LOCATION";

	/**
	 * Environment key name denoting the file timestamp for the shell script.
	 * Used to validate the local resource.
	 */
	public static final String TENSORFLOW_SCRIPT_TIMESTAMP = "TENSORFLOW_SCRIPT_TIMESTAMP";

	/**
	 * Environment key name denoting the file content length for the shell
	 * script. Used to validate the local resource.
	 */
	public static final String TENSORFLOW_SCRIPT_LEN = "TENSORFLOW_SCRIPT_LEN";

}
