import java.sql.Connection;
import javax.swing.SwingUtilities;
import server.db.dbConnect;
import server.ui.ServerDashboard;

public class Server {

    public static void main(String[] args) {

        // Kết nối DB nếu cần
        Connection conn = dbConnect.getConnect();
        if (conn == null) {
            System.out.println("❌ Không thể kết nối DB");
        } else {
            System.out.println("✔ Kết nối DB thành công");
        }

        // Mở giao diện admin server
        SwingUtilities.invokeLater(() -> {
            ServerDashboard dashboard = new ServerDashboard();
            dashboard.setVisible(true);
            dashboard.setLocationRelativeTo(null);
        });
    }
}
