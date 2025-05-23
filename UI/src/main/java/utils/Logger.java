package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance;

    private String logFilePath;
    private boolean consoleOutput;
    private boolean fileOutput;

    private Logger() {
        this.logFilePath = "application.log";
        this.consoleOutput = true;
        this.fileOutput = false;
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String formattedMessage = String.format("[%s] - %s", timestamp, message);

        if (consoleOutput) {
            System.out.println(formattedMessage);
        }

        if (fileOutput) {
            try (FileWriter fw = new FileWriter(logFilePath, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(formattedMessage);
            } catch (IOException e) {
                System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
            }
        }
    }

    public void log(String message, Throwable throwable) {
        log(message);
        log("Error: " + throwable.getMessage());

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        if (consoleOutput) {
            System.err.println(sb.toString());
        }
        if (fileOutput) {
            try (FileWriter fw = new FileWriter(logFilePath, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(sb.toString());
            } catch (IOException e) {
                System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
            }
        }
    }

    public void setLogFilePath(String path) {
        this.logFilePath = path;
    }

    public void setConsoleOutput(boolean enabled) {
        this.consoleOutput = enabled;
    }

    public void setFileOutput(boolean enabled) {
        this.fileOutput = enabled;
    }
}