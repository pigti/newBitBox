package unimelb.bitbox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import org.java_websocket.WebSocket;

import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	// Track the sockets for avoiding duplicates and tracking the number of connections
	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	// Track the server info of current conncetions
	private ArrayList<HostPort> connected = new ArrayList<HostPort>();
	protected FileSystemManager fileSystemManager;
	// The address of recipient port the current server
	protected HostPort serverAddr;
	protected Server peerServer;
	protected Client peerClient;

	public ServerMain(int serverPort, String[] peers, String path)
			throws NumberFormatException, IOException, NoSuchAlgorithmException {
		serverAddr = new HostPort(Configuration.getConfigurationValue("advertisedName"), serverPort);
		fileSystemManager = new FileSystemManager(path, this);
		initServer(serverPort);
		initClient(peers);
		//A thread which sync the directory periodically
		Thread t = new Thread() {
			public void run() {
				while (true) {
					if (sockets.size() != 0) {
						sync();
					}
					try {
						int interval = Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000;
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}

	// Simple initialisation methods and setter and getters
	private void initServer(int serverPort) {
		peerServer = new Server(this);
		peerServer.initP2PServer(serverPort);
	}

	public void initClient(String[] peers) {
		peerClient = new Client(this, peers);
	}
	
	public void addHp(Object o) {
		if(o instanceof Document) {
			connected.add(new HostPort((Document)o));
		}
	}
	
	public void addSocket(WebSocket ws) {
		sockets.add(ws);
	}
	
	public void delSocket(WebSocket ws) {
		if(sockets.contains(ws)){
			sockets.remove(ws);
		};
	}
	
	public int getSocketSize() {
		return sockets.size();
	}
	
	public ArrayList<HostPort> getConnected(){
		return connected;
	}
	
	public HostPort getAddr() {
		return serverAddr;
	}
	
	//check if the server have shaked with this client before
	public boolean containsSocket(WebSocket ws) {
		for (WebSocket client : sockets) {
			String shaked = client.getLocalSocketAddress().toString().substring(1);
			String newClient = ws.getRemoteSocketAddress().toString().substring(1);
			if(shaked.equals(newClient)) return true;
		}
		return false;
	}
	
	// broadcast the message to all subscribors
	public void broadcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("===Broadcast Start===");
		for (WebSocket socket : sockets) {
			peerClient.write(socket, message);
		}
		System.out.println("===Broadcast End===");
	}
	
	//General & periodically sync
	public void sync() {
		ArrayList<FileSystemEvent> pathevents = fileSystemManager.generateSyncEvents();
		System.out.println("===Sync Begins===");
		for(FileSystemEvent pathevent : pathevents) {
			log.info(pathevent.toString());
			processFileSystemEvent(pathevent);
		}
		System.out.println("===Sync Ends===");
	}
	
	//First Connection sync after handshake
	public void sync(WebSocket ws) {
		ArrayList<FileSystemEvent> pathevents = fileSystemManager.generateSyncEvents();
		System.out.println("===Sync Begins===");
		for(FileSystemEvent pathevent : pathevents) {
			log.info(pathevent.toString());
			processFileSystemEvent(pathevent, ws);
		}
		System.out.println("===Sync Ends===");
	}
	
	//If the peer is not on the client list, add it to perform a second handshake
	public void checkNewClient(Document hp) {
		peerClient.addPeer(hp);
	}

	//Broadcast to only the client of the new connection
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent, WebSocket ws) {
		// process events
		System.out.println(fileSystemEvent);
		Document request = null;
		switch (fileSystemEvent.event) {
		case FILE_CREATE:
			request = Protocol.fileCreateRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		case FILE_DELETE:
			request = Protocol.fileDeleteRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		case DIRECTORY_CREATE:
			request = Protocol.dirCreateRequest(fileSystemEvent.pathName);
			break;
		case DIRECTORY_DELETE:
			request = Protocol.dirDeleteRequest(fileSystemEvent.pathName);
			break;
		case FILE_MODIFY:
			request = Protocol.fileModifyRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		default:
			// TODO
			break;
		}
		if (request != null)
			peerServer.write(ws, request.toJson());
	}
	
	@Override
	//Broadcast to all subscribers
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
		// process events
		System.out.println(fileSystemEvent);
		Document request = null;
		switch (fileSystemEvent.event) {
		case FILE_CREATE:
			request = Protocol.fileCreateRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		case FILE_DELETE:
			request = Protocol.fileDeleteRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		case DIRECTORY_CREATE:
			request = Protocol.dirCreateRequest(fileSystemEvent.pathName);
			break;
		case DIRECTORY_DELETE:
			request = Protocol.dirDeleteRequest(fileSystemEvent.pathName);
			break;
		case FILE_MODIFY:
			request = Protocol.fileModifyRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
			break;
		default:
			// TODO
			break;
		}
		if (request != null)
			broadcast(request.toJson());
	}

	// Handler for a file create <request>, <response> the status of the file loader
	// for a new file
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
			} else if (fileSystemManager.checkShortcut(pathName)) {
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

	// Handler for a file delete <request>, <response> the status of the file loader
	// for a new file
	public String deleteRequestHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		String md5 = fileDescriptor.getString("md5");
		long lastModified = fileDescriptor.getLong("lastModified");

		Document response = Protocol.fileDeleteResponse(fileDescriptor, pathName);

		try {
			String message;
			boolean status;
			if (!fileSystemManager.isSafePathName(pathName)) {
				message = "Unsafe pathname given";
				status = false;
			} else {
				status = fileSystemManager.deleteFile(pathName, lastModified, md5);
				message = "deleted successfully";
				if (!status) {
					message = "file does not exist";
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

	// Generate a <request> for data, after creating a file loading or receive a
	// piece of data
	public String byteRequestGenerator(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		long fileSize = fileDescriptor.getLong("fileSize");
		long max = Long.parseLong(Configuration.getConfigurationValue("blockSize"));
		long length;

		Document response = Protocol.fileBytesRequest(fileDescriptor, pathName);

		if (request.getString("command").equals(Protocol.FILE_CREATE_RESPONSE)
				|| request.getString("command").equals(Protocol.FILE_MODIFY_RESPONSE)) {
			length = fileSize <= max ? fileSize : max;
			response.append("position", 0);
			response.append("length", length);
		} else if (request.getString("command").equals(Protocol.FILE_BYTES_RESPONSE)) {
			long position = request.getLong("length") + request.getLong("position");
			length = fileSize <= (position + max) ? fileSize : (position + max);
			length -= position;
			response.append("position", position);
			response.append("length", length);
		}
		return response.toJson();
	}

	/*
	 * Handler for a file create <response>, which can be regarded as a request
	 * <response> the file bytes regarding to the response
	 */
	public String fileByteRequestHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		String md5 = fileDescriptor.getString("md5");
		long fileSize = fileDescriptor.getLong("fileSize");
		long position = request.getLong("position");
		long length = request.getLong("length");

		Document response = Protocol.fileBytesResponse(fileDescriptor, pathName);
		try {
			ByteBuffer bb = fileSystemManager.readFile(md5, position, length);
			// Use base 64 encoder to encode the content
			if (bb != null && position + length <= fileSize) {
				response.append("position", position);
				response.append("length", length);
				Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
				bb.position(0);
				byte[] bs = new byte[bb.remaining()];
				bb.get(bs);
				String content = encoder.encodeToString(bs);
				response.append("content", content);
				response.append("status", "true");
				response.append("message", "successful read");
			} else {
				response.append("position", 0);
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

	/*
	 * Handler for a byte file <response>, which decodes the response, write to the
	 * file, and commit the change if file write completed
	 */
	public String byteResponseHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		long fileSize = fileDescriptor.getLong("fileSize");
		String content = request.getString("content");
		long position = request.getLong("position");
		long length = request.getLong("length");
		try {
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] bs = new byte[(int) length];
			bs = decoder.decode(content);
			ByteBuffer bb = ByteBuffer.wrap(bs);
			boolean status = fileSystemManager.writeFile(pathName, bb, position);
			if (!status)
				throw new Exception("Write Failure");
			if (position + length < fileSize)
				return byteRequestGenerator(request);
			else {
				if (!status)
					throw new Exception("Write Failure");
				status = fileSystemManager.checkWriteComplete(pathName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("<Error> Write Byte Failure!");
		}
		return null;
	}
	
	/*
	 * Handler for a create Directory <request>, which builds 
	 * a directory if possible
	 */
	public String createDirRequestHandler(Document request) {
		String pathName = request.getString("pathName");

		Document response = Protocol.dirCreateResponse(pathName);

		try {
			String message;
			boolean status;
			if (!fileSystemManager.isSafePathName(pathName)) {
				message = "Unsafe pathname given";
				status = false;
			} else {
				status = fileSystemManager.makeDirectory(pathName);
				message = "directory created";
				if (!status) {
					message = "pathname alreay exist";
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

	/*
	 * Handler for a delete Directory <request>, which deletes 
	 * a directory if possible
	 */
	public String deleteDirRequestHandler(Document request) {
		String pathName = request.getString("pathName");

		Document response = Protocol.dirDeleteResponse(pathName);
		try {
			String message;
			boolean status;
			if (!fileSystemManager.isSafePathName(pathName)) {
				message = "Unsafe pathname given";
				status = false;
			} else {
				status = fileSystemManager.deleteDirectory(pathName);
				message = "directory created";
				if (!status) {
					message = "pathname alreay deleted";
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
	
	/*
	 * Handler for a modify Directory <request>, which get the modify container
	 * ready if possible
	 */
	public String modifyFileRequestHandler(Document request) {
		Document fileDescriptor = (Document) request.get("fileDescriptor");
		String pathName = request.getString("pathName");
		String md5 = fileDescriptor.getString("md5");
		long lastModified = fileDescriptor.getLong("lastModified");

		Document response = Protocol.fileModifyResponse(fileDescriptor, pathName);

		try {
			String message;
			boolean status;

			if (!fileSystemManager.isSafePathName(pathName)) {
				message = "Unsafe pathname given";
				status = false;
			} else {
				status = fileSystemManager.modifyFileLoader(pathName, md5, lastModified);
				message = "file loader ready";
				if (!status) {
					message = "it is not a valid modification";
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
}
