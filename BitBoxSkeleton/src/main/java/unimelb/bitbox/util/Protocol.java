package unimelb.bitbox.util;

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

    public static Document handShakeRequest(String port) {
        Document document = new Document();
        document.append("command", HANDSHAKE_REQUEST);
        HostPort hostPort = new HostPort(Configuration.getConfigurationValue("advertisedName")+":"+port);
        document.append("hostPort", hostPort.toDoc());
        return document;
    }

    public static Document handShakeResponse() {
        Document document = new Document();
        document.append("command", HANDSHAKE_RESPONSE);   
        return document;
    }

    public static Document fileCreateRequest(Document fileDescriptor, String pathName) {
        Document document = new Document();
        document.append("command", Protocol.FILE_CREATE_REQUEST);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        return document;
    }

    public static Document fileCreateResponse(Document fileDescriptor, String pathName) {
        Document document = new Document();
        document.append("command", Protocol.FILE_CREATE_RESPONSE);
        document.append("fileDescriptor", fileDescriptor);
        document.append("pathName", pathName);
        return document;
    }

    //TODO other protocol
}
