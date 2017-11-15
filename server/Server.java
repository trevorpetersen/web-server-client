import java.nio.file.Files;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

/* This program serves webpages to clients on port 80. Each new request
   is handled on a a new thread */
public class Server{
  public static void main(String[] args){
    try{
      ServerSocket server = new ServerSocket(80, 0, InetAddress.getByName(null));
      while(true){
        // Wait for clients to connect
        Socket sock = server.accept();
        // Pass them off to a new thread
        ServerThread st = new ServerThread(sock);
        st.run();
      }       
    }catch(Exception e){
      System.err.println("failed");
      e.printStackTrace();
    }
  }

}
