import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.SocketFactory;

public class Driver{
  public static void main(String[] args) throws Exception{
    System.out.print("Enter a website to visit: ");
    String userInput = System.console().readLine();
    CustomHTTPConnection con = new CustomHTTPConnection(userInput);

    con.makeRequest("GET");

    System.out.println(con.getResponse());
  }

}


