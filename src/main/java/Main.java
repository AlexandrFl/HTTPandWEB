import org.apache.http.client.utils.URLEncodedUtils;

public class Main {
  public static void main(String[] args) {
    final int PORT = 9090;
    final int THREAD_COUNT = 64;

    Server server = new Server(PORT, THREAD_COUNT);
    server.start();
  }

}
