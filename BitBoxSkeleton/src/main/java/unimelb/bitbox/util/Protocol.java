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

	public static Document handShakeRequest(HostPort hp) {
		Document document = new Document();
		//Give advertised Host name, especially when running in an internal network
		hp.host = Configuration.getConfigurationValue("advertisedName");
		document.append("command", HANDSHAKE_REQUEST);
		document.append("hostPort", hp.toDoc());
		return document;
	}

	public static Document handShakeResponse(HostPort hp) {
		Document document = new Document();
		document.append("command", HANDSHAKE_RESPONSE);
		document.append("hostPort", hp.toDoc());
		return document;
	}

	public static Document connectionRefused(ArrayList<HostPort> hp) {
		Document document = new Document();
		document.append("command", CONNECTION_REFUSED);
		document.append("message", "connection limit reached");
		ArrayList<Document> docs = new ArrayList<>();
		for (HostPort hostPort : hp) {
			docs.add(hostPort.toDoc());
		}
		document.append("hostPort", docs);
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

	public static Document fileBytesRequest(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_BYTES_REQUEST);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		return document;
	}

	public static Document fileBytesResponse(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_BYTES_RESPONSE);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		document.append("position", "0");
		return document;
	}

	public static Document fileDeleteRequest(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_DELETE_REQUEST);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		return document;
	}

	public static Document fileDeleteResponse(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_DELETE_RESPONSE);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		return document;
	}

	public static Document dirCreateRequest(String pathName) {
		Document document = new Document();
		document.append("command", Protocol.DIRECTORY_CREATE_REQUEST);
		document.append("pathName", pathName);
		return document;
	}

	public static Document dirCreateResponse(String pathName) {
		Document document = new Document();
		document.append("command", Protocol.DIRECTORY_CREATE_RESPONSE);
		document.append("pathName", pathName);
		return document;
	}

	public static Document dirDeleteRequest(String pathName) {
		Document document = new Document();
		document.append("command", Protocol.DIRECTORY_DELETE_REQUEST);
		document.append("pathName", pathName);
		return document;
	}

	public static Document dirDeleteResponse(String pathName) {
		Document document = new Document();
		document.append("command", Protocol.DIRECTORY_DELETE_RESPONSE);
		document.append("pathName", pathName);
		return document;
	}

	public static Document fileModifyRequest(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_MODIFY_REQUEST);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		return document;
	}

	public static Document fileModifyResponse(Document fileDescriptor, String pathName) {
		Document document = new Document();
		document.append("command", Protocol.FILE_MODIFY_RESPONSE);
		document.append("fileDescriptor", fileDescriptor);
		document.append("pathName", pathName);
		return document;
	}

	public static Document invalidResponse(int errorCode) {
		Document document = new Document();
		document.append("command", Protocol.INVALID_PROTOCOL);
		switch (errorCode) {
		// 1 for handshake_request after handshaking
		case 1:
			document.append("message", "handshake_request after handshaking request has been completed");
		// 2 for a message missing field
		case 2:
			document.append("message", "a message does not contain required field of the required type");
		}
		return document;
	}

	public static boolean valid(Document request) {
		if (request.containsKey("command")) {
			switch (request.getString("command")) {
			case Protocol.HANDSHAKE_REQUEST:
				if (request.containsKey("hostPort"))
					return true;
				else
					return false;
			case Protocol.FILE_CREATE_REQUEST:
			case Protocol.FILE_DELETE_REQUEST:
				if (request.containsKey("fileDescriptor") && request.containsKey("pathName"))
					return true;
				else
					return false;
			case Protocol.FILE_BYTES_RESPONSE:
				if (request.containsKey("fileDescriptor") && request.containsKey("pathName")
						&& request.containsKey("position") && request.containsKey("length")
						&& request.containsKey("content") && request.containsKey("message")
						&& request.containsKey("status"))
					return true;
				else
					return false;
			case Protocol.DIRECTORY_CREATE_REQUEST:
			case Protocol.DIRECTORY_DELETE_REQUEST:
			case Protocol.FILE_MODIFY_REQUEST:
				if (request.containsKey("pathName"))
					return true;
				else
					return false;
			default:
				return false;
			}
		} else
			return false;
	}
}
