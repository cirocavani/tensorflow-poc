package tensorflow.yarn;

public final class ApplicationMasterOptions {

	private ApplicationMasterOptions() {
	}

	public static final String OPT_APP_ATTEMPT_ID = "app_attempt_id";

	public static final String OPT_APP_ATTEMPT_ID_DESC = "App Attempt ID. Not to be used unless for testing purposes";

	public static final String OPT_SHELL_ENV = "shell_env";

	public static final String OPT_SHELL_ENV_DESC = "Environment for shell script. Specified as env_key=env_val pairs";

	public static final String OPT_CONTAINER_MEMORY = "container_memory";

	public static final String OPT_CONTAINER_MEMORY_DESC = "Amount of memory in MB to be requested to run the shell command";

	public static final String OPT_CONTAINER_VCORES = "container_vcores";

	public static final String OPT_CONTAINER_VCORES_DESC = "Amount of virtual cores to be requested to run the shell command";

	public static final String OPT_NUM_CONTAINERS = "num_containers";

	public static final String OPT_NUM_CONTAINERS_DESC = "No. of containers on which the shell command needs to be executed";

	public static final String OPT_PRIORITY = "priority";

	public static final String OPT_PRIORITY_DESC = "Application Priority. Default 0";

	public static final String OPT_DEBUG = "debug";

	public static final String OPT_DEBUG_DESC = "Dump out debug information";

	public static final String OPT_HELP = "help";

	public static final String OPT_HELP_DESC = "Print usage";

}
