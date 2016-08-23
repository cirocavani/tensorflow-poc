load("//serving/tensorflow_serving:serving.bzl", "serving_proto_library")

serving_proto_library(
    name = "skeleton_proto",
    srcs = ["skeleton.proto"],
    has_services = 1,
    cc_api_version = 2,
    cc_grpc_version = 1,
)

cc_binary(
    name = "skeleton-server",
    srcs = [
        "skeleton.cc",
    ],
    linkopts = ["-lm"],
    deps = [
        ":skeleton_proto",
        "@tf_serving//tensorflow_serving/batching:basic_batch_scheduler",
        "@tf_serving//tensorflow_serving/batching:batch_scheduler",
        "@tf_serving//tensorflow_serving/core:manager",
        "@tf_serving//tensorflow_serving/core:servable_handle",
        "@tf_serving//tensorflow_serving/core:servable_id",
        "@tf_serving//tensorflow_serving/servables/tensorflow:simple_servers",
        "@grpc//:grpc++",
        "@org_tensorflow//tensorflow/contrib/session_bundle",
        "@org_tensorflow//tensorflow/contrib/session_bundle:manifest_proto_cc",
        "@org_tensorflow//tensorflow/contrib/session_bundle:signature",
        "@org_tensorflow//tensorflow/core:framework",
        "@org_tensorflow//tensorflow/core:lib",
        "@org_tensorflow//tensorflow/core:protos_all_cc",
        "@org_tensorflow//tensorflow/core:tensorflow",
    ],
)
