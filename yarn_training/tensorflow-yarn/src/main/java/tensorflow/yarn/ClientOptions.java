package tensorflow.yarn;

public final class ClientOptions {

	private ClientOptions() {
	}

	public static final String OPT_APPNAME = "appname";

	public static final String OPT_APPNAME_DESC = "Application Name. Default value - TensorflowYarn";
	
	public static final String OPT_PRIORITY = "priority";

	public static final String OPT_PRIORITY_DESC = "Application Priority. Default 0";

	public static final String OPT_QUEUE = "queue";

	public static final String OPT_QUEUE_DESC = "RM Queue in which this application is to be submitted";

	public static final String OPT_TIMEOUT = "timeout";

	public static final String OPT_TIMEOUT_DESC = "Application timeout in milliseconds";

	public static final String OPT_MASTER_MEMORY = "master_memory";

	public static final String OPT_MASTER_MEMORY_DESC = "Amount of memory in MB to be requested to run the application master";

	public static final String OPT_MASTER_VCORES = "master_vcores";

	public static final String OPT_MASTER_VCORES_DESC = "Amount of virtual cores to be requested to run the application master";

	public static final String OPT_MASTER_JAR = "master_jar";

	public static final String OPT_MASTER_JAR_DESC = "Jar file containing the application master";

	public static final String OPT_SHELL_COMMAND = "shell_command";

	public static final String OPT_SHELL_COMMAND_DESC = "Shell command to be executed by "
			+ "the Application Master. Can only specify either --shell_command or --shell_script";

	public static final String OPT_SHELL_SCRIPT = "shell_script";

	public static final String OPT_SHELL_SCRIPT_DESC = "Location of the shell script to be "
			+ "executed. Can only specify either --shell_command or --shell_script";

	public static final String OPT_SHELL_ARGS = "shell_args";

	public static final String OPT_SHELL_ARGS_DESC = "Command line args for the shell script. Multiple args can be separated by empty space.";

	public static final String OPT_SHELL_ENV = "shell_env";

	public static final String OPT_SHELL_ENV_DESC = "Environment for shell script. Specified as env_key=env_val pairs";

	public static final String OPT_SHELL_CMD_PRIORITY = "shell_cmd_priority";

	public static final String OPT_SHELL_CMD_PRIORITY_DESC = "Priority for the shell command containers";

	public static final String OPT_CONTAINER_MEMORY = "container_memory";

	public static final String OPT_CONTAINER_MEMORY_DESC = "Amount of memory in MB to be requested to run the shell command";

	public static final String OPT_CONTAINER_VCORES = "container_vcores";

	public static final String OPT_CONTAINER_VCORES_DESC = "Amount of virtual cores to be requested to run the shell command";

	public static final String OPT_NUM_CONTAINERS = "num_containers";

	public static final String OPT_NUM_CONTAINERS_DESC = "No. of containers on which the shell command needs to be executed";

	public static final String OPT_LOG_PROPERTIES = "log_properties";

	public static final String OPT_LOG_PROPERTIES_DESC = "log4j.properties file";

	public static final String OPT_KEEP_CONTAINERS = "keep_containers_across_application_attempts";

	public static final String OPT_KEEP_CONTAINERS_DESC = "Flag to indicate whether to keep containers across application attempts."
			+ " If the flag is true, running containers will not be killed when"
			+ " application attempt fails and these containers will be retrieved by" + " the new application attempt ";

	public static final String OPT_DEBUG = "debug";

	public static final String OPT_DEBUG_DESC = "Dump out debug information";

	public static final String OPT_HELP = "help";

	public static final String OPT_HELP_DESC = "Print usage";

}
