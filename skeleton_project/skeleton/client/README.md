Generating `skeleton_pb2.py`:

    pip install grpcio-tools
    python -m grpc.tools.protoc -I../server --python_out=. --grpc_python_out=. ../server/skeleton.proto
