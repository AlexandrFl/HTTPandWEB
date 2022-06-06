
import java.io.*;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String GET = "GET";
    private static final String POST = "POST";

    private final int THREAD_COUNT;
    private final int PORT;
    private final String startURL = "http://localhost:9090";

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
        final var allowedMethods = List.of(GET, POST);
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        while (true) {
            try (final var socket = serverSocket.accept();
                 final var in = new BufferedInputStream(socket.getInputStream());
                 final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {

                final var limit = 4096;
                in.mark(limit);
                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final var requetLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
//
                final var requestLine = new String(Arrays.copyOf(buffer, requetLineEnd));

                Request request = new Parser().getRequest(requestLine, startURL);

                System.out.println(request);
                request.getQueryParam("value");
                System.out.println();
                request.getQueryParams();


                if (request.getLength() != 3) {
                    continue;
                }
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

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
