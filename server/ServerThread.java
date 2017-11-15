import java.nio.file.Files;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

/* 
A thread that handles serving a webpage to a single client
*/
public class ServerThread implements Runnable{
  // The socket connection
  Socket socket;
  // Streams for communicating with client
  InputStream in;
  OutputStream out;
  /* 
  public single argument constructor 
  */
  public ServerThread(Socket s){
    try{
      //Instantiate
      socket = s;
      in = socket.getInputStream();
      out = socket.getOutputStream();
    }catch(Exception e){
      try{
        socket.close();
        return;
      }catch(Exception e2){
        return;
      }
    }
  }

 /*
  Name: run
  Purpose: the main action this thread will do. It will read the client
    request and serve data back.
  Description: serves data to the client
  Parameters: none
  Return: none
  */
  public void run(){
    /* Do nothing if the socket is already closed */
    if(socket.isClosed()){
      return;
    }
    try{
      String request = "";
      // Wait for the client send a byte of data 
      int singleByte = in.read();
      request += (char) singleByte;
      // Read the rest of the data 
      while(in.available() > 0){
        request += (char) in.read();
      }
      // Create a response object
      Response r = new Response(request);
      // Respond to the client
      out.write(r.getResponseText().getBytes());
      out.flush();
      socket.close();
    }catch(Exception e){
      try{
        socket.close();
        return;
      }catch(Exception e2){

      }
    }
  }
}
