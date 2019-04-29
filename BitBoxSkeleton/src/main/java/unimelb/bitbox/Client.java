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
	private String[] peers;
	private ArrayList<String> unconnected;

	public Client(String[] peers) {
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
					write(this, Protocol.handShakeRequest(peer.split(":")[1]).toString());
					sockets.add(this);
				}

				@Override
				public void onMessage(String msg) {
					System.out.println("Message received from the server: \nMessage: " + msg);

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
							// TODO
							break;
						case Protocol.FILE_BYTES_RESPONSE:
							// TODO
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
					System.out.println("Client connection closed: " + msg);
					sockets.remove(this);
					unconnected.add(peer);
				}

				@Override
				public void onError(Exception e) {
					System.out.println("Client connection failed: " + e.getLocalizedMessage());
					sockets.remove(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}

	public void write(WebSocket ws, String message) {
		System.out.println("Client is sending to port: " + ws.getRemoteSocketAddress().getPort() + "\nMessage:" + message);
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
