package me.elephantsuite.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.elephantsuite.Main;
import me.elephantsuite.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

// make http requests but easily
public class Request {

    private final static HttpClient CLIENT = HttpClient.newHttpClient();

    private final static String BACKEND_PATH = Main.PRIVATE_CONFIG.getConfigOption("elephantBackendDomain");

    private final String path;

    //called on completed request

    private final Method method;

    private final JsonObject body;

    public Request(String path, Method method, @Nullable JsonObject body) {
        this.path = path;
        this.method = method;
        this.body = body;
    }

    public JsonObject makeRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder()
                .method(method.name(), JsonUtils.toBody(body))
                .uri(URI.create(BACKEND_PATH + this.path))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        Main.LOGGER.info("Made " + this.method.name() + " request to " + BACKEND_PATH + this.path + " with body: " + JsonUtils.objToString(this.body));

        return JsonUtils.toJsonObj(response.body());
    }

    public JsonObject getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public Method getMethod() {
        return method;
    }
}
