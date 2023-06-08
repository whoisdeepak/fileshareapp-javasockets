package code;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class ServerFileThread extends Thread {
	
	private final Socket clientSocket;
	private final ServerFile serverFile;
    
    public ServerFileThread(Socket socket, ServerFile serverFile) throws IOException {
        this.clientSocket = socket;
        this.serverFile = serverFile;
    }
	
    public void run() {
    	
    	DataInputStream dis = null;
    	FileOutputStream fos = null;
    	OutputStream outStream = null;
    	FileInputStream fileInStream = null;
    	
        try {
            
        	dis = new DataInputStream(clientSocket.getInputStream());
        	
            String operation = dis.readUTF();
            String filePath = dis.readUTF();
               
            if ("upload".equalsIgnoreCase(operation)) {
            	
            	String destFile = filePath;
            	//String destFile = requestFunc[1];
                
                //check if file already exists and replace it if true
                File file = new File(destFile);
				
				fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[4096];
                int bytesRead;
                //long totalBytesRead = 0;
                while ((bytesRead = dis.read(buffer, 0, buffer.length)) != -1)
                {
                    fos.write(buffer, 0, bytesRead);
                    //totalBytesRead += bytesRead;
                }
                
                
            } else if ("download".equalsIgnoreCase(operation)) {
                
            	String source = filePath;
                
                //check if requested file exists on server path
                File servFile = new File(source);
                
                //Initializing the output stream
                outStream = clientSocket.getOutputStream();
                
                //Initializing the input stream
                fileInStream = new FileInputStream(servFile);
                
                //read data from the file on server side and write to output stream
                byte[] byteArr = new byte[1024];
                int read=0;
                while ((read = fileInStream.read(byteArr)) > 0) {
                	outStream.write(byteArr, 0, read);
                }
                
                outStream.close();
                fileInStream.close();
	
            } else if("list".equalsIgnoreCase(operation)) {
                
                String serverPath = filePath;
               
                File sourceFile = new File(serverPath);
                
                //Initializing the output stream
                outStream = clientSocket.getOutputStream();
                
                //check if requested path is valid
                if (!sourceFile.exists()) {
                	
                	outStream.write("ERR003: the path requested does not exist on server side!".getBytes());
                	
                }else {
                
	                if (sourceFile.isDirectory()) {
	                    
	                	//read and parse the files present in given path
	                    File[] files = sourceFile.listFiles();
	                    String[] filesAsStrArr = new String[files.length];
	                    for (int i = 0; i < files.length; i++) {
	                    	filesAsStrArr[i] = files[i].getName();
	                    }
	                    Arrays.sort(filesAsStrArr);
	                    
	                    //write the individual file names to output stream
	                    byte[] byteArr = String.join(",", filesAsStrArr).getBytes();
	                    outStream.write(byteArr);
	                    
	                } else {
	                    
	                	//if input path is a file path, write the filename to output stream 
	                    byte[] byteArr = sourceFile.getName().getBytes();
	                    outStream.write(byteArr);
	                }
                
                }
                
                outStream.close();
            	
            }else if("removeFile".equalsIgnoreCase(operation)) {
                
                String fileName = filePath;
                
                File file = new File(fileName);
                
                //Initializing the output stream
                outStream = clientSocket.getOutputStream();
                	
            	//check if file exists on server path
                if (file.exists()) {
                	
                	if(file.isDirectory()) {
                		outStream.write("ERR004: Please provide a file path, not a directory path".getBytes());
                	}else {
                		file.delete();
                		outStream.write("File removed successfully!".getBytes());
                	}
                } else {
                	outStream.write("ERR003: the path requested does not exist on server side!".getBytes());
                }
                
                outStream.close();
            	
            }else if("createDir".equalsIgnoreCase(operation)) {
                
                String dirPath = filePath;
                
                File dir = new File(dirPath);
                
                //Initializing the output stream
                outStream = clientSocket.getOutputStream();
                
            	//Create the directory on the server and write the response to output stream
                if (dir.exists()) {
                	outStream.write("ERR005: The input path/directory already exists on server".getBytes());
                } else {
                    boolean created = dir.mkdirs();
                    if (created) {
                    	outStream.write("Directory created successfully on server side!".getBytes());
                    } else {
                    	outStream.write("ERR006: Directory creation error, please try again".getBytes());
                    }
                }
                
                outStream.close();
            	
            }else if("removeDir".equalsIgnoreCase(operation)) {
                
                String dirPath = filePath;
                
                File dir = new File(dirPath);
                
                //Initializing the output stream
                outStream = clientSocket.getOutputStream();
                
                //handle all cases for removal and write response to output stream
                if (!dir.exists()) {
                	outStream.write("ERR003: the path requested does not exist on server side!".getBytes());
                } else if (!dir.isDirectory()) {
                	outStream.write("ERR007: the path requested is not a directory".getBytes());
                } else if (dir.listFiles().length > 0) {
                	outStream.write("ERR008: the requested directory is non empty, so it cannot be removed".getBytes());
                } else {
                    boolean removed = dir.delete();
                    if (removed) {
                    	outStream.write("Successfully removed input directory".getBytes());
                    } else {
                    	outStream.write("ERR009: Directory removal error, please try again".getBytes());
                    }
                }
                
                outStream.close();
            	
            }else if("shutdown".equalsIgnoreCase(operation)) {
                
            	outStream = clientSocket.getOutputStream();
            	
            	outStream.write("Server will shutdown in 5 seconds".getBytes());
            	
            	//wait for 5 seconds to allow current threads to finish their execution																	
            	this.sleep(5000);
            	
            	//calling shutdown method of server class
            	serverFile.shutdown();
            	
            	outStream.close();
            	
            }

            //dis.close();
            //clientSocket.close();
            
        }catch (ConnectException e) {
            System.out.println("ERR100: Lost connection to a client.");
        }catch (SocketException e) {
        	System.out.println("ERR100: Lost connection to a client.");
        }catch (FileNotFoundException e) {
        	System.out.println("ERR888: Either the source or the destination paths provided cannot be found");
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            try {
                if (dis != null) {
                    dis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (outStream!= null) {
                	outStream.close();
                }
                if (fileInStream != null) {
                	fileInStream.close();
                }
                if (clientSocket != null) {
                	clientSocket.close();
                }
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        }
    }
	
}
