package services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.ConfigLoader;
import utils.Logger;
import services.ServiceUser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.management.OperatingSystemMXBean;

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
        server.createContext("/" + config.getServerName() + "/api/system-info", new SystemInfoHandler()); // NUEVO

        server.setExecutor(null);
        server.start();

        logger.log("Servidor para API Rest inicializado en el puerto: " + server.getAddress().getPort());
    }

    // --- Handlers ---

    private class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            ResponseMessage responseMessage = new ResponseMessage("success", "API está funcionando correctamente");
            String response = mapper.writeValueAsString(responseMessage);
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

            String response = mapper.writeValueAsString(serviceUser.allUsers());
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class SystemInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            // JVM memory
            long freeJvmMemory = runtime.freeMemory();
            long totalJvmMemory = runtime.totalMemory();
            long usedJvmMemory = totalJvmMemory - freeJvmMemory;

            // Physical memory
            long freePhysicalMemory = osBean.getFreePhysicalMemorySize();
            long totalPhysicalMemory = osBean.getTotalPhysicalMemorySize();
            long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

            // Disk space (from root partition)
            File root = new File("/");
            long totalDisk = root.getTotalSpace();
            long freeDisk = root.getFreeSpace();
            long usedDisk = totalDisk - freeDisk;

            // CPU usage
            double cpuLoad = osBean.getSystemCpuLoad() * 100.0; // Convertir a porcentaje

            SystemInfoResponse sysInfo = new SystemInfoResponse(
                    System.getProperty("os.name"),
                    osBean.getAvailableProcessors(),
                    Math.round(cpuLoad * 100.0) / 100.0, // redondeo a 2 decimales
                    usedJvmMemory / (1024 * 1024),
                    freeJvmMemory / (1024 * 1024),
                    totalJvmMemory / (1024 * 1024),
                    usedPhysicalMemory / (1024 * 1024),
                    freePhysicalMemory / (1024 * 1024),
                    totalPhysicalMemory / (1024 * 1024),
                    usedDisk / (1024 * 1024 * 1024),
                    freeDisk / (1024 * 1024 * 1024),
                    totalDisk / (1024 * 1024 * 1024)
            );

            String response = mapper.writeValueAsString(sysInfo);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }


    // --- Clases auxiliares ---

    private static class ResponseMessage {
        private String status;
        private String message;

        public ResponseMessage(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    private static class SystemInfoResponse {
        private final String osName;
        private final int availableProcessors;
        private final double cpuUsagePercent;

        private final long usedJvmMemoryMB;
        private final long freeJvmMemoryMB;
        private final long totalJvmMemoryMB;

        private final long usedPhysicalMemoryMB;
        private final long freePhysicalMemoryMB;
        private final long totalPhysicalMemoryMB;

        private final long usedDiskGB;
        private final long freeDiskGB;
        private final long totalDiskGB;

        public SystemInfoResponse(String osName, int availableProcessors, double cpuUsagePercent,
                                  long usedJvmMemoryMB, long freeJvmMemoryMB, long totalJvmMemoryMB,
                                  long usedPhysicalMemoryMB, long freePhysicalMemoryMB, long totalPhysicalMemoryMB,
                                  long usedDiskGB, long freeDiskGB, long totalDiskGB) {
            this.osName = osName;
            this.availableProcessors = availableProcessors;
            this.cpuUsagePercent = cpuUsagePercent;
            this.usedJvmMemoryMB = usedJvmMemoryMB;
            this.freeJvmMemoryMB = freeJvmMemoryMB;
            this.totalJvmMemoryMB = totalJvmMemoryMB;
            this.usedPhysicalMemoryMB = usedPhysicalMemoryMB;
            this.freePhysicalMemoryMB = freePhysicalMemoryMB;
            this.totalPhysicalMemoryMB = totalPhysicalMemoryMB;
            this.usedDiskGB = usedDiskGB;
            this.freeDiskGB = freeDiskGB;
            this.totalDiskGB = totalDiskGB;
        }

        public String getOsName() { return osName; }
        public int getAvailableProcessors() { return availableProcessors; }
        public double getCpuUsagePercent() { return cpuUsagePercent; }

        public long getUsedJvmMemoryMB() { return usedJvmMemoryMB; }
        public long getFreeJvmMemoryMB() { return freeJvmMemoryMB; }
        public long getTotalJvmMemoryMB() { return totalJvmMemoryMB; }

        public long getUsedPhysicalMemoryMB() { return usedPhysicalMemoryMB; }
        public long getFreePhysicalMemoryMB() { return freePhysicalMemoryMB; }
        public long getTotalPhysicalMemoryMB() { return totalPhysicalMemoryMB; }

        public long getUsedDiskGB() { return usedDiskGB; }
        public long getFreeDiskGB() { return freeDiskGB; }
        public long getTotalDiskGB() { return totalDiskGB; }
    }


    public void stopAPI() {
        if (server != null) {
            server.stop(0);
            logger.log("Servidor detenido exitosamente.");
        }
    }
}
