package net.creeperhost.sa;

import java.io.PrintStream;

/**
 * Created by covers1624 on 31/7/23.
 */
public class Logger {

    private static final String LOGGER_NAME = "SerializationAgent";
    public static PrintStream logger = System.out;

    public static void log(String level, String message) {
        logger.println("[" + LOGGER_NAME + "] " + "[" + level + "]: " + message);
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }
    public static void error(String message, Throwable ex) {
        error(message);
        ex.printStackTrace(logger);
    }

    public static void debug(String message) {
        if (SerializationAgent.DEBUG) {
            log("DEBUG", message);
        }
    }
}
