package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    private String url = "jdbc:postgresql://localhost:5434/postgres";
    private String user = "postgres";
    private String passwd = "monSuperMotDePasse";
    private static Connection connect;

    private ConnectionDB() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
            connect = DriverManager.getConnection(url, user, passwd);
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getInstance() throws ClassNotFoundException {
        if (connect == null) {
            new ConnectionDB();
        }
        return connect;
    }
}
