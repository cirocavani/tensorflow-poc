py_library(
    name = "mnist_input_data",
    srcs = ["mnist_input_data.py"],
    visibility = ["//client:__pkg__"],
)

py_binary(
    name = "mnist_export",
    srcs = [
        "mnist_export.py",
    ],
    deps = [
        ":mnist_input_data",
        "@org_tensorflow//tensorflow:tensorflow_py",
        "@org_tensorflow//tensorflow/contrib/session_bundle:exporter",
    ],
)
