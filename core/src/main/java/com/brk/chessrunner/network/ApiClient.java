package com.brk.chessrunner.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ApiClient {
    // Luego buscar la forma de que esta BASE_URL se pueda cambiar dinamicamente durante la ejecución de la aplicación
    private static final String BASE_URL = "http://localhost:58080/api";

    // Interfaz para manejar las respuestas sin congelar el juego
    public interface ApiCallback {
        void onExito(JsonValue respuesta);
        void onError(String mensajeError);
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
            .url(BASE_URL + "/usuarios/login")
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

                if (statusCode == 200 || statusCode == 201) {
                    // Si el servidor responde OK, parseamos el JSON de respuesta
                    JsonReader jsonReader = new JsonReader();
                    JsonValue json = jsonReader.parse(resultAsString);
                    callback.onExito(json);
                } else {
                    callback.onError("Error del servidor. Código: " + statusCode + " - " + resultAsString);
                }
            }

            @Override
            public void failed(Throwable t) {
                callback.onError("Fallo de conexión. ¿Está encendida la API? Detalles: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onError("Petición cancelada");
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
                // Tu API quizás requiera estos campos extras, los mandamos en 0 o vacíos por ahora
                .append("\"nivelAlcanzado\":0,")
                .append("\"piezaMortal\":\"\",")
                .append("\"tiempoSobrevivido\":").append(p.getTiempoSobrevivido()).append(",")
                .append("\"fechaPartida\":\"").append(java.time.LocalDateTime.now().toString()).append("\"")
                .append("}");
            if (i < partidas.size() - 1) jsonBody.append(",");
        }
        jsonBody.append("]");

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + "/partidas/sync/" + usuarioId)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(jsonBody.toString())
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    // Retornamos un JSON vacío o de éxito, ya que lo importante es el status 200 OK
                    callback.onExito(new JsonReader().parse("{\"status\":\"success\"}"));
                } else {
                    callback.onError("Error de sincronización. Código: " + statusCode);
                }
            }
            @Override
            public void failed(Throwable t) { callback.onError("Fallo de conexión: " + t.getMessage()); }
            @Override
            public void cancelled() { callback.onError("Sincronización cancelada"); }
        });
    }
}
