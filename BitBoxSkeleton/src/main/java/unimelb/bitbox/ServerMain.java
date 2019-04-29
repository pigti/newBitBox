package unimelb.bitbox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	protected FileSystemManager fileSystemManager;
	protected Server peerServer;
	protected Client peerClient;

	public ServerMain(int serverPort, String[] peers, String path)
			throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager = new FileSystemManager(path, this);
		initServer(serverPort);
		initClient(peers);
	}

	private void initServer(int serverPort) {
		peerServer = new Server(this);
		peerServer.initP2PServer(serverPort);
	}

	public void initClient(String[] peers) {
		peerClient = new Client(this, peers);
	}

	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
		// process events
		System.out.println(fileSystemEvent);
		switch (fileSystemEvent.event) {
		case FILE_CREATE:
			fileCreateHandler(fileSystemEvent);
			break;
		case FILE_DELETE:
			fileDeleteHandler(fileSystemEvent);
			break;
		case DIRECTORY_CREATE:
			// TODO
			break;
		case DIRECTORY_DELETE:
			// TODO
			break;
		case FILE_MODIFY:
			// TODO
			break;
		default:
			// TODO
			break;
		}
	}

	//Handler for a file create <event>, generate a <request> for create a file
	private void fileCreateHandler(FileSystemEvent fileSystemEvent) {
		Document request = Protocol.fileCreateRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
		peerClient.broadcast(request.toJson());
	}
	
	//Handler for a file create <request>, <response> the status of the file loader for a new file
	public String createRequestHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		String md5 = fileDescriptor.getString("md5");
		long fileSize = fileDescriptor.getLong("fileSize");
		long lastModified = fileDescriptor.getLong("lastModified");

		Document response = Protocol.fileCreateResponse(fileDescriptor, pathName);

		try {
			String message;
			boolean status;
			if (!fileSystemManager.isSafePathName(pathName)) {
				message = "Unsafe pathname given";
				status = false;
			} else if (fileSystemManager.checkShortcut(pathName)){
				message = "ShortCut Found.";
				status = false;
			} else {
				status = fileSystemManager.createFileLoader(pathName, md5, fileSize, lastModified);
				message = "file loader ready";
				if (!status) {
					if (fileSystemManager.fileNameExists(pathName, md5)) {
						message = "file already existed";
					} else {
						message = "file is loading";
					}
				}
			}
			response.append("message", message);
			response.append("status", status);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Handle exceptions with different message
			response.append("message", "internal server error");
			response.append("status", "false");
		}
		return response.toJson();
	}
	
	/*Handler for a file create <response>, which can be regarded as a request
	  <response> the file bytes regarding to the response*/
	public String createResponseHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		String md5 = fileDescriptor.getString("md5");
		long fileSize = fileDescriptor.getLong("fileSize");

		Document response = Protocol.fileBytesResponse(fileDescriptor, pathName);
		try {
			long max = Long.parseLong(Configuration.getConfigurationValue("blockSize"));
			long length = fileSize <= max ? fileSize : max;
			ByteBuffer bb = fileSystemManager.readFile(md5, 0, length);
			//Use base 64 encoder to encode the content
			if(bb!=null) {
			response.append("length", length);
			Base64.Encoder encoder = Base64.getEncoder();
			bb.position(0);
			byte[]bs = new byte[bb.remaining()];
			bb.get(bs);
			String content = encoder.encodeToString(bs);
			response.append("content", content);
			response.append("status", "true");
			response.append("message", "successful read");
			} else {
				response.append("length", 0);
				response.append("content", "");
				response.append("status", "false");
				response.append("message", "unsuccessful read");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Handle exceptions with different message
			response.append("message", "internal server error");
			response.append("status", "false");
		}
		return response.toJson();
	}

	private void fileDeleteHandler(FileSystemEvent fileSystemEvent) {
		
	}

	public void fileDeleteHandler(Document request) {

	}

	private void directoryCreateHandler(FileSystemEvent fileSystemEvent) {

	}

	public void directoryCreateHandler(Document request) {

	}

	private void directoryDeleteHandler(FileSystemEvent fileSystemEvent) {

	}

	public void directoryDeleteHandler(Document request) {

	}

	private void fileModifyHandler(FileSystemEvent fileSystemEvent) {

	}

	public void fileModifyHandler(Document request) {

	}

	private void invalidEventHandler(Document request) {

	}

}
