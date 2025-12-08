package server.db;

import server.model.CommandHistory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandHistoryDAO {

    // Lưu lịch sử lệnh
    public void saveHistory(int userId, String serverIp, String command, String result, String clientIp) {

        String sql = """
            INSERT INTO CommandHistory(user_id, server_ip, command, result, client_ip)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConnect.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, serverIp);
            ps.setString(3, command);
            ps.setString(4, result);
            ps.setString(5, clientIp);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lấy toàn bộ lịch sử (cho Admin)
    public List<CommandHistory> getAll() {
        List<CommandHistory> list = new ArrayList<>();

        String sql = """
            SELECT h.history_id, h.user_id, u.username, h.server_ip, h.command,
                   h.result, h.error_message, h.client_ip
            FROM CommandHistory h
            JOIN Users u ON u.user_id = h.user_id
            ORDER BY h.history_id DESC
        """;

        try (Connection conn = dbConnect.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CommandHistory(
                        rs.getInt("history_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("server_ip"),
                        rs.getString("command"),
                        rs.getString("result"),
                        rs.getString("error_message"),
                        rs.getString("client_ip")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ⭐ Lấy lịch sử theo user_id (cho User)
    public List<CommandHistory> getByUserId(int userId) {
        List<CommandHistory> list = new ArrayList<>();

        String sql = """
            SELECT h.history_id, h.user_id, u.username, h.server_ip, h.command,
                   h.result, h.error_message, h.client_ip
            FROM CommandHistory h
            JOIN Users u ON u.user_id = h.user_id
            WHERE h.user_id = ?
            ORDER BY h.history_id DESC
        """;

        try (Connection conn = dbConnect.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CommandHistory(
                            rs.getInt("history_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("server_ip"),
                            rs.getString("command"),
                            rs.getString("result"),
                            rs.getString("error_message"),
                            rs.getString("client_ip")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<CommandHistory> searchByUserIdAndKeyword(int userId, String keyword) {
        List<CommandHistory> list = new ArrayList<>();
        String sql = """
            SELECT h.history_id, h.user_id, u.username, h.server_ip, h.command,
                   h.result, h.error_message, h.client_ip
            FROM CommandHistory h
            JOIN Users u ON u.user_id = h.user_id
            WHERE h.user_id = ?
              AND (h.command LIKE ? OR h.result LIKE ?)
            ORDER BY h.history_id DESC
        """;

        try (Connection conn = dbConnect.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String keywordLike = "%" + keyword + "%";
            ps.setInt(1, userId);
            ps.setString(2, keywordLike);
            ps.setString(3, keywordLike);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CommandHistory(
                            rs.getInt("history_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("server_ip"),
                            rs.getString("command"),
                            rs.getString("result"),
                            rs.getString("error_message"),
                            rs.getString("client_ip")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean deleteById(int historyId, int requesterId, boolean isAdmin) {
        String sql = isAdmin
                ? "DELETE FROM CommandHistory WHERE history_id = ?"
                : "DELETE FROM CommandHistory WHERE history_id = ? AND user_id = ?";

        try (Connection conn = dbConnect.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, historyId);
            if (!isAdmin) {
                ps.setInt(2, requesterId);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
