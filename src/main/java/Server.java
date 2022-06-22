import org.apache.http.NameValuePair;

import java.io.*;
import java.net.ServerSocket;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int THREAD_COUNT;
    private final int PORT;
    private final String startURL = "http://localhost:";

    public Server(int port, int threadCount) {
        this.PORT = port;
        this.THREAD_COUNT = threadCount;
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        try (final var serverSocket = new ServerSocket(PORT)) {
            final var port = serverSocket.getLocalPort();
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> processing(socket, port));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processing(Socket socket, int port) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Parser parser = new Parser();
            Request request = parser.getRequest(startURL + port, in);
            if (request == null) {
                return;
            }
            if (!request.getPath().startsWith("/favicon")) {
                final var path = request.getPath();
                if (!validPaths.contains(path)) {
                    out.write((
                            """
                                    HTTP/1.1 404 Not Found\r
                                    Content-Length: 0\r
                                    Connection: close\r
                                    \r
                                    """
                    ).getBytes());
                    out.flush();
                    return;
                }

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                } else {
                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();

                }
                if (!request.getMethod().equals("GET")) {
                    HashMap<String, String> postParams = request.getPostParams();
                    HashMap<String, String> postParam = request.getPostParam("value");
                }
                if (!request.getMethod().equals("POST")) {
                    List<NameValuePair> queryParams = request.getQueryParams();
                    List<NameValuePair> queryParam = request.getQueryParam("value");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
