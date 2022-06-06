public class Parser {

//    private final String request;
//    private final String startURL = "http://localhost:9090";

    public Request getRequest(String request, String startURL) {
        var parsePart = request.split(" ");
        if (parsePart.length != 3) {
            return null;
        }
        var prot = parsePart[2];
        var method = parsePart[0];
        if (method.equals("GET")) {
            var delimer = parsePart[1].indexOf('?');
            var path = parsePart[1].substring(0, delimer);
            var query = parsePart[1].substring(delimer);
            var URL = startURL + path + query;
            return new Request(method, URL, path, query, prot, parsePart.length);
        } else if (method.equals("POST")) {
            var path = parsePart[1];
            var URL = startURL + path;
            return new Request(method, URL, path, null, prot, parsePart.length);
        } else {
            return null;
        }
    }
}