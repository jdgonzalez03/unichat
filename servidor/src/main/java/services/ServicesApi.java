package services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.ConfigLoader;
import utils.Logger;
import services.ServiceUser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ServicesApi {
    private static ServicesApi instance;
    private HttpServer server;
    private final ConfigLoader config = ConfigLoader.getInstance();
    private final Logger logger = Logger.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();  // Jackson
    private final ServiceUser serviceUser;

    private ServicesApi() {
        this.serviceUser = new ServiceUser();
    }

    public static synchronized ServicesApi getInstance() {
        if (instance == null) {
            instance = new ServicesApi();
        }
        return instance;
    }

    public void initAPI() throws IOException {
        int port = Integer.parseInt(config.getServerApiPort());
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new HelloHandler());
        server.createContext("/" + config.getServerName() + "/api/users", new AllUsersHandler());

        server.setExecutor(null);
        server.start();

        logger.log("Servidor para API Rest inicializado en el puerto: " + server.getAddress().getPort());
    }

    // Handler que responde un JSON simple con Jackson
    private class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Objeto que queremos enviar como JSON
            ResponseMessage responseMessage = new ResponseMessage("success", "API está funcionando correctamente");

            // Convertir objeto a JSON
            String response = mapper.writeValueAsString(responseMessage);

            // Configurar headers
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class AllUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            logger.log("Request: " + exchange.getRequestURI());

            // Objeto que queremos enviar como JSON

            // Convertir objeto a JSON
            String response = mapper.writeValueAsString(serviceUser.allUsers());

            // Configurar headers
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Clase para la estructura JSON de prueba. Pero se pueden usar las clases de los objetos.
    private static class ResponseMessage {
        private String status;
        private String message;

        public ResponseMessage(String status, String message) {
            this.status = status;
            this.message = message;
        }

        // Getters para Jackson
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    public void stopAPI() {
        if (server != null) {
            server.stop(0);
            logger.log("Servidor detenido exitosamente.");
        }
    }
}
