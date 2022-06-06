import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Request {

    private final String method;
    private final String URL;
    private final String path;
    private final String query;
    private final String prot;
    private final int requestLength;
    private List<NameValuePair> params = null;

    public Request(String method, String URL, String path, String query, String prot, int requestLength) {
        this.method = method;
        this.URL = URL;
        this.path = path;
        this.query = query;
        this.prot = prot;
        this.requestLength = requestLength;
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

    public int getLength() {
        return requestLength;
    }

    public void getQueryParams() throws URISyntaxException {
        params = URLEncodedUtils.parse(new URI(URL), "UTF-8");
        if (!params.isEmpty()) {
            System.out.println("Параметры из строки запроса");
            for (NameValuePair param : params) {
                System.out.println(param.getName() + " : " + param.getValue());
            }
        }
    }

    public void getQueryParam(String name) throws URISyntaxException {
        params = URLEncodedUtils.parse(new URI(URL), "UTF-8");
        if (!params.isEmpty()) {
            System.out.println("Параметры из строки запроса");
            for (NameValuePair param : params) {
                if (param.getName().equals(name)) {
                    System.out.println(param.getName() + " : " + param.getValue());
                }
            }
        }
    }

    @Override
    public String toString() {
        if (method.equals("POST")) {
            return "Протокол - " + prot + "\nМетод - " + method + "\nПуть - " + path;
        }
        return "Протокол - " + prot + "\nМетод - " + method + "\nПуть - " + path + query;
    }
}
