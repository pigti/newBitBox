==>COMP 90015 Project 1<==
Team Valar Dohaeris

==>Intro<==
The project is implemented by WebSocket and all protocols specified in the
specification, you can assign the path and peer in the config file.

==>Config<==
1. To make debugging easier(multiple instances), you can specify many ports,
paths and peers(like path1 path2 path3 ...) in the config file, and change the
corresponding value in the peer(n).java file.
2. The program assumes a friendly test environment, so please don't input
invalid values and malicious address to the config file
3. Please assign your public IP address to the advertisedName.

==>Does WebSocket Satisfy the specification?<==
Q1. All communication will be via persistent TCP connections between the peers?
A1. WebSocket is an Application level protocol based on a TCP connection. We
implements our protocol message in the field of the message of this protocol.

Q2. All messages will be in JSON format, one JSON message per line, i.e. the
JSON object is followed by a new line character?
A2. The JSON objected is encoded into the message in ByteBuffer Format, and it
is the only message sent across the peers.

Q3. The text encoding for messages will be UTF-8: e.g. new BufferedWriter(new
OutputStreamWriter(socket.getOutputStream(), "UTF8"))?
A3. All String is converted into ByteBuffer Format with "UTF-8" charSet before
sent to the peer

Q4. File contents will be transmitted inside JSON using Base64 encoding?
A4. All File contents are encoded by Base64.Encoder.

Q5. Interactions will in the most part be asynchronous request/reply between
peers?
A5. Interactions are all based on protocol messages in an asynchronous way.

Q6. Can the WebSocket connect to a simple TCP implements?
A6. No, for security purpose and protocol standard, the WebSocket can only
connect to a WebSocket Instance.

Q7. Why chose WebSocket Implementation?
A7. We think that the BitBot should work on a persistent protocol to overcome
the concurrency challenges. Also, we want to try some recent protocols and learn
the latest technologies in the industry. Our mentor suggests it's OK to use
external libraries.

==>Exceptions<==
1. InetAddress.getLocalHost() may return a desired address when you have
multiple network interface in an OS without preference option, in this situation,
please disable unnecessary network interfaces
2. If you are running in a local area network and want to connect to a public
IP address, please do port mapping.
3. WebSocket is incompatible with Socket, please don't try connecting to other
implementations.
4. You must create the monitored paths and specify at least one peer for the
program before execution
