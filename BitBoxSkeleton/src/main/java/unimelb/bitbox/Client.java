package unimelb.bitbox;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
	protected ServerMain serverMain;
	private ArrayList<String> unconnected;
	private ArrayList<String> tobeConnected;
	boolean tryConnecting = false;

	public Client(ServerMain serverMain, String[] peers) {
		this.serverMain = serverMain;
		unconnected = new ArrayList<>(Arrays.asList(peers));
		tobeConnected = new ArrayList<>(Arrays.asList(peers));
		Client c = this;
		// Retry when no connections are available
		Thread t = new Thread() {
			public void run() {
				while (true) {
					if (unconnected.size() != 0 && serverMain.getSocketSize() < Integer
							.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"))) {
						tryConnecting = true;
						for (int i = 0; i < tobeConnected.size(); i++) {
							String peer = tobeConnected.get(i);
							if (unconnected.contains(peer)) {
								unconnected.remove(peer);
								c.connectToPeer(peer);
							}
						}
					} else
						tryConnecting = false;
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
	
	//try connecting as a client
	public void connectToPeer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI("ws://" + peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// The only use of client is to start the handShakeRequest from any port
					System.out.println("Connected");
					write(this, Protocol.handShakeRequest(serverMain.getAddr()).toString());
				}

				@Override
				public void onMessage(ByteBuffer bb) {
					try {
						// Parse UTF-8 message
						byte[] bs = new byte[bb.remaining()];
						bb.get(bs);
						String msg = new String(bs, "UTF-8");
						System.out.println("<Client> Message Received. \n<Message> " + msg);
						JSONParser parser = new JSONParser();
						Document response = new Document((JSONObject) parser.parse(msg));
						switch (response.getString("command")) {
						case Protocol.INVALID_PROTOCOL:
							// Close the connection when the protocol is invalid
							this.close(1, "Invalid message");
							break;
						case Protocol.CONNECTION_REFUSED:
							// Close the connection when connection is refused
							this.close(1, "Connection Refused");
							@SuppressWarnings("unchecked") 
							ArrayList<Document> otherPeers = (ArrayList<Document>)response.get("hostPort");
							for(Document peer : otherPeers) {
								addPeer(peer);
							}
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
						case Protocol.DIRECTORY_DELETE_RESPONSE:
						case Protocol.FILE_DELETE_RESPONSE:
						case Protocol.FILE_MODIFY_RESPONSE:
							// noThing to do
							break;
						default:
							break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					if (!tryConnecting) {
						System.out.println("<Client> Connection closed: " + "\n<Warning> " + msg);
					}
					if (!unconnected.contains(peer)) {
						unconnected.add(peer);
					}
					serverMain.delSocket(this);
				}

				@Override
				public void onError(Exception e) {
					if (!tryConnecting) {
						System.out.println("<Client> Connection failed: " + "\n<Error> " + e.getLocalizedMessage());
					}
					if (!unconnected.contains(peer)) {
						unconnected.add(peer);
					}
					serverMain.delSocket(this);
				}

				@Override
				public void onMessage(String msg) {
					System.out.println("<Warning> Uncoded message Received. \n<Message> " + msg);
					// TODO Auto-generated method stub
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}

	// add a new client to be connected
	public void addPeer(Document hp) {
		String parsedHp = (new HostPort(hp)).toString();
		if (parsedHp != null) {
			if (!tobeConnected.contains(parsedHp)) {
				tobeConnected.add(parsedHp);
				unconnected.add(parsedHp);
			}
		} else {
			System.out.println("<Error> Host & Port unable to parse.");
		}
	}
	
	// send the message
	public void write(WebSocket ws, String message) {
		System.out.println("<Client> Sending message to port: " + ws.getLocalSocketAddress().getHostString() + " : "
				+ ws.getLocalSocketAddress().getPort() + " -> " + ws.getRemoteSocketAddress().getHostString() + " : "
				+ ws.getRemoteSocketAddress().getPort() + "\n<Message> " + message);
		//Send message in UTF-8
		byte[] bs = message.getBytes( Charset.forName("UTF-8"));
		ByteBuffer bb = ByteBuffer.wrap(bs);
		ws.send(bb);
	}
}
