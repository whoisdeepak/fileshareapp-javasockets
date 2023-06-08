package code;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerFile {
	
	ServerSocket serverSocket = null;
	
	public ServerFile() {
		super();
	}
    
    public void handle(String[] args) throws IOException {
    	
		try{
			int port = Integer.parseInt(args[2]);
			serverSocket = new ServerSocket(port);
		    System.out.println("Server started and is listening to port " + port);
		    
		    while (true) {
		        Socket socket = serverSocket.accept();
		        System.out.println("A new client has connected to server");
		        
		        // Initialize the class that handles multithreading for incoming client requests
		        ServerFileThread servLogic = new ServerFileThread(socket, this);
		        servLogic.start();
		    }
		} catch(SocketException e) {
			System.out.println("ERR01 Socket error: " +e.getMessage());
		} catch (IOException e) {
		    System.out.println("ERR02 IO error: " + e.getMessage());
		}
	
    }
    
    
    public synchronized void shutdown() {
    	
    	try {
    		
	        //stops accepting new client requests
	        serverSocket.close();
	        
	        //completely shuts down server, ending all active threads and the current thread
	        System.exit(0);
        
    	}catch(SocketException e) {
			System.out.println("ERR01 Socket error: " +e.getMessage());
		} catch (IOException e) {
		    System.out.println("ERR02 IO error: " + e.getMessage());
		}
    }

 }












