all:
	make compile_slice
	make compile_proto

compile_slice:
	slice2java --output-dir src/main/java/ src/main/resources/slice/bank.ice

compile_proto:
	protoc -I=. --java_out=src/main/java/ --plugin=protoc-gen-grpc-java=./protoc-gen-grpc-java-1.20.0-linux-x86_32.exe --grpc-java_out=src/main/java src/main/resources/proto/exchange.proto
