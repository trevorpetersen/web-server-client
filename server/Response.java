import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.IOException;

/*
  This class is used to produce the response the server should send. The ctor
  takes the HTTP request that was made by a client. In the ctor, all the work
  is done. Call getResponseText() for the response that the server should send.
*/
public class Response{
  // The first line of the resp Ex: HTTP/1.1 200 OK
  String firstLine;
  // The headers of the response
  List<Pair> responseHeaders;
  // The data (the contents of the file to be sent to the client)
  String data;

  public Response(String request){
    //Instantiate 
    responseHeaders = new ArrayList<Pair>();
    firstLine = "";
    data = "";

    // Add some default headers
    String key = "Content-type";
    String value = "text/html; charset=utf-8";
    responseHeaders.add(new Pair<String, String>(key, value));
    key = "Connection";
    value= "Close";
    responseHeaders.add(new Pair<String, String>(key, value));

    // Set the response to be OK (errors later will override)
    setFirstLine(200);

    //Find the request type
    int index = request.indexOf(" ");
    if(index == -1 || request.length() < index + 1){
      setFirstLine(400);
      return;
    }else{
      request = request.substring(index + 1);
    }

    //Find the filepath
    index = request.indexOf(" ");
    if(index == -1 || request.length() < index + 1){
      setFirstLine(400);
      return;
    }else{
      String filePath = request.substring(0, index);
      // Standard case
      if(filePath.equals("/")){
        filePath = "www/index.html";
      }else{
        filePath = "www" + filePath;
      }
      request = request.substring(index + 1);

      //Write the file requested by the client to a String
      try{
        if(filePath.endsWith(".php")){
          Process phpProcess = Runtime.getRuntime().exec("php " + filePath);
          phpProcess.waitFor();
          if(phpProcess.exitValue() == 1){
            throw new IOException();
          }
          InputStream phpFileInputStream = phpProcess.getInputStream();
          data = "";
          int read = 0;
          while( (read = phpFileInputStream.read()) != -1){
            data += (char) read;
          }
        }else{
          byte[] fileContents = Files.readAllBytes(Paths.get(filePath));
          data = new String(fileContents, StandardCharsets.UTF_8);
        }
      }catch(Exception e){
        data = "";
        setFirstLine(404);
        return;
      }
     
    }
  }

  /*
  Name: setFirstLine
  Purpose: set the first line of the response
  Description: changes the value of firstLine
  Parameters: code - the HTTP status code
  Return: none
  */
  private void setFirstLine(int code){
    // Set it according to the code
    firstLine = "HTTP/1.1 " + code + " "; 
    String mess = "";
    // Set the message
    switch(code){
      case 200:
         mess = "OK";
         firstLine += mess;
         break;
      case 400:
        mess = "Bad Request";
        firstLine += mess;
        data = "<html> <body> <h1>" + code + " " +mess + "</h1></body></html>";
        break;
      case 404:
        mess = "Not Found";
        firstLine += mess;
        data = "<html> <body> <h1>" + code + " " +mess + "</h1></body></html>";
        break;
    }
  } 

  /*
  Name: addHeader
  Purpose: add a header to the response
  Description: adds a Pair to responseHeader
  Parameters: key - the name of the header
              value - the value of the header
  Return: none
  */
  public void addHeader(String key, String value){
    responseHeaders.add(new Pair<String,String>(key, value));
  } 

  /*
  Name: getResponseText
  Purpose: get the string that the server should send
  Description: returns a string 
  Parameters: none
  Return: the response message as a string
  */
  public String getResponseText(){
    String res = firstLine + "\r\n";
    for(Pair p : responseHeaders){
      res += p.getFirst() + ": " + p.getSecond() + "\r\n";
    }
    res += "\r\n";
    res += data; 
    res += "\r\n";
    if(! data.equals("")){
      res += "\r\n";
    }
    return res;
  }
}
