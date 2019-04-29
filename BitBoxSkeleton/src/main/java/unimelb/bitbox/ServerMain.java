package unimelb.bitbox;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	protected FileSystemManager fileSystemManager;
	protected Server peerServer;
	protected Client peerClient;

	public ServerMain(int serverPort, String[] peers) throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
		initServer(serverPort);
		initClient(peers);
	}

	private void initServer(int serverPort) {
        peerServer = new Server(this);
        peerServer.initP2PServer(serverPort);
    }

    public void initClient(String[] peers) {
        peerClient = new Client(peers);
    }

	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
		//process events
        System.out.println(fileSystemEvent);
        switch (fileSystemEvent.event) {
            case FILE_CREATE:
                fileCreateHandler(fileSystemEvent);
                break;
            case FILE_DELETE:
                fileDeleteHandler(fileSystemEvent);
                break;
            case DIRECTORY_CREATE:
                //TODO
                break;
            case DIRECTORY_DELETE:
                //TODO
                break;
            case FILE_MODIFY:
                //TODO
                break;
            default:
                //TODO
                break;
        }
	}

	private void fileCreateHandler(FileSystemEvent fileSystemEvent) {
        Document request = Protocol.fileCreateRequest(fileSystemEvent.fileDescriptor.toDoc(), fileSystemEvent.pathName);
        peerClient.broadcast(request.toJson());
    }

    public String fileCreateHandler(Document request) {
	    Document fileDescriptor = (Document) request.get("fileDescriptor");
	    String pathName = request.getString("pathName");
	    String md5 = fileDescriptor.getString("md5");
	    long fileSize = fileDescriptor.getLong("fileSize");
	    long lastModified = fileDescriptor.getLong("lastModified");

        Document response = Protocol.fileCreateResponse(fileDescriptor, pathName);

	    try {
            boolean status = fileSystemManager.createFileLoader(pathName, md5, fileSize, lastModified);
            String message = "file loader ready";
            if (!status) {
                if (fileSystemManager.fileNameExists(pathName, md5)) {
                    message = "file already existed";
                }
                else {
                    message = "file is loading";
                }
            }
            response.append("message", message);
            response.append("status", status);
        }
	    catch (Exception e) {
	        e.printStackTrace();
	        // TODO: Handle exceptions with different message
            response.append("message", "internal server error");
            response.append("status", false);
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
