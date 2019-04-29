package unimelb.bitbox.util;

import java.util.ArrayList;

public class Protocol {
    public static final String INVALID_PROTOCOL = "INVALID_PROTOCOL";
    public static final String CONNECTION_REFUSED = "CONNECTION_REFUSED";
    public static final String HANDSHAKE_REQUEST = "HANDSHAKE_REQUEST";
    public static final String HANDSHAKE_RESPONSE = "HANDSHAKE_RESPONSE";
    public static final String FILE_CREATE_REQUEST = "FILE_CREATE_REQUEST";
    public static final String FILE_CREATE_RESPONSE = "FILE_CREATE_RESPONSE";
    public static final String FILE_DELETE_REQUEST = "FILE_DELETE_REQUEST";
    public static final String FILE_DELETE_RESPONSE = "FILE_DELETE_RESPONSE";
    public static final String FILE_MODIFY_REQUEST = "FILE_MODIFY_REQUEST";
    public static final String FILE_MODIFY_RESPONSE = "FILE_MODIFY_RESPONSE";
    public static final String DIRECTORY_CREATE_REQUEST = "DIRECTORY_CREATE_REQUEST";
    public static final String DIRECTORY_CREATE_RESPONSE = "DIRECTORY_CREATE_RESPONSE";
    public static final String DIRECTORY_DELETE_REQUEST = "DIRECTORY_DELETE_REQUEST";
    public static final String DIRECTORY_DELETE_RESPONSE = "DIRECTORY_DELETE_RESPONSE";
    public static final String FILE_BYTES_REQUEST = "FILE_BYTES_REQUEST";
    public static final String FILE_BYTES_RESPONSE = "FILE_BYTES_RESPONSE";

    public static Document invalidProtocol(String message) {
        Document document = new Document();
        document.append("command", INVALID_PROTOCOL);
        document.append("message", message);
        return document;
    }

    public static Document handShakeRequest(String addr) {
        Document document = new Document();
        document.append("command", HANDSHAKE_REQUEST);
        HostPort hostPort = new HostPort(addr);
        document.append("hostPort", hostPort.toDoc());
        return document;
    }

    public static Document handShakeResponse(String port) {
        Document document = new Document();
        document.append("command", HANDSHAKE_RESPONSE);
        HostPort hostPort = new HostPort(Configuration.getConfigurationValue("advertisedName")+":"+port);
        document.append("hostPort", hostPort.toDoc());
        return document;
    }

    public static Document connectionRefused(ArrayList<HostPort> hp) {
        Document document = new Document();
        document.append("command", CONNECTION_REFUSED);
        document.append("message", "connection limit reached");
        String hpDoc = "[";
        for(HostPort hostPort: hp) {
        	hpDoc += hostPort.toDoc().toString();
        	hpDoc += ",";
        }
        hpDoc = hpDoc.substring(0,hpDoc.length()-2);
        hpDoc += "]";
        document.append("peers", hpDoc);
        return document;
    }

    public static Document fileCreateRequest(Document fileDescriptor, String pathName) {
        Document document = new Document();
        document.append("command", Protocol.FILE_CREATE_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        return document;
    }

    public static Document fileCreateResponse(Document fileDescriptor, String pathName, String message, Boolean status) {
        Document document = new Document();
        document.append("command", Protocol.FILE_CREATE_RESPONSE);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        document.append("message",message);
        document.append("status", status);
        return document;
    }

    public static Document fileBytesRequest(Document fileDescriptor, String pathName, Integer pos, Integer len) {
        Document document = new Document();
        document.append("command", Protocol.FILE_BYTES_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        document.append("position", pos);
        document.append("length", len);
        return document;
    }

    public static Document fileBytesResponse(Document fileDescriptor, String pathName, Integer pos, Integer len,
                                             String cont, String mes, Boolean st) {
        Document document = new Document();
        document.append("command", Protocol.FILE_BYTES_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        document.append("position", pos);
        document.append("length", len);
        document.append("content", cont);
        document.append("message",mes);
        document.append("status", st);
        return document;
    }

    public static Document fileDeleteRequest(Document fileDescriptor, String pathName) {
        Document document = new Document();
        document.append("command", Protocol.FILE_DELETE_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        return document;
    }

    public static Document fileDeleteResponse(Document fileDescriptor, String pathName, String message, Boolean status) {
        Document document = new Document();
        document.append("command", Protocol.FILE_DELETE_RESPONSE);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        document.append("message",message);
        document.append("status", status);
        return document;
    }

    public static Document fileModifyRequest(Document fileDescriptor, String pathName) {
        Document document = new Document();
        document.append("command", Protocol.FILE_MODIFY_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        return document;
    }

    public static Document fileModifyResponse(Document fileDescriptor, String pathName, String message, Boolean status) {
        Document document = new Document();
        document.append("command", Protocol.FILE_MODIFY_RESPONSE);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        document.append("message",message);
        document.append("status", status);
        return document;
    }

    public static Document directoryCreateRequest(String pathName) {
        Document document = new Document();
        document.append("command", Protocol.DIRECTORY_CREATE_REQUEST);
        document.append("pathName", pathName);
        return document;
    }

    public static Document directoryCreateResponse(String pathName, String message, Boolean status) {
        Document document = new Document();
        document.append("command", Protocol.DIRECTORY_CREATE_RESPONSE);
        document.append("pathName", pathName);
        document.append("message",message);
        document.append("status", status);
        return document;
    }

    public static Document directoryDeleteRequest(String pathName) {
        Document document = new Document();
        document.append("command", Protocol.DIRECTORY_DELETE_REQUEST);
        document.append("pathName", pathName);
        return document;
    }

    public static Document directoryDeleteResponse(String pathName, String message, Boolean status) {
        Document document = new Document();
        document.append("command", Protocol.DIRECTORY_DELETE_RESPONSE);
        document.append("pathName", pathName);
        document.append("message",message);
        document.append("status", status);
        return document;
    }
}
