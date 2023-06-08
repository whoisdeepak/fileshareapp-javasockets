package code;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class ClientFile {
    
	public ClientFile() {
		super();
	}

	public void handle(String[] args) throws IOException {
		
		try {
		
	        String operation = args[1];
	      
	        String source = null;
	        String destPath = null;
	        String serverPath = null;
	
	        ClientFile client = new ClientFile();
	
	        if (operation.equalsIgnoreCase("upload")) {
	        	source = args[2];
	        	destPath = args[3];
	            client.uploadFile(source, destPath);
	        } else if (operation.equalsIgnoreCase("download")) {
	        	source = args[2];
	        	destPath = args[3];
	            client.downloadFile(source, destPath);
	        }else if(operation.equalsIgnoreCase("dir")) {
	        	serverPath = args[2];
	        	client.listFiles(serverPath);
	        }else if(operation.equalsIgnoreCase("rm")) {
	        	serverPath = args[2];
	        	client.removeFile(serverPath);
	        }else if(operation.equalsIgnoreCase("mkdir")) {
	        	serverPath = args[2];
	        	client.createDir(serverPath);
	        }else if(operation.equalsIgnoreCase("rmdir")) {
	        	serverPath = args[2];
	        	client.removeDir(serverPath);
	        }else if(operation.equalsIgnoreCase("shutdown")) {
	        	client.shutdown();
	        }else {
	            System.err.println("ERR300: Invalid command. Valid command syntax is as follows: ");
	            System.err.println("java -jar pa1.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
	            System.exit(1);
	        }
        
		}catch(ArrayIndexOutOfBoundsException e) {
			System.err.println("ERR300: Invalid command. Valid command syntax is as follows: ");
            System.err.println("java -jar pa1.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
		}
        
    }
	
	
	
	public void uploadFile(String source, String dest) {
		
		try {
			
			
			 String envVar = System.getenv("PA1_SERVER");
			 System.out.println("PA1_SERVER value is " + envVar);
			 
			 //check if environment variable is null or empty 
			 if(envVar==null) {
				 System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client"); 
				 System.err.println("Environment variable set in a given cmd won't be accessible in another cmd"); 
				 System.exit(1); 
			 }
			 
			 String[] hostPort = envVar.split(":"); 
			 String host = hostPort[0]; 
			 int port = Integer.parseInt(hostPort[1]);
			 
			 System.out.println("host is " + host); 
			 System.out.println("port is " + port);
			 
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
			
			//check if file intended to be uploaded exists on client or not
			File cliFile = new File(source);
	        if (!cliFile.exists()) {
                System.err.println("ERR002: requested file does not exist on client side");
                System.exit(1);
            }
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("upload");
			out.writeUTF(dest);
	        
			long byteCount = 0;
	    	FileInputStream fis = new FileInputStream(cliFile);
	    	
	    	File serFile = new File(dest);
	    	if (serFile.exists() && serFile.length()>0 && serFile.length() < cliFile.length()) { 
				fis.skip(serFile.length());
				System.out.println("***********************");
				System.out.println("SKIPPING RE-UPLOAD OF " + serFile.length() + " BYTES");
				System.out.println("RESUMING UPLOAD FROM THE NEXT PORTION");
				System.out.println("***********************");
				byteCount = serFile.length();
			}
	
	        byte[] byteArray = new byte[10];
	        int read;
	
	        
	        while ((read = fis.read(byteArray)) != -1) 
	        {
	        	out.write(byteArray, 0, read);
	            byteCount += read;
	            System.out.println(byteCount+" bytes of the file uploaded");
	        }
	        
	        if(byteCount>0) {
	        	System.out.println("");
	        	System.out.println("UPLOAD COMPLETED SUCCESSFULLY");
	        }
	        
	        fis.close();
	        out.close();
	        socket.close();
		    
		}catch (ConnectException e) {
            System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
		
    }
	
	

    public void downloadFile(String source, String dest) {
    	
    	try {
    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
			
			//File corresponding to server path
	        File servFile = new File(source);
	        if (!servFile.exists()) {
                System.err.println("ERR002: requested file does not exist on server side");
                System.exit(1);
            }
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("download");
			out.writeUTF(source);
	        
	        //Initializing the input stream
	        InputStream inStream = socket.getInputStream();
	        
	        
	        
	        long byteCount = 0;
	        //File corresponding to client path
	        File cliFile = new File(dest);
	    	if (cliFile.exists() && cliFile.length()>0 && cliFile.length() < servFile.length()) { 
	    		inStream.skip(cliFile.length());
				System.out.println("***********************");
				System.out.println("SKIPPING RE-DOWNLOAD OF " + cliFile.length() + " BYTES");
				System.out.println("RESUMING DOWNLOAD FROM THE NEXT PORTION");
				System.out.println("***********************");
				byteCount = cliFile.length();
			}
	    	
	    	//Initializing the output stream
	        OutputStream fileOutStream = new FileOutputStream(cliFile, true);
	        
	        //read data from server and write to the output stream
	        
	        byte[] servResponse = new byte[300];
	        int read=0;
	        while ((read = inStream.read(servResponse)) > 0) {
	        	fileOutStream.write(servResponse, 0, read);
	        	byteCount += read;
	            System.out.println(byteCount+" bytes of the file downloaded");
	        }
	        
	        if(byteCount>0) {
	        	System.out.println("");
	        	System.out.println("DOWNLOAD COMPLETED SUCCESSFULLY");
	        }
	        
	        fileOutStream.close();
	        out.close();
	        inStream.close();
	        socket.close();

		}catch (ConnectException e) {
			System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    
    public void listFiles(String serverPath) {
    	try {
	        
    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("list");
			out.writeUTF(serverPath);
	        
	        //Initializing the input stream
	        InputStream inStream = socket.getInputStream();
	        
	        //Read the file names from the server
	        byte[] servResponse = new byte[1024];
	        int bytes = inStream.read(servResponse);
	        String allFilesLoaded = new String(servResponse, 0, bytes);
	        
	        //Show the list of files in client console
	        String[] files = allFilesLoaded.split(",");
	        for (String file : files) {
	            System.out.println(file);
	        }
	        
	        out.close();
	        inStream.close();
	        socket.close();
	        
    	}catch (ConnectException e) {
    		System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void removeFile(String serverPath) {
    	
    	try {

    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("removeFile");
			out.writeUTF(serverPath);
	        
	        //Initializing the input stream
            InputStream inStream = socket.getInputStream();
            
            //Read the response from the server and print it to client console
            byte[] servResponse = new byte[1024];
            int bytes = inStream.read(servResponse);
            String servResonseMsg = new String(servResponse, 0, bytes);
            System.out.println(servResonseMsg);
            
            out.close();
            inStream.close();
            socket.close();
    		
    	}catch (ConnectException e) {
    		System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
    
    
    public void createDir(String serverPath) {
    	
    	try {
    		
    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("createDir");
			out.writeUTF(serverPath);
	        
    		//Initializing the input stream
            InputStream inStream = socket.getInputStream();            
            
            //Read the response from the server and print it to client console
            byte[] servResponse = new byte[1024];
            int read = inStream.read(servResponse);
            String servResponseMsg = new String(servResponse, 0, read);
            System.out.println(servResponseMsg);
            
            out.close();
            inStream.close();
            socket.close();
    		
    	}catch (ConnectException e) {
    		System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
    
    
    public void removeDir(String serverPath) {
    	
    	try {
    		
    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket(host, port);
    		
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("removeDir");
			out.writeUTF(serverPath);
			
	        //Initializing the input stream
	        InputStream inStream = socket.getInputStream();
	        
	        //Read the response from the server and print it to client console
	        byte[] servResponse = new byte[1024];
	        int read = inStream.read(servResponse);
	        String servResponseMsg = new String(servResponse, 0, read);
	        System.out.println(servResponseMsg);
	        
	        out.close();
	        inStream.close();
	        socket.close();
    		
    	}catch (ConnectException e) {
    		System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    	
    	
    }
    
    
    public void shutdown() {
    	
    	try {

    		String envVar = System.getenv("PA1_SERVER");
			System.out.println("PA1_SERVER value is " + envVar);
			
			//check if environment variable is null or empty
			if(envVar==null) {
				System.err.println("ERR200: Please define host:port values as an env variable 'PA1_SERVER' before running client");
				System.err.println("Environment variable set in a given cmd won't be accessible in another cmd");
				System.exit(1);
			}
			
			String[] hostPort = envVar.split(":");
			String host = hostPort[0];
			int port = Integer.parseInt(hostPort[1]);
			
			System.out.println("host is " + host);
			System.out.println("port is " + port);
			
			//Initializing client socket
			Socket socket = new Socket("localhost", 3333);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("shutdown");
			out.writeUTF("");
	        
	        //Initializing the input stream
            InputStream inStream = socket.getInputStream();
            
            //Read the response from the server and print it to client console
            byte[] servResponse = new byte[1024];
            int bytes = inStream.read(servResponse);
            String servResonseMsg = new String(servResponse, 0, bytes);
            System.out.println(servResonseMsg);
            
            out.close();
            inStream.close();
            socket.close();
    		
    	}catch (ConnectException e) {
    		System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (SocketException e) {
        	System.out.println("ERR100: Could not connect to server. Please make sure server is up and client socket port is correct");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
    
		
}

