package unimelb.bitbox;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.*;

public class Server {

	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	protected ServerMain serverMain;

	public Server(ServerMain serverMain) {
		this.serverMain = serverMain;
	}

	public List<WebSocket> getSockets() {
		return sockets;
	}

	public void initP2PServer(int port) {
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
			}

			public void onClose(WebSocket webSocket, int i, String s, boolean b) {
				System.out.println("Connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onMessage(WebSocket webSocket, String msg) {
				System.out.println("<Server> Message received. \n<Message> " + msg);

				try {
					JSONParser parser = new JSONParser();
					Document request = new Document((JSONObject) parser.parse(msg));
					switch (request.getString("command")) {
					case Protocol.HANDSHAKE_REQUEST:
						// Refuse the handshake and return the list of peers
						if (sockets.size() < Integer
								.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))) {
							write(webSocket, Protocol.handShakeResponse(Integer.toString(port)).toString());
							sockets.add(webSocket);
						} else {
							ArrayList<HostPort> hp = new ArrayList<>();
							for (WebSocket ws : sockets) {
								hp.add(new HostPort(ws.getRemoteSocketAddress().toString().substring(1)));
							}
							write(webSocket, Protocol.connectionRefused(hp).toString());
						}
						break;
					case Protocol.FILE_CREATE_REQUEST:
						String response1 = serverMain.createRequestHandler(request);
						write(webSocket, response1);
						request = new Document((JSONObject) parser.parse(response1));
						if(request.getBoolean("status")==true) {
							write(webSocket, serverMain.byteRequestGenerator(request));
						}
						break;
					case Protocol.FILE_BYTES_RESPONSE:
						String result = serverMain.byteResponseHandler(request);
						if (result != null)
							write(webSocket, result);
						else System.out.println("<Server> The file transfer is Completed if no error message is shown.");
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
						if(request.getBoolean("status")==true) {
							write(webSocket, serverMain.byteRequestGenerator(request));
						}
						break;
					default:
						break;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			public void onError(WebSocket webSocket, Exception e) {
				System.out.println("<Server> Connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onStart() {
			}
		};
		socketServer.start();
		System.out.println("<Server> Listening websocket p2p port on: " + port);
	}

	public void write(WebSocket ws, String message) {
		System.out.println("<Server> Sending message to port: " + ws.getRemoteSocketAddress().getPort() + "\n<Message> "
				+ message);
		ws.send(message);
	}
}
