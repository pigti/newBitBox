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
import java.util.List;

public class Client {

    private List<WebSocket> sockets = new ArrayList<WebSocket>();

    public List<WebSocket> getSockets() {
        return sockets;
    }

    public void connectToPeer(String peer) {
        try {
            final WebSocketClient socketClient = new WebSocketClient(new URI("ws://" + peer)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    write(this, "客户端连接成功");
                    sockets.add(this);
                }

                @Override
                public void onMessage(String msg) {
                    System.out.println("收到服务端发送的消息:" + msg);

                    try {
                        JSONParser parser = new JSONParser();
                        Document response = new Document((JSONObject) parser.parse(msg));
                        switch (response.getString("command")) {
                            case Protocol.INVALID_PROTOCOL:
                                //TODO
                                break;
                            case Protocol.CONNECTION_REFUSED:
                                //TODO
                                break;
                            case Protocol.HANDSHAKE_RESPONSE:
                                //TODO
                                break;
                            case Protocol.FILE_CREATE_RESPONSE:
                                //TODO
                                break;
                            case Protocol.FILE_BYTES_RESPONSE:
                                //TODO
                                break;
                            case Protocol.DIRECTORY_CREATE_RESPONSE:
                                //TODO
                                break;
                            case Protocol.DIRECTORY_DELETE_RESPONSE:
                                //TODO
                                break;
                            case Protocol.FILE_DELETE_RESPONSE:
                                //TODO
                                break;
                            case Protocol.FILE_MODIFY_RESPONSE:
                                //TODO
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
                    System.out.println("connection failed " + msg);
                    sockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("connection failed" + e.getLocalizedMessage());
                    sockets.remove(this);
                }
            };
            socketClient.connect();
        } catch (URISyntaxException e) {
            System.out.println("p2p connect is error:" + e.getMessage());
        }
    }

    public void write(WebSocket ws, String message) {
        System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
        ws.send(message);
    }

    public void broadcast(String message) {
        if (sockets.size() == 0) {
            return;
        }
        System.out.println("======广播消息开始：");
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
        System.out.println("======广播消息结束");
    }
}