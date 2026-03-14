package com.listeners;
import com.database.DbConnection;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    // This method runs when the web application is STARTED
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("------------------------------------------");
        System.out.println("[VIBETHON] Web Application Starting...");
        System.out.println("------------------------------------------");
        
        // You can optionally initialize your DB pool here if you want it
        // to start exactly when the server starts.
    }

    // This method runs when the web application is STOPPED or RELOADED
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("------------------------------------------");
        System.out.println("[VIBETHON] Web Application Stopping...");
        
        // This stops the "HikariPool housekeeper" memory leak
        DbConnection.shutdown();
        
        System.out.println("[VIBETHON] Resources cleaned up successfully.");
        System.out.println("------------------------------------------");
    }
}