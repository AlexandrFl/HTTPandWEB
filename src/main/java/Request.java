import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class Request {

    private final String method;
    private final String URL;
    private final String path;
    private final String query;
    private final String protocol;
    private final String body;
    private List<NameValuePair> params = null;

    public Request(String method, String URL, String path, String query, String protocol, String body) {
        this.method = method;
        this.URL = URL;
        this.path = path;
        this.query = query;
        this.protocol = protocol;
        this.body = body;
    }
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
    public String getProtocol() {
        return protocol;
    }
    public String getQuery() {
        return query;
    }
    public String getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        params = URLEncodedUtils.parse(URI.create(URL), StandardCharsets.UTF_8);
        if (!params.isEmpty()) {
            System.out.println("Параметры из строки запроса");
            for (NameValuePair param : params) {

                System.out.println(param.getName() + " : " + param.getValue());
            }
        }
        return params;
    }
    public List<NameValuePair> getQueryParam(String name) {
        try {
            params = URLEncodedUtils.parse(new URI(URL), StandardCharsets.UTF_8);
            if (!params.isEmpty()) {
                System.out.println("Параметры из строки запроса с именем " + name);
                for (NameValuePair param : params) {
                    if (param.getName().equals(name)) {
                        System.out.println(param.getName() + " : " + param.getValue());
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return params;
    }

    public HashMap<String, String> getPostParams() {
        HashMap<String, String> postParams = new HashMap<>();
        System.out.println("Параметры из тела запроса");
        final var bodyParts = body.split("&");
        for (String bodyPart : bodyParts) {
            var keyAndValue = bodyPart.split("=");
            if (keyAndValue.length == 2) {
                postParams.put(keyAndValue[0], keyAndValue[1]);
                System.out.println(keyAndValue[0] + " : " + keyAndValue[1]);
            } else {
                postParams.put(keyAndValue[0], null);
                System.out.println(keyAndValue[0]);
            }
        }
        return postParams;
    }

    public HashMap<String, String> getPostParam(String name) {
        HashMap<String, String> postParam = new HashMap<>();
        System.out.println("Параметры из тела запроса с именем " + name);
        final var bodyParts = body.split("&");
        for (String bodyPart : bodyParts) {
            var keyAndValue = bodyPart.split("=");
            if (keyAndValue.length == 2) {
                postParam.put(keyAndValue[0], keyAndValue[1]);
                if (keyAndValue[0].equals(name)) {
                    System.out.println(keyAndValue[0] + " : " + keyAndValue[1]);
                } else {
                    postParam.put(keyAndValue[0], null);
                }
            }
        }
        return postParam;
    }
    @Override
    public String toString() {
        return "Метод - " + getMethod() + ", Путь - " + getPath() + ", Протокол - " + getProtocol() +
                "\nQuery - " + getQuery() + ", Тело запроса - " + getBody();
    }
}
