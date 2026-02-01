package com.example;

// import org.apache.log4j.Category; // 1.1.3 Category import
import org.apache.log4j.Logger; // 1.2.17 Logger import

public class App {
    // private static Category logger = Category.getInstance(App.class);
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Hello, this is a sample application using Log4j.");
    }
}
