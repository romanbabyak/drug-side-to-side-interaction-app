package com;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * method to establish connection to the database
 */
public class DatabaseConnection {
    /**
     * Establishes a connection to the database using the specified params
     *
     * @return a Connection object if successful, or null if the connection fails
     */
    public static Connection connect(String url, String user, String password) {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

