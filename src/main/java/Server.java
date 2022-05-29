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

    public Server(int port, int threadCount) {
        this.PORT = port;
        this.THREAD_COUNT = threadCount;
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
                 final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                final var requestLine = in.readLine();
                if (requestLine != null) {
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        continue;
                    }
                    final var path = parts[1];
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
                        continue;
                    }

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
