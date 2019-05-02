package unimelb.bitbox;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.*;

public class Server {
	protected ServerMain serverMain;

	public Server(ServerMain serverMain) {
		this.serverMain = serverMain;
	}

	public void initP2PServer(int port) {
		try {
			WebSocketServer socketServer = new WebSocketServer(
					new InetSocketAddress(InetAddress.getLocalHost(), port)) {
				public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				}

				public void onClose(WebSocket webSocket, int i, String s, boolean b) {
					System.out.println("Connection failed to peer:" + webSocket.getRemoteSocketAddress());
					serverMain.delSocket(webSocket);
				}

				public void onMessage(WebSocket webSocket, ByteBuffer bb) {
					try {
						// Parse UTF-8 message
						byte[] bs = new byte[bb.remaining()];
						bb.get(bs);
						String msg = new String(bs, "UTF-8");
						System.out.println("<Server> Message received. \n<Message> " + msg);
						JSONParser parser = new JSONParser();
						Document request = new Document((JSONObject) parser.parse(msg));
						if (Protocol.valid(request)) {
							switch (request.getString("command")) {
							case Protocol.HANDSHAKE_REQUEST:
								// Refuse the handshake and return the list of peers
								if (serverMain.getSocketSize() < Integer
										.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))) {
									serverMain.checkNewClient((Document) request.get("hostPort"));
									write(webSocket, Protocol.handShakeResponse(serverMain.getAddr()).toString());
									serverMain.addHp(request.get("hostPort"));
								} else if (serverMain.containsSocket(webSocket)) {
									// Send Invalid Protocol
									write(webSocket, Protocol.invalidResponse(1).toString());
									webSocket.close(1, "Invalid message");
									;
								} else {
									ArrayList<HostPort> hp = serverMain.getConnected();
									write(webSocket, Protocol.connectionRefused(hp).toString());
								}
								break;
							case Protocol.FILE_CREATE_REQUEST:
								String response1 = serverMain.createRequestHandler(request);
								write(webSocket, response1);
								request = new Document((JSONObject) parser.parse(response1));
								if (request.getBoolean("status") == true) {
									write(webSocket, serverMain.byteRequestGenerator(request));
								}
								break;
							case Protocol.FILE_BYTES_RESPONSE:
								String result = serverMain.byteResponseHandler(request);
								if (result != null)
									write(webSocket, result);
								else
									System.out.println(
											"<Server> The file transfer is Completed if no error message is shown.");
								break;
							case Protocol.FILE_DELETE_REQUEST:
								write(webSocket, serverMain.deleteRequestHandler(request));
								break;
							case Protocol.DIRECTORY_CREATE_REQUEST:
								write(webSocket, serverMain.createDirRequestHandler(request));
								break;
							case Protocol.DIRECTORY_DELETE_REQUEST:
								write(webSocket, serverMain.deleteDirRequestHandler(request));
								break;
							case Protocol.FILE_MODIFY_REQUEST:
								String response1m = serverMain.modifyFileRequestHandler(request);
								write(webSocket, response1m);
								request = new Document((JSONObject) parser.parse(response1m));
								if (request.getBoolean("status") == true) {
									write(webSocket, serverMain.byteRequestGenerator(request));
								}
								break;
							default:
								break;
							}
						} else {
							// Send Invalid Protocol
							write(webSocket, Protocol.invalidResponse(2).toString());
							webSocket.close(1, "Invalid message");
							;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

				public void onError(WebSocket webSocket, Exception e) {
					System.out.println("<Server> Connection failed to peer:" + webSocket.getRemoteSocketAddress());
				}

				public void onStart() {
				}

				@Override
				public void onMessage(WebSocket conn, String msg) {
					System.out.println("<Warning> Uncoded message Received. \n<Message> " + msg);
					// TODO Auto-generated method stub
				}
			};
			System.out.println(socketServer.getAddress());
			socketServer.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("<Server> Listening websocket p2p port on: " + port);
	}

	// send the message
	public void write(WebSocket ws, String message) {
		System.out.println("<Server> Sending message to port: " + ws.getLocalSocketAddress().getHostString() + " : "
				+ ws.getLocalSocketAddress().getPort() + " -> " + ws.getRemoteSocketAddress().getHostString() + " : "
				+ ws.getRemoteSocketAddress().getPort() + "\n<Message> " + message);
		// Send message in UTF-8
		byte[] bs = message.getBytes(Charset.forName("UTF-8"));
		ByteBuffer bb = ByteBuffer.wrap(bs);
		ws.send(bb);
	}
}
