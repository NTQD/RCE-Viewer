package server.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnect {

    private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;"
            + "databaseName=RemoteControlDB;"
            + "encrypt=true;trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASS = "12";

    public static Connection getConnect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println(">> Kết nối DB thành công");
            return conn;
        } catch (Exception e) {
            System.out.println(">> Lỗi kết nối DB!");
            e.printStackTrace();
            return null;
        }
    }
}
