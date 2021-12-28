package FileTransferProject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
   

    public static void main(String[] args)
    {
        
    	int concurrency=1;
    	
    	File directoryPath = new File(args[0]);
        //List of all files and directories
        String contents[] = directoryPath.list();
        
        
        if(args[1]!=null)
            concurrency=Integer.parseInt(args[1]);
            else
            	concurrency=1;
        	
       	sendConcurrencyNumber(concurrency,contents.length); //sending concurrency number and number of files in the folder to server
        
       	int counter=0;
       	
       	
       	
       	long startTime = System.nanoTime();
        System.out.println("Client Start time: "+startTime);
        
        
       // List<Thread> allThreads = new ArrayList<Thread>();//thread list
        
        Thread[] allThreads=new Thread[concurrency];
        
        
        for(int j=0; j<contents.length; j=j+concurrency) {
          // System.out.println(contents[i]);
           
		        for(int i=0;i<concurrency&&counter<contents.length;i++)
		       	{
		       		sendFile sf=new sendFile(args[0]+"\\"+contents[counter],5000+counter);
		       		sf.start();
		       		counter++;
		       		
		       		
		       		allThreads[i]=sf; //adding thread to the list
		       		
		       	}    
		        
		        
		      //joining all the threads
	        	for (int i=0;i<concurrency&&counter<contents.length;i++)
	    			try {
	    				allThreads[i].join();
	    			} catch (InterruptedException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
		        
		      /*  try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
           
        }
        
        
    /*  //joining all the threads
    	for (int i = 0; i < allThreads.size(); i++)
			try {
				((Thread) allThreads.get(i)).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    	
    	long finishTime=receivingServerFinishTime();
    	
    	 System.out.println("Received finish time: "+finishTime);
    	 
    	 long differenceSec=(finishTime-startTime)/1000000000;
    	 
    	 
    	 System.out.println("Difference in sec: "+differenceSec);
    	
       
}
    public static long receivingServerFinishTime()
    {
    	long finishTime=0;
    	try
    	{
    		
    	System.out.println("Receiving finish time.....");
    	ServerSocket sc=new ServerSocket(1001);
    	Socket s=sc.accept();
    	BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
    	finishTime=Long.parseLong(br.readLine());
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return finishTime;
    }
    
    public static void sendConcurrencyNumber(int n, int numberOfFile)
    {
    	try
    	{
    	Socket s=new Socket("localhost",1000);
    	PrintStream ps=new PrintStream(s.getOutputStream());
    	ps.println(n);
    	ps.flush();
    	ps.println(numberOfFile);
    	ps.flush();
    	s.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
class sendFile extends Thread
{
private String FileName;
private int PortNumber;
private DataOutputStream dataOutputStream = null;
private DataInputStream dataInputStream = null;

	sendFile(String FileName, int PortNumber)
	{
	 this.FileName = FileName;
	 this.PortNumber=PortNumber;
	}
	
	public void run() {
		try
		{
			 try(Socket socket = new Socket("localhost",PortNumber)) {
		            dataInputStream = new DataInputStream(socket.getInputStream());
		            dataOutputStream = new DataOutputStream(socket.getOutputStream());
		            
		            System.out.println(FileName);
		            
		            sendFilethread(FileName);
		         //   sleep(1000);
		           // sendFile("source1.txt");
		            		            
		            dataInputStream.close();
		            dataInputStream.close();
		        }catch (Exception e){
		            e.printStackTrace();
		        }
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	 void sendFilethread(String path) throws Exception{
	        int bytes = 0;
	        File file = new File(path);
	        FileInputStream fileInputStream = new FileInputStream(file);
	        
	        // send file size
	        dataOutputStream.writeLong(file.length());  
	        dataOutputStream.writeBytes(path+"\n");
	        // break file into chunks
	        byte[] buffer = new byte[4*1024];
	        while ((bytes=fileInputStream.read(buffer))!=-1){
	            dataOutputStream.write(buffer,0,bytes);
	            dataOutputStream.flush();
	        }
	        fileInputStream.close();
	        
	        
	        
	        
	      //sending checksum
	      MessageDigest md5Digest = MessageDigest.getInstance("MD5");
	        
	      //Get the checksum
	      String checksum = getFileChecksum(md5Digest, file);
	       
	      //see checksum
	     // System.out.println(checksum);
	      
	      dataOutputStream.writeBytes(checksum);
	      
	      
	    }
	 
	 String getFileChecksum(MessageDigest digest, File file) throws IOException
	 {
	     //Get file input stream for reading the file content
	     FileInputStream fis = new FileInputStream(file);
	      
	     //Create byte array to read data in chunks
	     byte[] byteArray = new byte[1024];
	     int bytesCount = 0; 
	       
	     //Read file data and update in message digest
	     while ((bytesCount = fis.read(byteArray)) != -1) {
	         digest.update(byteArray, 0, bytesCount);
	     };
	      
	     //close the stream; We don't need it now.
	     fis.close();
	      
	     //Get the hash's bytes
	     byte[] bytes = digest.digest();
	      
	     //This bytes[] has bytes in decimal format;
	     //Convert it to hexadecimal format
	     StringBuilder sb = new StringBuilder();
	     for(int i=0; i< bytes.length ;i++)
	     {
	         sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	     }
	      
	     //return complete hash
	    return sb.toString();
	 }
}