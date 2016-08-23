package tensorflow.yarn;

import static tensorflow.yarn.ClientOptions.OPT_APPNAME;
import static tensorflow.yarn.ClientOptions.OPT_APPNAME_DESC;
import static tensorflow.yarn.ClientOptions.OPT_CONTAINER_MEMORY;
import static tensorflow.yarn.ClientOptions.OPT_CONTAINER_MEMORY_DESC;
import static tensorflow.yarn.ClientOptions.OPT_CONTAINER_VCORES;
import static tensorflow.yarn.ClientOptions.OPT_CONTAINER_VCORES_DESC;
import static tensorflow.yarn.ClientOptions.OPT_DEBUG;
import static tensorflow.yarn.ClientOptions.OPT_DEBUG_DESC;
import static tensorflow.yarn.ClientOptions.OPT_HELP;
import static tensorflow.yarn.ClientOptions.OPT_HELP_DESC;
import static tensorflow.yarn.ClientOptions.OPT_KEEP_CONTAINERS;
import static tensorflow.yarn.ClientOptions.OPT_KEEP_CONTAINERS_DESC;
import static tensorflow.yarn.ClientOptions.OPT_LOG_PROPERTIES;
import static tensorflow.yarn.ClientOptions.OPT_LOG_PROPERTIES_DESC;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_JAR;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_JAR_DESC;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_MEMORY;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_MEMORY_DESC;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_VCORES;
import static tensorflow.yarn.ClientOptions.OPT_MASTER_VCORES_DESC;
import static tensorflow.yarn.ClientOptions.OPT_NUM_CONTAINERS;
import static tensorflow.yarn.ClientOptions.OPT_NUM_CONTAINERS_DESC;
import static tensorflow.yarn.ClientOptions.OPT_PRIORITY;
import static tensorflow.yarn.ClientOptions.OPT_PRIORITY_DESC;
import static tensorflow.yarn.ClientOptions.OPT_QUEUE;
import static tensorflow.yarn.ClientOptions.OPT_QUEUE_DESC;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_ARGS;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_ARGS_DESC;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_CMD_PRIORITY;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_CMD_PRIORITY_DESC;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_COMMAND;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_COMMAND_DESC;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_ENV;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_ENV_DESC;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_SCRIPT;
import static tensorflow.yarn.ClientOptions.OPT_SHELL_SCRIPT_DESC;
import static tensorflow.yarn.ClientOptions.OPT_TIMEOUT;
import static tensorflow.yarn.ClientOptions.OPT_TIMEOUT_DESC;
import static tensorflow.yarn.TFConstants.SCRIPT_PATH;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_LEN;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_LOCATION;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_TIMESTAMP;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.QueueACL;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.QueueUserACLInfo;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

public class Client {

	private static final Log log = LogFactory.getLog(Client.class);

	private static final String APP_MASTER_MAIN_CLASS = "tensorflow.yarn.ApplicationMaster";

	private static final String APP_TYPE = "TENSORFLOW";

	private Configuration hadoopConf;

	private YarnClient yarnClient;

	private String appName = "";
	private int appMasterPriority = 0;
	private String appMasterQueue = "";
	private int appMasterMemory = 10;
	private int appMasterVCores = 1;

	private String appMasterJar = "";
	private final String appMasterMainClass;

	private String shellCommand = "";
	private String shellScriptPath = "";
	private String[] shellArgs = new String[0];
	private Map<String, String> shellEnv = new HashMap<>();
	private int shellCmdPriority = 0;

	private int containerMemory = 10;
	private int containerVirtualCores = 1;
	private int numContainers = 1;

	private String log4jPropFile = "";

	private final long clientStartTime = System.currentTimeMillis();
	private long clientTimeout = 600000;

	private boolean keepContainers = false;

	boolean debugFlag = false;

	private Options opts;

	private static final String shellCommandPath = "shellCommands";
	private static final String shellArgsPath = "shellArgs";
	private static final String appMasterJarPath = "AppMaster.jar";
	private static final String log4jPath = "log4j.properties";

	public static void main(String[] args) {
		boolean result = false;
		try {
			Client client = new Client();
			log.info("Initializing Client");
			try {
				boolean doRun = client.init(args);
				if (!doRun) {
					System.exit(0);
				}
			} catch (IllegalArgumentException e) {
				System.err.println(e.getLocalizedMessage());
				client.printUsage();
				System.exit(-1);
			}
			result = client.run();
		} catch (Throwable t) {
			log.fatal("Error running CLient", t);
			System.exit(1);
		}
		if (result) {
			log.info("Application completed successfully");
			System.exit(0);
		}
		log.error("Application failed to complete successfully");
		System.exit(2);
	}

	public Client() throws Exception {
		this(new YarnConfiguration());
	}

	public Client(Configuration conf) throws Exception {
		this(APP_MASTER_MAIN_CLASS, conf);
	}

	Client(String appMasterMainClass, Configuration conf) {
		this.hadoopConf = conf;
		this.appMasterMainClass = appMasterMainClass;
		yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		opts = new Options();
		opts.addOption(OPT_APPNAME, true, OPT_APPNAME_DESC);
		opts.addOption(OPT_PRIORITY, true, OPT_PRIORITY_DESC);
		opts.addOption(OPT_QUEUE, true, OPT_QUEUE_DESC);
		opts.addOption(OPT_TIMEOUT, true, OPT_TIMEOUT_DESC);
		opts.addOption(OPT_MASTER_MEMORY, true, OPT_MASTER_MEMORY_DESC);
		opts.addOption(OPT_MASTER_VCORES, true, OPT_MASTER_VCORES_DESC);
		opts.addOption(OPT_MASTER_JAR, true, OPT_MASTER_JAR_DESC);
		opts.addOption(OPT_SHELL_COMMAND, true, OPT_SHELL_COMMAND_DESC);
		opts.addOption(OPT_SHELL_SCRIPT, true, OPT_SHELL_SCRIPT_DESC);
		opts.addOption(OPT_SHELL_ARGS, true, OPT_SHELL_ARGS_DESC);
		opts.getOption(OPT_SHELL_ARGS).setArgs(Option.UNLIMITED_VALUES);
		opts.addOption(OPT_SHELL_ENV, true, OPT_SHELL_ENV_DESC);
		opts.addOption(OPT_SHELL_CMD_PRIORITY, true, OPT_SHELL_CMD_PRIORITY_DESC);
		opts.addOption(OPT_CONTAINER_MEMORY, true, OPT_CONTAINER_MEMORY_DESC);
		opts.addOption(OPT_CONTAINER_VCORES, true, OPT_CONTAINER_VCORES_DESC);
		opts.addOption(OPT_NUM_CONTAINERS, true, OPT_NUM_CONTAINERS_DESC);
		opts.addOption(OPT_LOG_PROPERTIES, true, OPT_LOG_PROPERTIES_DESC);
		opts.addOption(OPT_KEEP_CONTAINERS, false, OPT_KEEP_CONTAINERS_DESC);
		opts.addOption(OPT_DEBUG, false, OPT_DEBUG_DESC);
		opts.addOption(OPT_HELP, false, OPT_HELP_DESC);
	}

	private void printUsage() {
		new HelpFormatter().printHelp("Client", opts);
	}

	public boolean init(String[] args) throws ParseException {
		CommandLine cliParser = new GnuParser().parse(opts, args);

		if (args.length == 0) {
			throw new IllegalArgumentException("No args specified for client to initialize");
		}

		if (cliParser.hasOption(OPT_LOG_PROPERTIES)) {
			String log4jPath = cliParser.getOptionValue(OPT_LOG_PROPERTIES);
			try {
				Log4jPropertyHelper.updateLog4jConfiguration(Client.class, log4jPath);
			} catch (Exception e) {
				log.warn("Can not set up custom log4j properties. " + e);
			}
		}

		if (cliParser.hasOption(OPT_HELP)) {
			printUsage();
			return false;
		}

		if (cliParser.hasOption(OPT_DEBUG)) {
			debugFlag = true;
		}

		if (cliParser.hasOption(OPT_KEEP_CONTAINERS)) {
			log.info(OPT_KEEP_CONTAINERS);
			keepContainers = true;
		}

		appName = cliParser.getOptionValue(OPT_APPNAME, "TensorflowYarn");
		appMasterPriority = Integer.parseInt(cliParser.getOptionValue(OPT_PRIORITY, "0"));
		appMasterQueue = cliParser.getOptionValue(OPT_QUEUE, "default");
		appMasterMemory = Integer.parseInt(cliParser.getOptionValue(OPT_MASTER_MEMORY, "10"));
		appMasterVCores = Integer.parseInt(cliParser.getOptionValue(OPT_MASTER_VCORES, "1"));

		if (appMasterMemory < 0) {
			throw new IllegalArgumentException(
					"Invalid memory specified for application master, exiting. Specified memory=" + appMasterMemory);
		}
		if (appMasterVCores < 0) {
			throw new IllegalArgumentException("Invalid virtual cores specified for application master, exiting."
					+ " Specified virtual cores=" + appMasterVCores);
		}

		if (!cliParser.hasOption(OPT_MASTER_JAR)) {
			throw new IllegalArgumentException("No jar file specified for application master");
		}

		appMasterJar = cliParser.getOptionValue(OPT_MASTER_JAR);

		if (!cliParser.hasOption(OPT_SHELL_COMMAND) && !cliParser.hasOption(OPT_SHELL_SCRIPT)) {
			throw new IllegalArgumentException(
					"No shell command or shell script specified to be executed by application master");
		} else if (cliParser.hasOption(OPT_SHELL_COMMAND) && cliParser.hasOption(OPT_SHELL_SCRIPT)) {
			throw new IllegalArgumentException(
					"Can not specify shell_command option and shell_script option at the same time");
		} else if (cliParser.hasOption(OPT_SHELL_COMMAND)) {
			shellCommand = cliParser.getOptionValue(OPT_SHELL_COMMAND);
		} else {
			shellScriptPath = cliParser.getOptionValue(OPT_SHELL_SCRIPT);
		}
		if (cliParser.hasOption(OPT_SHELL_ARGS)) {
			shellArgs = cliParser.getOptionValues(OPT_SHELL_ARGS);
		}
		if (cliParser.hasOption(OPT_SHELL_ENV)) {
			String envs[] = cliParser.getOptionValues(OPT_SHELL_ENV);
			for (String env : envs) {
				env = env.trim();
				int index = env.indexOf('=');
				if (index == -1) {
					shellEnv.put(env, "");
					continue;
				}
				String key = env.substring(0, index);
				String val = "";
				if (index < (env.length() - 1)) {
					val = env.substring(index + 1);
				}
				shellEnv.put(key, val);
			}
		}
		shellCmdPriority = Integer.parseInt(cliParser.getOptionValue(OPT_SHELL_CMD_PRIORITY, "0"));

		containerMemory = Integer.parseInt(cliParser.getOptionValue(OPT_CONTAINER_MEMORY, "10"));
		containerVirtualCores = Integer.parseInt(cliParser.getOptionValue(OPT_CONTAINER_VCORES, "1"));
		numContainers = Integer.parseInt(cliParser.getOptionValue(OPT_NUM_CONTAINERS, "1"));

		if (containerMemory < 0 || containerVirtualCores < 0 || numContainers < 1) {
			throw new IllegalArgumentException("Invalid no. of containers or container memory/vcores specified,"
					+ " exiting. Specified containerMemory=" + containerMemory + ", containerVirtualCores="
					+ containerVirtualCores + ", numContainer=" + numContainers);
		}

		clientTimeout = Integer.parseInt(cliParser.getOptionValue(OPT_TIMEOUT, "3600000"));

		log4jPropFile = cliParser.getOptionValue(OPT_LOG_PROPERTIES, "");

		return true;
	}

	public boolean run() throws IOException, YarnException {
		log.info("Running Client");
		yarnClient.start();

		YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
		log.info("Got Cluster metric info from ASM" + ", numNodeManagers=" + clusterMetrics.getNumNodeManagers());

		List<NodeReport> clusterNodeReports = yarnClient.getNodeReports(NodeState.RUNNING);
		log.info("Got Cluster node info from ASM");
		for (NodeReport node : clusterNodeReports) {
			log.info(
					"Got node report from ASM for, nodeId=" + node.getNodeId() + ", nodeAddress" + node.getHttpAddress()
							+ ", nodeRackName" + node.getRackName() + ", nodeNumContainers" + node.getNumContainers());
		}

		QueueInfo queueInfo = yarnClient.getQueueInfo(this.appMasterQueue);
		log.info("Queue info, queueName=" + queueInfo.getQueueName() + ", queueCurrentCapacity="
				+ queueInfo.getCurrentCapacity() + ", queueMaxCapacity=" + queueInfo.getMaximumCapacity()
				+ ", queueApplicationCount=" + queueInfo.getApplications().size() + ", queueChildQueueCount="
				+ queueInfo.getChildQueues().size());

		List<QueueUserACLInfo> listAclInfo = yarnClient.getQueueAclsInfo();
		for (QueueUserACLInfo aclInfo : listAclInfo) {
			for (QueueACL userAcl : aclInfo.getUserAcls()) {
				log.info(
						"User ACL Info for Queue, queueName=" + aclInfo.getQueueName() + ", userAcl=" + userAcl.name());
			}
		}

		// Get a new application id
		YarnClientApplication app = yarnClient.createApplication();
		GetNewApplicationResponse appResponse = app.getNewApplicationResponse();
		// TODO get min/max resource capabilities from RM and change memory ask
		// if needed
		// If we do not have min/max, we may not be able to correctly request
		// the required resources from the RM for the app master
		// Memory ask has to be a multiple of min and less than max.
		// Dump out information about cluster capability as seen by the resource
		// manager
		int maxMem = appResponse.getMaximumResourceCapability().getMemory();
		log.info("Max mem capabililty of resources in this cluster " + maxMem);

		// A resource ask cannot exceed the max.
		if (appMasterMemory > maxMem) {
			log.info("AM memory specified above max threshold of cluster. Using max value, specified=" + appMasterMemory
					+ ", max=" + maxMem);
			appMasterMemory = maxMem;
		}

		int maxVCores = appResponse.getMaximumResourceCapability().getVirtualCores();
		log.info("Max virtual cores capabililty of resources in this cluster " + maxVCores);

		if (appMasterVCores > maxVCores) {
			log.info("AM virtual cores specified above max threshold of cluster. Using max value, specified="
					+ appMasterVCores + ", max=" + maxVCores);
			appMasterVCores = maxVCores;
		}

		// set the application name
		ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
		ApplicationId appId = appContext.getApplicationId();

		appContext.setKeepContainersAcrossApplicationAttempts(keepContainers);
		appContext.setApplicationName(appName);
		appContext.setApplicationType(APP_TYPE);

		// Set up the container launch context for the application master
		ContainerLaunchContext appMasterContainer = Records.newRecord(ContainerLaunchContext.class);

		// set local resources for the application master
		// local files or archives as needed
		// In this scenario, the jar file for the application master is part of
		// the local resources
		Map<String, LocalResource> appMasterResources = new HashMap<String, LocalResource>();

		log.info("Copy App Master jar from local filesystem and add to local environment");
		// Copy the application master jar to the filesystem
		// Create a local resource to point to the destination jar path
		FileSystem fs = FileSystem.get(hadoopConf);
		addToLocalResources(fs, appMasterJar, appMasterJarPath, appId.toString(), appMasterResources, null);

		// Set the log4j properties if needed
		if (!log4jPropFile.isEmpty()) {
			addToLocalResources(fs, log4jPropFile, log4jPath, appId.toString(), appMasterResources, null);
		}

		// The shell script has to be made available on the final container(s)
		// where it will be executed.
		// To do this, we need to first copy into the filesystem that is visible
		// to the yarn framework.
		// We do not need to set this as a local resource for the application
		// master as the application master does not need it.
		String hdfsShellScriptLocation = "";
		long hdfsShellScriptLen = 0;
		long hdfsShellScriptTimestamp = 0;
		if (!shellScriptPath.isEmpty()) {
			Path shellSrc = new Path(shellScriptPath);
			String shellPathSuffix = appName + "/" + appId.toString() + "/" + SCRIPT_PATH;
			Path shellDst = new Path(fs.getHomeDirectory(), shellPathSuffix);
			fs.copyFromLocalFile(false, true, shellSrc, shellDst);
			hdfsShellScriptLocation = shellDst.toUri().toString();
			FileStatus shellFileStatus = fs.getFileStatus(shellDst);
			hdfsShellScriptLen = shellFileStatus.getLen();
			hdfsShellScriptTimestamp = shellFileStatus.getModificationTime();
		}

		if (!shellCommand.isEmpty()) {
			addToLocalResources(fs, null, shellCommandPath, appId.toString(), appMasterResources, shellCommand);
		}

		if (shellArgs.length > 0) {
			addToLocalResources(fs, null, shellArgsPath, appId.toString(), appMasterResources,
					StringUtils.join(shellArgs, " "));
		}
		// Set local resource info into app master container launch context
		appMasterContainer.setLocalResources(appMasterResources);

		// Set the necessary security tokens as needed
		// amContainer.setContainerTokens(containerToken);

		// Set the env variables to be setup in the env where the application
		// master will be run
		log.info("Set the environment for the application master");
		Map<String, String> env = new HashMap<>();

		// put location of shell script into env
		// using the env info, the application master will create the correct
		// local resource for the
		// eventual containers that will be launched to execute the shell
		// scripts
		env.put(TENSORFLOW_SCRIPT_LOCATION, hdfsShellScriptLocation);
		env.put(TENSORFLOW_SCRIPT_TIMESTAMP, Long.toString(hdfsShellScriptTimestamp));
		env.put(TENSORFLOW_SCRIPT_LEN, Long.toString(hdfsShellScriptLen));

		// Add AppMaster.jar location to classpath
		// At some point we should not be required to add
		// the hadoop specific classpaths to the env.
		// It should be provided out of the box.
		// For now setting all required classpaths including
		// the classpath to "." for the application jar
		StringBuilder classPathEnv = new StringBuilder(Environment.CLASSPATH.$$())
				.append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
		for (String c : hadoopConf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
			classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
			classPathEnv.append(c.trim());
		}
		classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./log4j.properties");

		// add the runtime classpath needed for tests to work
		if (hadoopConf.getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, false)) {
			classPathEnv.append(':');
			classPathEnv.append(System.getProperty("java.class.path"));
		}

		env.put("CLASSPATH", classPathEnv.toString());

		appMasterContainer.setEnvironment(env);

		Vector<CharSequence> appMasterCmd = new Vector<>(30);

		log.info("Setting up app master command");
		appMasterCmd.add(Environment.JAVA_HOME.$$() + "/bin/java");
		appMasterCmd.add("-Xmx" + appMasterMemory + "m");
		appMasterCmd.add(appMasterMainClass);
		appMasterCmd.add("--container_memory " + String.valueOf(containerMemory));
		appMasterCmd.add("--container_vcores " + String.valueOf(containerVirtualCores));
		appMasterCmd.add("--num_containers " + String.valueOf(numContainers));
		appMasterCmd.add("--priority " + String.valueOf(shellCmdPriority));

		for (Map.Entry<String, String> entry : shellEnv.entrySet()) {
			appMasterCmd.add("--shell_env " + entry.getKey() + "=" + entry.getValue());
		}
		if (debugFlag) {
			appMasterCmd.add("--debug");
		}

		appMasterCmd.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stdout");
		appMasterCmd.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stderr");

		// Get final commmand
		StringBuilder command = new StringBuilder();
		for (CharSequence str : appMasterCmd) {
			command.append(str).append(" ");
		}

		log.info("Completed setting up app master command " + command.toString());
		List<String> commands = new ArrayList<>();
		commands.add(command.toString());
		appMasterContainer.setCommands(commands);

		// Set up resource type requirements
		// For now, both memory and vcores are supported, so we set memory and
		// vcores requirements
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(appMasterMemory);
		capability.setVirtualCores(appMasterVCores);
		appContext.setResource(capability);

		// Service data is a binary blob that can be passed to the application
		// Not needed in this scenario
		// amContainer.setServiceData(serviceData);

		// Setup security tokens
		if (UserGroupInformation.isSecurityEnabled()) {
			Credentials credentials = new Credentials();
			String tokenRenewer = hadoopConf.get(YarnConfiguration.RM_PRINCIPAL);
			if (tokenRenewer == null || tokenRenewer.length() == 0) {
				throw new IOException("Can't get Master Kerberos principal for the RM to use as renewer");
			}

			// For now, only getting tokens for the default file-system.
			final Token<?> tokens[] = fs.addDelegationTokens(tokenRenewer, credentials);
			if (tokens != null) {
				for (Token<?> token : tokens) {
					log.info("Got dt for " + fs.getUri() + "; " + token);
				}
			}
			DataOutputBuffer dob = new DataOutputBuffer();
			credentials.writeTokenStorageToStream(dob);
			ByteBuffer fsTokens = ByteBuffer.wrap(dob.getData(), 0, dob.getLength());
			appMasterContainer.setTokens(fsTokens);
		}

		appContext.setAMContainerSpec(appMasterContainer);

		// Set the priority for the application master
		Priority pri = Records.newRecord(Priority.class);
		// TODO - what is the range for priority? how to decide?
		pri.setPriority(appMasterPriority);
		appContext.setPriority(pri);

		// Set the queue to which this application is to be submitted in the RM
		appContext.setQueue(appMasterQueue);

		// Submit the application to the applications manager
		// SubmitApplicationResponse submitResp =
		// applicationsManager.submitApplication(appRequest);
		// Ignore the response as either a valid response object is returned on
		// success
		// or an exception thrown to denote some form of a failure
		log.info("Submitting application to ASM");

		yarnClient.submitApplication(appContext);

		// TODO
		// Try submitting the same request again
		// app submission failure?

		// Monitor the application
		return monitorApplication(appId);
	}

	private boolean monitorApplication(ApplicationId appId) throws YarnException, IOException {
		while (true) {
			// Check app status every 1 second.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.debug("Thread sleep in monitoring loop interrupted");
			}

			// Get application report for the appId we are interested in
			ApplicationReport report = yarnClient.getApplicationReport(appId);

			log.info("Got application report from ASM for, appId=" + appId.getId() + ", clientToAMToken="
					+ report.getClientToAMToken() + ", appDiagnostics=" + report.getDiagnostics() + ", appMasterHost="
					+ report.getHost() + ", appQueue=" + report.getQueue() + ", appMasterRpcPort=" + report.getRpcPort()
					+ ", appStartTime=" + report.getStartTime() + ", yarnAppState="
					+ report.getYarnApplicationState().toString() + ", distributedFinalState="
					+ report.getFinalApplicationStatus().toString() + ", appTrackingUrl=" + report.getTrackingUrl()
					+ ", appUser=" + report.getUser());

			YarnApplicationState state = report.getYarnApplicationState();
			FinalApplicationStatus dsStatus = report.getFinalApplicationStatus();
			if (YarnApplicationState.FINISHED == state) {
				if (FinalApplicationStatus.SUCCEEDED == dsStatus) {
					log.info("Application has completed successfully. Breaking monitoring loop");
					return true;
				} else {
					log.info("Application did finished unsuccessfully. YarnState=" + state.toString()
							+ ", DSFinalStatus=" + dsStatus.toString() + ". Breaking monitoring loop");
					return false;
				}
			} else if (YarnApplicationState.KILLED == state || YarnApplicationState.FAILED == state) {
				log.info("Application did not finish. YarnState=" + state.toString() + ", DSFinalStatus="
						+ dsStatus.toString() + ". Breaking monitoring loop");
				return false;
			}

			if (System.currentTimeMillis() > (clientStartTime + clientTimeout)) {
				log.info("Reached client specified timeout for application. Killing application");
				forceKillApplication(appId);
				return false;
			}
		}
	}

	private void forceKillApplication(ApplicationId appId) throws YarnException, IOException {
		// TODO clarify whether multiple jobs with the same app id can be
		// submitted and be running at
		// the same time.
		// If yes, can we kill a particular attempt only?

		// Response can be ignored as it is non-null on success or
		// throws an exception in case of failures
		yarnClient.killApplication(appId);
	}

	private void addToLocalResources(FileSystem fs, String fileSrcPath, String fileDstPath, String appId,
			Map<String, LocalResource> localResources, String resources) throws IOException {
		String suffix = appName + "/" + appId + "/" + fileDstPath;
		Path dst = new Path(fs.getHomeDirectory(), suffix);
		if (fileSrcPath == null) {
			FSDataOutputStream ostream = null;
			try {
				ostream = FileSystem.create(fs, dst, new FsPermission((short) 0710));
				ostream.writeUTF(resources);
			} finally {
				IOUtils.closeQuietly(ostream);
			}
		} else {
			fs.copyFromLocalFile(new Path(fileSrcPath), dst);
		}
		FileStatus scFileStatus = fs.getFileStatus(dst);
		LocalResource scRsrc = LocalResource.newInstance(ConverterUtils.getYarnUrlFromURI(dst.toUri()),
				LocalResourceType.FILE, LocalResourceVisibility.APPLICATION, scFileStatus.getLen(),
				scFileStatus.getModificationTime());
		localResources.put(fileDstPath, scRsrc);
	}
}
