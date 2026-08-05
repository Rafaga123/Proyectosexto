package com.brk.chessrunner.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ApiClient {

    public static String getBaseUrl() {
        String ip = Gdx.app.getPreferences("ChessRunnerSettings")
            .getString("serverIp", "192.168.1.100");
        return "http://" + ip + ":58080/api";
    }

    // Interfaz para manejar las respuestas sin congelar el juego
    public interface ApiCallback {
        void onExito(JsonValue respuesta);
        void onError(String mensajeError);
    }

    /**
     * Traduce códigos de estado HTTP a mensajes amigables para el usuario.
     */
    private static String traducirError(int statusCode, String rawBody) {
        // Si el cuerpo tiene texto (y no es un JSON gigante), a menudo es el mensaje de error del Result Pattern
        if (rawBody != null && !rawBody.isEmpty() && rawBody.length() < 100 && !rawBody.contains("{")) {
            return rawBody;
        }

        return switch (statusCode) {
            case 401 -> "Credenciales incorrectas. Revise su correo y contraseña.";
            case 403 -> "Acceso denegado. Su cuenta podría estar inactiva o bloqueada.";
            case 404 -> "El servidor no responde. Revise la configuración de la IP.";
            case 500 -> "Error interno del servidor. Intente de nuevo en unos momentos.";
            default -> "Error inesperado (" + statusCode + "). Revise su conexión a internet.";
        };
    }

    /**
     * Petición para iniciar sesión en Spring Boot
     */
    public static void login(String correo, String password, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        // Construimos el JSON a mano para este ejemplo simple
        String jsonBody = "{\"correo\":\"" + correo + "\", \"password\":\"" + password + "\"}";

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(getBaseUrl() + "/usuarios/login")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(jsonBody)
            .build();

        // Enviamos la petición asíncrona
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String resultAsString = httpResponse.getResultAsString();

                if (statusCode >= 200 && statusCode < 300) {
                    // Si el servidor responde OK, parseamos el JSON de respuesta
                    JsonReader jsonReader = new JsonReader();
                    JsonValue json = jsonReader.parse(resultAsString);
                    callback.onExito(json);
                } else {
                    callback.onError(traducirError(statusCode, resultAsString));
                }
            }

            @Override
            public void failed(Throwable t) {
                callback.onError("Revise su conexión a internet");
            }

            @Override
            public void cancelled() {
                callback.onError("Conexión cancelada");
            }
        });
    }

    /**
     * Petición para registrar una nueva cuenta en Spring Boot
     */
    public static void registrar(String id, String alias, String correo, String password, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        String jsonBody = "{\"id\":\"" + id + "\", \"alias\":\"" + alias +
            "\", \"correo\":\"" + correo + "\", \"password\":\"" + password + "\"}";

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(getBaseUrl() + "/usuarios")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(jsonBody)
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String resultAsString = httpResponse.getResultAsString();

                if (statusCode >= 200 && statusCode < 300) {
                    JsonReader jsonReader = new JsonReader();
                    JsonValue json = jsonReader.parse(resultAsString);
                    callback.onExito(json);
                } else {
                    callback.onError(traducirError(statusCode, resultAsString));
                }
            }

            @Override
            public void failed(Throwable t) {
                callback.onError("Revise su conexión a internet");
            }

            @Override
            public void cancelled() {
                callback.onError("Conexión cancelada");
            }
        });
    }

    /**
     * Petición para enviar partidas locales a Spring Boot
     */
    public static void sincronizarPartidas(String usuarioId, java.util.List<com.brk.chessrunner.database.PartidaLocal> partidas, ApiCallback callback) {
        if (partidas.isEmpty()) {
            callback.onError("No hay partidas pendientes por sincronizar.");
            return;
        }

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        // Construimos el JSON Array manualmente (para no depender de librerías externas complejas)
        StringBuilder jsonBody = new StringBuilder("[");
        for (int i = 0; i < partidas.size(); i++) {
            com.brk.chessrunner.database.PartidaLocal p = partidas.get(i);
            jsonBody.append("{")
                .append("\"id\":\"").append(p.getId()).append("\",")
                .append("\"puntuacion\":").append(p.getPuntuacion()).append(",")
                .append("\"nivelAlcanzado\":").append(p.getNivelAlcanzado()).append(",")
                .append("\"piezaMortal\":\"").append(p.getPiezaMortal()).append("\",")
                .append("\"tiempoSobrevivido\":").append(p.getTiempoSobrevivido()).append(",")
                .append("\"fechaPartida\":\"").append(p.getFechaPartida()).append("\"")
                .append("}");
            if (i < partidas.size() - 1) jsonBody.append(",");
        }
        jsonBody.append("]");

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(getBaseUrl() + "/partidas/sync/" + usuarioId)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(jsonBody.toString())
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String resultAsString = httpResponse.getResultAsString();

                if (statusCode >= 200 && statusCode < 300) {
                    // Retornamos un JSON vacío o de éxito, ya que lo importante es el status 200 OK
                    callback.onExito(new JsonReader().parse("{\"status\":\"success\"}"));
                } else {
                    callback.onError(traducirError(statusCode, resultAsString));
                }
            }
            @Override
            public void failed(Throwable t) { callback.onError("Revise su conexión a internet"); }
            @Override
            public void cancelled() { callback.onError("Conexión cancelada"); }
        });
    }
}
