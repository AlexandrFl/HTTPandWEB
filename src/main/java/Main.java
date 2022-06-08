
public class Main {
  public static void main(String[] args) {
    final int PORT = 9090;
    final int THREAD_COUNT = 64;
    final String NAME_OF_QUERY_PARAM = "value";
    final String NAME_OF_BODY_PARAM = "value";

    Server server = new Server(PORT, THREAD_COUNT, NAME_OF_QUERY_PARAM, NAME_OF_BODY_PARAM);
    server.start();
  }
}
