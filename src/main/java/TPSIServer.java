import com.cedarsoftware.util.io.JsonWriter;
import com.sun.net.httpserver.*;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
//import org.apache.commons.codec.binary.Base64;

public class TPSIServer {
	public static void main(String[] args) throws Exception {
		int port = 8000;
		String mainPath = args[0];

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new RootHandler(new File(mainPath).getCanonicalPath()));
		server.createContext("/echo/", new EchoHandler());
		server.createContext("/redirect/", new RedirectHandler());
		server.createContext("/cookies/", new CookiesHandler());
		server.createContext("/auth/", new AuthenticationHandler());
		HttpContext auth2cont = server.createContext("/auth2/", new BasicAuthenticatorHandler());
		auth2cont.setAuthenticator(	new BasicAuthenticator("user") {
			@Override
			public boolean checkCredentials(String username, String password) {
				return username.equals("admin") && password.equals("admin");
			}
		});
		System.out.println("Starting server on port: " + port);
		server.start();
	}

	static class RootHandler implements HttpHandler {
		private String mPath;

		RootHandler(String mainPath) {
			mPath = mainPath;
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestedURL = exchange.getRequestURI().getPath().replace("/", "\\");
			System.out.println(requestedURL);
			String filePath = mPath + requestedURL;
			File requestedFile = new File(filePath);
			String requestedFilePath = requestedFile.getCanonicalPath();
			boolean isFound = requestedFilePath.startsWith(mPath);
			System.out.println("Żądana ścieżka: " + isFound);
			OutputStream os = exchange.getResponseBody();
			if (isFound) {
				boolean exists = requestedFile.exists();

				if (exists) {
					if (requestedFile.isDirectory()) {
						File[] listOfFiles = requestedFile.listFiles();
						String fileURLS = "";
						for (int i = 0; i < listOfFiles.length; i++) {
							String path = listOfFiles[i].getPath().replace(mPath, "");
							String path_correct = path.replace("/", "\\");
							fileURLS += "<a href=\"" + path_correct + "\">" + listOfFiles[i].getName() + "</a><br />";
						}

						File htmlTemplateFile = new File("src//main/java/files.html");
						String htmlString = FileUtils.readFileToString(htmlTemplateFile);
						htmlString = htmlString.replace("$body", fileURLS);
						File newHtmlFile = new File("src//main/java/files_ready.html");
						FileUtils.writeStringToFile(newHtmlFile, htmlString);

						byte[] response = Files.readAllBytes(Paths.get("src//main/java/files_ready.html"));
						exchange.getResponseHeaders().set("Content-Type", "text/html");
						exchange.sendResponseHeaders(200, response.length);
						os.write(response);
					} else {
						String mimeType = Files.probeContentType(requestedFile.toPath());
						String[] path = exchange.getRequestURI().getPath().split("/");
						String requestedFileName = path[path.length-1];
						byte[] response = Files.readAllBytes(Paths.get(requestedFile.getCanonicalPath()));
						exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + requestedFileName);
						System.out.println(mimeType);
						if(!mimeType.equals("null")) {
							exchange.getResponseHeaders().set("Content-Type", mimeType);
							exchange.sendResponseHeaders(200, response.length);
							os.write(response);
						} else {
							exchange.sendResponseHeaders(501, -1);
						}
					}
				} else {
					exchange.sendResponseHeaders(404, -1);
				}
			} else {
				exchange.sendResponseHeaders(403, -1);
			}
			os.close();
		}
	}

	static class EchoHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			Headers requestHeaders = exchange.getRequestHeaders();

			Map<String, Object> jsonArgs = new HashMap<>();
			jsonArgs.put(JsonWriter.PRETTY_PRINT, true);
			String response = JsonWriter.objectToJson(requestHeaders,jsonArgs);

			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	static class RedirectHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			String[] path = exchange.getRequestURI().getPath().split("/");
			int code = Integer.parseInt(path[path.length-1]);

			exchange.getResponseHeaders().set("Location", "https://www.google.com");
			exchange.sendResponseHeaders(code, -1);
			System.out.print("Redirection status: " + code + "\n");
			OutputStream os = exchange.getResponseBody();
			os.close();

		}
	}


	//SPOSTRZEŻENIA:
	//Jeśli parametry Path oraz Domain są niezdefiniowane, to w sesji zapamiętane zostaje pierwsze wygenerowane ciasteczko i nie można go nadpisać
	//Ciasteczko przesyłane pod inne URI jest nadpisywane jeśli zdefiniowane są Path oraz Domain
	//Ciasteczko zostanie zapamiętane w sesji nawet jeśli serwer przestanie działać
	//Jeśli domain jest niepoprawnie określone, ciasteczko nie zostanie wysłane i zapamiętane jest ostatnie wysłane ciasteczko w sesji
	static class CookiesHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			Random rand = new Random();
			int id = rand.nextInt(50);
			//List<String> domain = exchange.getRequestHeaders().get("Domain");
			//System.out.println("Domain name: " + domain);


			System.out.print("Cookie ID generated: " + id + "\n");
			exchange.getResponseHeaders().set("Content-Type", "text/html");

			exchange.getResponseHeaders().set("Set-Cookie", "SID=" + id + "; Path=/echo/; Domain=");
			exchange.sendResponseHeaders(200, -1);
			OutputStream os = exchange.getResponseBody();
			os.close();
		}
	}

	static class AuthenticationHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			String auth = exchange.getRequestHeaders().getFirst("Authorization");
			String userpass = null;
			if (auth != null) {
				auth = auth.split(" ")[1];
				byte[] decoded = Base64.getDecoder().decode(auth);
				userpass = new String(decoded);
				System.out.println(auth.getBytes("UTF-8"));
			}

			if (auth == null) {
				exchange.getResponseHeaders().set("WWW-Authenticate", "Basic");
				exchange.getResponseHeaders().set("charset", "UTF-8");
				exchange.sendResponseHeaders(401, -1);
				exchange.close();
			} else {
				if (!userpass.equals("admin:admin")) {
					exchange.getResponseHeaders().set("WWW-Authenticate", "Basic");
					exchange.sendResponseHeaders(401, -1);
					exchange.close();
				} else {
					String response = "Authorization completed successfully.";
					exchange.sendResponseHeaders(200, response.getBytes().length);
					exchange.getResponseHeaders().set("Content-Type", "text/plain");
					OutputStream os = exchange.getResponseBody();
					os.write(response.getBytes());
					os.close();
				}
			}
		}
	}


	static class BasicAuthenticatorHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			exchange.getRequestBody();
				String response = "OK";
				exchange.sendResponseHeaders(200, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
		}
	}
}


