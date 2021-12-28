package FileTransferProject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Server {
    

    public static void main(String[] args) {
    	
    	
    	//List<Thread> allThreads = new ArrayList<Thread>();//thread list
    	
    	
    	
    	int concurrency[]=receiveConcurrencyNumber();
    	int count=0;

    	Thread[] allThreads=new Thread[concurrency[0]];
    	
    	for(int j=0;j<concurrency[1];j=j+concurrency[0])   //concurrency[1] number of files in the folder
    	{
    		for(int i=0;i<concurrency[0]&&count<concurrency[1];i++) //concurrency[0] concurrency value
        	{
        		receiveFile rf=new receiveFile(5000+count,count);
        		rf.start();
        		count++;
        		
        		allThreads[i]=rf;
        		//allThreads.add(rf); //adding thread to the list
        	}
    		
    		//joining all the threads
        	for (int i=0;i<concurrency[0]&&count<concurrency[1];i++)
    			try {
    				allThreads[i].join();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	
    		
    		
    		/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    	}
    	
    	
    	
    	sendingServerFinishTime();
    	      
    }
    
    public static void sendingServerFinishTime()
    {
    	long finishTime = System.nanoTime();
        System.out.println("Server finish time: "+finishTime);
    	
    	try
    	{
    	Socket s=new Socket("localhost",1001);
    	PrintStream ps=new PrintStream(s.getOutputStream());
    	ps.println(finishTime);
    	ps.flush();
    	s.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    public static int[] receiveConcurrencyNumber()
    {
    	int[] n=new int[2];
    	try
    	{
    	System.out.println("Receiving concurrency value.....");
    	ServerSocket sc=new ServerSocket(1000);
    	Socket s=sc.accept();
    	BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
    	n[0]=Integer.parseInt(br.readLine());
    	n[1]=Integer.parseInt(br.readLine());
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return n;
    	
    }
}

class receiveFile extends Thread{
	private DataOutputStream dataOutputStream = null;
   private  DataInputStream dataInputStream = null;
    private int PortNumber;
   private  int threadNumber;
	receiveFile(int PortNumber, int threadNo)
	{
		this.PortNumber=PortNumber;
		this.threadNumber=threadNo;
	}
	public void run()
	{
		 try(ServerSocket serverSocket = new ServerSocket(PortNumber)){
	            System.out.println("listening to port:"+PortNumber);
	            Socket clientSocket = serverSocket.accept();
	            System.out.println(clientSocket+" connected.");
	            dataInputStream = new DataInputStream(clientSocket.getInputStream());
	            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

	           // for(int i=0;i<=1;i++)
	            receiveFile();
	           //receiveFile();

	            dataInputStream.close();
	           dataOutputStream.close();
	            clientSocket.close();
	        } catch (Exception e){
	            e.printStackTrace();
	        }
	}
    private void receiveFile() throws Exception{
    int bytes = 0;
   
    
    long size = dataInputStream.readLong();     // read file size
    System.out.println(size);
    String FileName=dataInputStream.readLine();
    System.out.println("Thread"+ threadNumber +FileName);
    FileOutputStream fileOutputStream = new FileOutputStream("E:\\"+FileName);
    byte[] buffer = new byte[4*1024];
    while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
        fileOutputStream.write(buffer,0,bytes);
        size -= bytes;      // read upto file size
    }
    fileOutputStream.close();
    
      
    
    //integrity check
    String checksumServer=dataInputStream.readLine();
    //System.out.println(checksum);
    
    MessageDigest md5Digest = MessageDigest.getInstance("MD5");
    
    //Get the checksum
    File file = new File("E:\\"+FileName);
    String checksumClient = getFileChecksum(md5Digest, file);
     
    //see checksum
   // System.out.println("Client checksum:"+checksumClient+"  Server checksum: "+checksumServer);
    
    if(checksumServer.equals(checksumClient))
    	System.out.println("There is no error in the transferred file: "+FileName);
    else
    	System.out.println("There is error in the transferred file: "+FileName);
    
    
    
    
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
