import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Parser {
    public Request getRequest(String startURL, BufferedInputStream in) throws IOException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);
        final var requestLineDelim = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelim, 0, read);
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }
        final var method = requestLine[0];
        final var pathAndQuery = requestLine[1];
        if (!pathAndQuery.startsWith("/") && pathAndQuery.startsWith("/favicon")) {
            return null;
        }
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelim.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }
        in.reset();
        in.skipNBytes(headersStart);
        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        String body = null;
        if (!method.equals("GET")) {
            in.skipNBytes(headersDelimiter.length);
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                body = new String(bodyBytes);
            }
        }
        var protocol = requestLine[2];
        if (method.equals("GET")) {
            var pathAndQueryParts = pathAndQuery.split("\\?");
            if (pathAndQueryParts.length == 2) {
                var path = pathAndQueryParts[0];
                var query = "?" + pathAndQueryParts[1];
                var URL = startURL + path + query;
                return new Request(method, URL, path, query, protocol, null);
            } else if (pathAndQueryParts.length == 1) {
                var path = pathAndQueryParts[0];
                var URL = startURL + path;
                return new Request(method, URL, path, null, protocol, null);
            }
        }
        var URL = startURL + pathAndQuery;
        return new Request(method, URL, pathAndQuery, null, protocol, body);
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
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