# FileTransferProject
This is a simple client-server project in Java. This project will be used to transfer files from client to server. Server will be listening on a port (say port# 5050) and Client will connect to Server and transfer files to Server. Here are details:

1. Client will take source folder path (i.e., folder that contains a set of files)  and the number of concurrent file transfer count as command line arguments. For example, "java Client myFolder 5" will transfer files in “myFolder” folder to destination server five at a time. In other words, concurrent file transfer means transferring multiple files over separate connections to increase overall throughput. If concurrency number is not entered, the applciation should transfer one file at a time (aka concurrency=1), by default.

2. The application should support integrity verification. That is, the client and server will calculate checksum of each file after it is transferred and compare them to make sure data is transferred without any error in the network.
