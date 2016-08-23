java_binary(
    name = "yarn-client",
    javacopts = ["-source", "7", "-target", "7"],
    srcs = [
        "Client.java",
        "ClientOptions.java",
        "TFConstants.java",
        "Log4jPropertyHelper.java"
    ],
    classpath_resources	= ["log4j.properties"],
    main_class = "tensorflow.yarn.Client",
    deps = [
        "@hadoop_yarn_client//jar",
        "@hadoop_yarn_api//jar",
        "@hadoop_yarn_common//jar",
        "@hadoop_common//jar",
        "@commons_cli//jar",
        "@commons_io//jar",
        "@commons_lang//jar",
        "@commons_logging//jar",
        "@log4j//jar",
    ],
    runtime_deps = [
        "@hadoop_auth//jar",
        "@hadoop_hdfs//jar",
        "@guava//jar",
        "@commons_collections//jar",
        "@commons_configuration//jar",
        "@slf4j_api//jar",
        "@protobuf_java//jar",
    ],
)

java_binary(
    name = "application-master",
    javacopts = ["-source", "7", "-target", "7"],
    srcs = [
        "ApplicationMaster.java",
        "ApplicationMasterOptions.java",
        "TFConstants.java",
        "Log4jPropertyHelper.java"
    ],
    classpath_resources	= ["log4j.properties"],
    main_class = "tensorflow.yarn.ApplicationMaster",
    deps = [
        "@hadoop_yarn_client//jar",
        "@hadoop_yarn_api//jar",
        "@hadoop_yarn_common//jar",
        "@hadoop_common//jar",
        "@commons_cli//jar",
        "@commons_io//jar",
        "@commons_logging//jar",
        "@log4j//jar",
    ],
    runtime_deps = [
        "@guava//jar",
        "@commons_collections//jar",
    ],
)
