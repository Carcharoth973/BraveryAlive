package com.braveryalive;

import com.braveryalive.controller.BuildController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebApplication {
    public static void main(String[] args) throws IOException {
        BuildController controller = new BuildController();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // API endpoints
        server.createContext("/api/roll", controller::handleRoll);
        server.createContext("/api/version", controller::handleVersion);
        server.createContext("/api/export/discord", controller::handleExportDiscord);
        server.createContext("/api/export/league", controller::handleExportLeague);

        // Static files
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            // Remove leading slash for resource loading
            String resourcePath = path.startsWith("/") ? path.substring(1) : path;

            try {
                // Try to load from classpath (works for both development and JAR)
                var resourceStream = getClass().getClassLoader().getResourceAsStream("static/" + resourcePath);
                if (resourceStream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                byte[] content = resourceStream.readAllBytes();
                resourceStream.close();

                // Set content type based on file extension
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);

                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".gif")) return "image/gif";
            return "text/plain";
        }
    }
            }

