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
                write(webSocket, "服务端连接成功");
                sockets.add(webSocket);
            }

            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onMessage(WebSocket webSocket, String msg) {
                System.out.println("接收到客户端消息：" + msg);

                try {
                    JSONParser parser = new JSONParser();
                    Document request = new Document((JSONObject) parser.parse(msg));
                    switch (request.getString("command")) {
                        case Protocol.HANDSHAKE_REQUEST:
                            //TODO
                            break;
                        case Protocol.FILE_CREATE_REQUEST:
                            write(webSocket, serverMain.fileCreateHandler(request));
                            break;
                        case Protocol.FILE_BYTES_REQUEST:
                            //TODO
                            break;
                        case Protocol.DIRECTORY_CREATE_REQUEST:
                            //TODO
                            break;
                        case Protocol.DIRECTORY_DELETE_REQUEST:
                            //TODO
                            break;
                        case Protocol.FILE_DELETE_REQUEST:
                            //TODO
                            break;
                        case Protocol.FILE_MODIFY_REQUEST:
                            //TODO
                            break;
                        default:
                            break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            public void onError(WebSocket webSocket, Exception e) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onStart() {

            }
        };
        socketServer.start();
        System.out.println("listening websocket p2p port on: " + port);
    }

    public void write(WebSocket ws, String message) {
        System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
        ws.send(message);
    }
}
