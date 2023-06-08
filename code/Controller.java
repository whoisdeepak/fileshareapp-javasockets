package code;

import java.io.IOException;

public class Controller {

public static void main(String[] args) throws IOException {
        
		if("server".equalsIgnoreCase(args[0])) {
	
			ServerFile servFile = new ServerFile();
			servFile.handle(args);
        
		}else if("client".equalsIgnoreCase(args[0])) {
			
			ClientFile cli = new ClientFile();
			cli.handle(args);
			
		}else {
			System.err.println("ERR300: Invalid command. Valid command syntax is as follows: ");
            System.err.println("java -jar pa1.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
		}
    
	}
}
