import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
public class Client{
  public static void main(String[] args){
    try{
      Socket client = new Socket(InetAddress.getByName(null), 80);
      OutputStream out = client.getOutputStream(); 
      String data = "GET /index.html HTTP1.1\r\n\r\n";
      out.write(data.getBytes());
      out.flush();
      InputStream in = client.getInputStream();
      String response = "";
      int read = in.read();
      response += (char) read;
      int bytesLeft = in.available(); 
      while(bytesLeft > 0){
        response += ((char)in.read());
        bytesLeft = in.available(); 
      }
      System.out.println(response);
      client.close();
    }catch(Exception e){
      System.err.println("failed");
    }
  }
}
