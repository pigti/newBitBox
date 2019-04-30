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

public class Client {
	protected ServerMain serverMain;
	private ArrayList<String> unconnected;

	public Client(ServerMain serverMain, String[] peers) {
		this.serverMain = serverMain;
		unconnected = new ArrayList<>(Arrays.asList(peers));
		Client c = this;
		//Retry when no connections are available
		Thread t = new Thread() {
			public void run() {
				while (true) {
					if (unconnected.size() != 0) {
						for (int i = 0; i < peers.length; i++) {
							String peer = peers[i];
							if (unconnected.contains(peer)) {
								unconnected.remove(peers[i]);
								c.connectToPeer(peers[i]);
							}
						}
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}

	public void connectToPeer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI("ws://" + peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					String locAddr = this.getLocalSocketAddress().toString().substring(1);
					write(this, Protocol.handShakeRequest(locAddr).toString());
				}

				@Override
				public void onMessage(String msg) {
					System.out.println("<Client> Message Received. \n<Message> " + msg);

					try {
						JSONParser parser = new JSONParser();
						Document response = new Document((JSONObject) parser.parse(msg));
						switch (response.getString("command")) {
						case Protocol.INVALID_PROTOCOL:
							this.close();
							// Close the connection when the protocol is invalid
							break;
						case Protocol.CONNECTION_REFUSED:
							// Close the connection when connection is refused
							this.close();
							break;
						case Protocol.HANDSHAKE_RESPONSE:
							// sync after handshake
							serverMain.sync(this);
							// add the socket into the serverMain
							serverMain.addSocket(this);
							break;
						case Protocol.FILE_CREATE_RESPONSE:
							// do Nothing
							break;
						case Protocol.FILE_BYTES_REQUEST:
							write(this, serverMain.fileByteRequestHandler(response));
							break;
						case Protocol.DIRECTORY_CREATE_RESPONSE:
							// noThing to do
							break;
						case Protocol.DIRECTORY_DELETE_RESPONSE:
							// noThing to do
							break;
						case Protocol.FILE_DELETE_RESPONSE:
							// noThing to do
							break;
						case Protocol.FILE_MODIFY_RESPONSE:
							// noThing to do
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
					System.out.println("<Client> Connection closed: " + "\n<Warning> " + msg);
					if (!unconnected.contains(peer)) {
						unconnected.add(peer);
					}
					serverMain.delSocket(this);
				}

				@Override
				public void onError(Exception e) {
					System.out.println("<Client> Connection failed: " + "\n<Error> " + e.getLocalizedMessage());
					if (!unconnected.contains(peer)) {
						unconnected.add(peer);
					}
					serverMain.delSocket(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}

	public void write(WebSocket ws, String message) {
		System.out.println("<Client> Sending message to port: " + ws.getRemoteSocketAddress().getPort() + "\n<Message> "
				+ message);
		ws.send(message);
	}
}
