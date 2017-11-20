import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.SocketFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;

public class CustomHTTPConnection{
  public final static String GET = "GET";
  public final static String POST = "POST";

  private final static String END_LINE = "\r\n";
  private final static String BLANK_LINE = END_LINE + END_LINE;

  class HTTPRequest{

    String requestType;
    String endpoint;
    String hostName;
    String protocolVersion;
    List<Pair<String, String>> headers;
    List<Pair<String, String>> data;

    public HTTPRequest(){
      this.requestType = GET;
      this.hostName = "";
      this.endpoint = "/";
      this.protocolVersion = "HTTP/1.1";
      this.headers = new ArrayList<Pair<String, String>>();
      this.data= new ArrayList<Pair<String, String>>();
    }

    public void setHostName(String hostName){
      this.hostName = hostName;
    }

    public void setRequestType(String requestType){
      switch(requestType){
        case GET:
        case POST:
          this.requestType = requestType;
          break;
      }
    }

    public void setEndpoint(String endpoint){
      if(endpoint.equals("")){
        this.endpoint = "/";
      }else{
        this.endpoint = endpoint;
      }
    }

    public void setHeaders(List<Pair<String, String>> headers){
      this.headers = headers;
    }

    public void addHeader(Pair<String, String> headerPair){
      headers.add(headerPair);
    }

    public void addPostParam(String key, String value){
      data.add(new Pair<String, String>(key, value));
    }

    public Pair<String, String> findHeader(String key){
      for(Pair<String, String> pair : headers){
        if(pair.getFirst().equals(key)){
          return pair;
        }
      }

      return null;
     
    }

    public void overwriteHeader(Pair<String,String> headerPair){
      Pair<String, String> pair = findHeader(headerPair.getFirst());
      if(pair != null){
        pair.setSecond(headerPair.getSecond());
      }else{
        addHeader(headerPair);
      }
    }

    public void removeHeader(String key){
      for(int i = 0; i < headers.size(); i++){
        Pair pair = headers.get(i);
        if(pair.getFirst().equals(key)){
          headers.remove(i);
          break;
        }
      }
    }

    private String getEncodedData(){
      StringBuilder sb = new StringBuilder();
      boolean isFirst = true;

      for(Pair<String, String> pair : data){
        if(isFirst){
          isFirst = false;
        }else{
          sb.append("&");
        }

        try{
          String encodedKey = URLEncoder.encode(pair.getFirst(), "UTF-8");
          String encodedValue = URLEncoder.encode(pair.getSecond(), "UTF-8");

          sb.append(encodedKey + "=" + encodedValue);
        }catch(Exception e){
          System.err.println("Failed to encode post parameters");
        }
      }

      return sb.toString();
    }

    public String getInitialRequestLine(){
      return requestType + " " + endpoint + " " + protocolVersion + END_LINE;
    }

    private void updateDefaultHeaders(){
      overwriteHeader(new Pair<String, String>("Host", hostName));
      overwriteHeader(new Pair<String, String>("Connection", "close"));
      if(requestType.equals(POST)){
        overwriteHeader(new Pair<String, String>("Content-Type", "application/x-www-form-urlencoded"));
        overwriteHeader(new Pair<String, String>("Content-Length", getEncodedData().getBytes().length + ""));
      }else{
        removeHeader("Content-Type");
        removeHeader("Content-Length");
      }
    }

    private String getFormattedHeaders(){
      StringBuilder sb = new StringBuilder();

      for( Pair<String,String> pair : headers){
        sb.append(pair.getFirst() + ": " + pair.getSecond() + END_LINE);
      }
      
      return sb.toString();
    }

    public String toString(){

      updateDefaultHeaders();
      String req = getInitialRequestLine() +
                   getFormattedHeaders() +
                   END_LINE;

      if(requestType.equals(POST)){
        return req +
               getEncodedData();
      }else{
        return req;
      }

    }
  }



  class HTTPResponse{
    int statusCode;
    String statusMessage;
    List<Pair<String, String>> headers;
    String body;


    HTTPResponse(String responseText){
      headers = new ArrayList<Pair<String, String>>();
      String[] splitRes = splitAtFirstInstance(responseText, END_LINE);

      String firstLine = splitRes[0];
      String headersAndBody = splitRes[1];
 
      splitRes = splitAtFirstInstance(headersAndBody, BLANK_LINE);

      String headersText = splitRes[0];
      String bodyText = splitRes[1];

      parseFirstLine(firstLine);
      parseHeaders(headersText);
      body = bodyText;
 
    }

    private void parseFirstLine(String firstLine){
      String[] parts = firstLine.split(" ");
      statusCode = Integer.parseInt(parts[1]);
      statusMessage = parts[2];
    }

    private void parseHeaders(String headersText){
      String[] parts = headersText.split(END_LINE);

      for(String headerText : parts){
        String[] headerParts = headerText.split(": ");
        headers.add(new Pair<String, String>(headerParts[0], headerParts[1]));
      }
    
    }

    private String[] splitAtFirstInstance(String text, String delin){
      int index = text.indexOf(delin);
      if(index < 0){
        return null;
      }
     String first = text.substring(0, index + delin.length());
     String second = text.substring(index + delin.length());

     return new String[] {first, second};

    }

    public int getStatusCode(){
      return statusCode;
    }
 
    public List<Pair<String,String>> getHeaders(){
      return headers;
    }

    public String toString(){
      StringBuilder sb = new StringBuilder();
      sb.append("Status Code: " + statusCode);
      sb.append("\n");
      sb.append("Status Message: " + statusMessage);

      sb.append("\n");
      sb.append("Headers: ");
      sb.append("\n");
      for( Pair<String,String> p : headers){
        sb.append("\t" + p.getFirst() + ":" + p.getSecond());
        sb.append("\n");
      }

      sb.append("Body: ");
      sb.append("\n");
      sb.append(body);
      sb.append("\n");

      return sb.toString();
    }

  }



  private Socket socket;
  private URL url;
  private HTTPRequest request;
  private HTTPResponse response;

  public CustomHTTPConnection(String url) throws URISyntaxException, IOException{
    String hostName = getDomainName(url);
    this.socket = getSocket(hostName);
    this.request = new HTTPRequest();
    setHostName(hostName);
    request.setEndpoint(url.substring(url.indexOf(hostName) + hostName.length()));
  }

  private Socket getSocket(String hostName) throws IOException{
    if(url.getProtocol().equals("http")){
      return new Socket(hostName, 80);
    }else{
      SocketFactory fac = SSLSocketFactory.getDefault();
      Socket socketSSL = fac.createSocket(hostName, 443);
      socketSSL.setSoTimeout(3000);
      return socketSSL;
    }
  }

  public String getDomainName(String urlString) throws MalformedURLException{
      url = null;
    try{
      url = new URL(urlString);
    }catch(Exception MalformedURLException){
      url = new URL("https://" + urlString);
    }
    return url.getHost();
  }

  public void addPostParam(String key, String value){
    request.addPostParam(key, value);
  }

  public String makeRequest(){

    String responseString = "";
    try{
      PrintWriter pw = new PrintWriter(socket.getOutputStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

      pw.print(request);
      System.out.println(request);
      pw.flush();

      int nextChar;
      while ((nextChar = br.read()) != -1) {
        responseString += (char) nextChar;
      }


    }catch(Exception e){
      return "Error";
    }


    response = new HTTPResponse(responseString);
    return responseString;
  }

  public void setHostName(String hostName){
    request.setHostName(hostName);
  }

  public void setRequestType(String requestType){
    request.setRequestType(requestType);
  }

  public List<Pair<String,String>> getResponseHeaders(){
    return response.getHeaders();
  }

  public int getStatusCode(){
    return response.getStatusCode();
  }

  public Pair<String,String> getResponseHeader(String key){
    for(Pair<String,String> pair : response.getHeaders()){
      if(pair.getFirst().equals(key)){
        return pair;
      }
    }

    return null;
  }

  public String getRequest(){
    return request.toString();
  }

  public String getResponse(){
    return response.toString();
  }

}
