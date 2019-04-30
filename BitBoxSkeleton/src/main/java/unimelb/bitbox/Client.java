package unimelb.bitbox;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client implements Runnable {

	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	protected ServerMain serverMain;
	private String[] peers;
	private ArrayList<String> unconnected;

	public Client(ServerMain serverMain, String[] peers) {
		this.serverMain = serverMain;
		this.peers = peers;
		unconnected = new ArrayList<>(Arrays.asList(peers));
		this.run();
	}

	public List<WebSocket> getSockets() {
		return sockets;
	}

	public void run() {
		while (unconnected.size() != 0) {
			for (int i = 0; i < peers.length; i++) {
				String peer = peers[i];
				if (unconnected.contains(peer)) {
					unconnected.remove(peers[i]);
					this.connectToPeer(peers[i]);
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void connectToPeer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI("ws://" + peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					String locAddr = this.getLocalSocketAddress().toString().substring(1);
					write(this, Protocol.handShakeRequest(locAddr).toString());
					sockets.add(this);
				}

				@Override
				public void onMessage(String msg) {
					System.out.println("<Client> Message Received. \n<Message> " + msg);

					try {
						JSONParser parser = new JSONParser();
						Document response = new Document((JSONObject) parser.parse(msg));
						switch (response.getString("command")) {
						case Protocol.INVALID_PROTOCOL:
							// TODO
							break;
						case Protocol.CONNECTION_REFUSED:
							// TODO
							break;
						case Protocol.HANDSHAKE_RESPONSE:
							// TODO
							break;
						case Protocol.FILE_CREATE_RESPONSE:
							if((response.getBoolean("status") == true)) {
								System.out.println("File loader is ready");
							} else {
								System.out.println("<Client> File Create Failed : "  + "\n<Exception> " + response.getString("message"));
							}
							break;
						case Protocol.FILE_BYTES_REQUEST:
							write(this,serverMain.fileByteRequestHandler(response));
							break;
						case Protocol.DIRECTORY_CREATE_RESPONSE:
							// TODO
							break;
						case Protocol.DIRECTORY_DELETE_RESPONSE:
							// TODO
							break;
						case Protocol.FILE_DELETE_RESPONSE:
							// TODO
							break;
						case Protocol.FILE_MODIFY_RESPONSE:
							// TODO
							break;
						default:
							break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					System.out.println("<Client> Connection closed: "  + "\n<Warning> " + msg);
					sockets.remove(this);
					unconnected.add(peer);
				}

				@Override
				public void onError(Exception e) {
					System.out.println("<Client> Connection failed: " + "\n<Error> " + e.getLocalizedMessage());
					sockets.remove(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}

	public void write(WebSocket ws, String message) {
		System.out.println("<Client> Sending message to port: " + ws.getRemoteSocketAddress().getPort() + "\n<Message> " + message);
		ws.send(message);
	}

	public void broadcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("===Start===");
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("===End===");
	}
}
