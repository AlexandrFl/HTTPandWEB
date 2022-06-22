
public class Main {
  public static void main(String[] args) {
    final int PORT = 8080;
    final int THREAD_COUNT = 64;

    Server server = new Server(PORT, THREAD_COUNT);
    server.start();
  }
}
