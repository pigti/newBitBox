==>COMP 90015 Project 1<==
Team Valar Dohaeris

==>Intro<==
The project is implemented by webSocket and all protocols specified in the
specification, you can assign the path and peer in the config file.

==>Config<==
1. To make debug easier(mutiple instances), you can specify many ports, paths
and peers(like path1 path2 path3 ...) in the config file, and change the
cooresponding value in the peer(n).java file.
2. The program assumes a friendly test environment, so please don't input
invalid values to the config file
3. Please assign your public IP address to the advertisedName.

==>Exceptions<==
1. InetAddress.getLocalHost() may return a desired address when you have
multiple network interface in an OS without preference option, in this situation,
please disable unnecesssary network interfaces
2. If you are running in a local area network and want to connect to a public
IP address, please do port mapping.
3. WebSocket is incompatible with Socket, please don't try connecting to other
implementations.
4. You must create the monitored paths and specify at least 1 peer for the
program before execution
