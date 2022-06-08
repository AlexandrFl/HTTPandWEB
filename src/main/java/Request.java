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
    private final String prot;
    private final String body;
    private List<NameValuePair> params = null;

    public Request(String method, String URL, String path, String query, String prot, String body) {
        this.method = method;
        this.URL = URL;
        this.path = path;
        this.query = query;
        this.prot = prot;
        this.body = body;
    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProt() {
        return prot;
    }

    public String getBody() {
        return body;
    }

    public void getQueryParams() {
        params = URLEncodedUtils.parse(URI.create(URL), StandardCharsets.UTF_8);
        if (!params.isEmpty()) {
            System.out.println("Параметры из строки запроса");
            for (NameValuePair param : params) {

                    System.out.println(param.getName() + " : " + param.getValue());

            }
        }
    }

    public void getQueryParam(String name) {
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
    }

    public void getPostParams() {
        HashMap<String, String> p = new HashMap<>();
        System.out.println("Параметры из тела запроса");
        final var bodyParts = body.split("&");
        for (String bodyPart : bodyParts) {
            var keyAndValue = bodyPart.split("=");
            if (keyAndValue.length == 2) {
                p.put(keyAndValue[0], keyAndValue[1]);
                System.out.println(keyAndValue[0] + " : " + keyAndValue[1]);
            } else {
                p.put(keyAndValue[0], null);
                System.out.println(keyAndValue[0]);
            }
        }
    }

    public void getPostParam(String name) {
        HashMap<String, String> p = new HashMap<>();
        System.out.println("Параметры из тела запроса с именем " + name);
        final var bodyParts = body.split("&");
        for (String bodyPart : bodyParts) {
            var keyAndValue = bodyPart.split("=");
            if (keyAndValue.length == 2) {
                p.put(keyAndValue[0], keyAndValue[1]);
                if (keyAndValue[0].equals(name)) {
                    System.out.println(keyAndValue[0] + " : " + keyAndValue[1]);
                } else {
                    p.put(keyAndValue[0], null);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (method.equals("POST")) {
            return "Протокол - " + prot + "\nМетод - " + method + "\nПуть - " + path + "\nТело запроса - " + body;
        } else if (method.equals("GET") && body != null) {
            return "Протокол - " + prot + "\nМетод - " + method + "\nПуть - " + path + query + "\nТело запроса - " + body;
        }
        return "Протокол - " + prot + "\nМетод - " + method + "\nПуть - " + path + query;
    }


}
