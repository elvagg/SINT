import com.sun.net.httpserver.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ProxyServer {
    public static BlackList blackList = new BlackList();
    public static Statistics statistics = new Statistics();
    public static ConData connectionData = new ConData();

    public static void main(String[] args) throws Exception {
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ProxyHandler());
        System.out.println("Starting server on port: " + port);
        server.start();
    }

    static class ProxyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
           try {


            statistics.readDataFromFile();
            //class collecting statistics for this request
            ConData connectionData = new ConData();

            URI weburi = exchange.getRequestURI();
            URL url = new URL(weburi.toString());
            HttpURLConnection webProxyConnection
                    = (HttpURLConnection) url.openConnection();

            blackList.getBlackList();
            if (blackList.contains(weburi.toString())){
                System.out.println("Forbidden URL");
                exchange.sendResponseHeaders(403, -1);
            } else {
                Headers requestHeaders = exchange.getRequestHeaders();
                String method = exchange.getRequestMethod();
                if (requestHeaders != null) {
                    for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                        if (!entry.getKey().equals("Transfer-Encoding"))
                            webProxyConnection.addRequestProperty(entry.getKey(), String.join(",", entry.getValue()));
                    }
                }
                webProxyConnection.setInstanceFollowRedirects(false);
                webProxyConnection.setAllowUserInteraction(true);
                webProxyConnection.setDoInput(true);
                webProxyConnection.setDoOutput(true);
                webProxyConnection.setUseCaches(false);
                webProxyConnection.setRequestMethod(method);

                byte[] requestBytes = new byte[0];
                if (exchange.getRequestHeaders().containsKey("Content-Length")) {
                    if ((Integer.parseInt(exchange.getRequestHeaders().get("Content-Length").get(0)))>=0) {
                        InputStream is = exchange.getRequestBody();
                        OutputStream os = webProxyConnection.getOutputStream();
                        requestBytes = IOUtils.toByteArray(is);
                        os.write(requestBytes);
                        is.close();
                        os.close();
                    }
                }

//
//
//            if (exchange.getRequestHeaders().containsKey("Content-Type")) {
//                if (Integer.parseInt(exchange.getRequestHeaders().get("Content-Length").get(0)) >= 0) {
//                    IOUtils.copy(exchange.getRequestBody(), webProxyConnection.getOutputStream());
//                }
//            }
                webProxyConnection.connect();

                Map<String, List<String>> responseHeaders = webProxyConnection.getHeaderFields();

                for (String headerName : responseHeaders.keySet()) {
                    if (headerName != null && !headerName.equals("Transfer-Encoding")) {
                        exchange.getResponseHeaders().put(headerName, responseHeaders.get(headerName));
                    }
                }

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                //response code
                int contentLength = 0;
//            byte[] requestBytes = new byte[0];
                try {
                    if (responseHeaders.containsKey("Content-Length")) {
                        String contentLengthStr = responseHeaders.get("Content-Length").get(0);
                        contentLength = Integer.parseInt(contentLengthStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                exchange.sendResponseHeaders(webProxyConnection.getResponseCode(), contentLength);
                byte[] bytes = new byte[100];
                try {
                    if (webProxyConnection.getInputStream().available() != 0) {
//                    IOUtils.copy(webProxyConnection.getInputStream(), exchange.getResponseBody());
//                    System.out.println("Reading from InputStream");
                        InputStream is = webProxyConnection.getInputStream();
                        bytes = IOUtils.toByteArray(is);
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        is.close();
                        os.close();
                    }
                } catch (Exception e) {
                    try {
                        if (webProxyConnection.getErrorStream().available() != 0) {
//                        System.out.println("Code: " + webProxyConnection.getResponseCode());
//                        IOUtils.copy(webProxyConnection.getErrorStream(), exchange.getResponseBody());
//                        System.out.println("Reading from ErrorStream");
                            InputStream es = webProxyConnection.getErrorStream();
                            bytes = IOUtils.toByteArray(es);
                            OutputStream os = exchange.getResponseBody();
                            os.write(bytes);
                            es.close();
                            os.close();
                        }
                    } catch (Exception ee) {
                        System.out.println("NullPointer Exception - No Body ");
                    }
                }
//            if (200 <= webProxyConnection.getResponseCode() && webProxyConnection.getResponseCode() < 300) {
//                IOUtils.copy(webProxyConnection.getInputStream(),exchange.getResponseBody());
//            } else {
//                IOUtils.copy(webProxyConnection.getErrorStream(),exchange.getResponseBody());
//            }
                webProxyConnection.getInputStream().close();
                exchange.getResponseBody().close();


                connectionData.url = exchange.getRequestHeaders().get("Host").get(0);
                connectionData.dataSend = (long)requestBytes.length;
                connectionData.dataGot = (long)bytes.length;


                statistics.insertData(connectionData);
                System.out.println(connectionData.toString());
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

