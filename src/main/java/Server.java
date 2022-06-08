
import java.io.*;
import java.net.ServerSocket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int THREAD_COUNT;
    private final int PORT;
    private final String startURL = "http://localhost:9090";
    private final String NAME_OF_QUERY_PARAM;
    private final String NAME_OF_BODY_PARAM;

    public Server(int port, int threadCount, String NAME_OF_QUERY_PARAM, String NAME_OF_BODY_PARAM) {
        this.PORT = port;
        this.THREAD_COUNT = threadCount;
        this.NAME_OF_QUERY_PARAM = NAME_OF_QUERY_PARAM;
        this.NAME_OF_BODY_PARAM = NAME_OF_BODY_PARAM;
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(PORT)) {
            ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
            while (!serverSocket.isClosed()) {
                threadPool.submit(processing(serverSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Runnable processing(ServerSocket serverSocket) {

        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        while (true) {
            try (final var socket = serverSocket.accept();
                 final var in = new BufferedInputStream(socket.getInputStream());
                 final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {

                Parser parser = new Parser();
                Request request = parser.getRequest(startURL, in);
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
                        continue;
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
                        request.getPostParams();
                        request.getPostParam(NAME_OF_BODY_PARAM);
                    }
                    if (!request.getMethod().equals("POST")) {
                        request.getQueryParams();
                        request.getQueryParam(NAME_OF_QUERY_PARAM);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
