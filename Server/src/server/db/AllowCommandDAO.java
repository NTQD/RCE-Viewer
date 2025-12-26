package server.db;

import server.model.AllowCommand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllowCommandDAO {

    private Connection getConnection() throws SQLException {
        return dbConnect.getConnect();
    }

    // ======================================================
    // LẤY TẤT CẢ LỆNH ĐƯỢC PHÉP
    // ======================================================
    public List<AllowCommand> getAll() {
        List<AllowCommand> list = new ArrayList<>();
        String sql = "SELECT * FROM AllowedCommands ORDER BY cmd_id DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AllowCommand ac = new AllowCommand();
                ac.setCmd_id(rs.getInt("cmd_id"));
                ac.setUser_id(rs.getInt("user_id"));
                ac.setCommand_text(rs.getString("command_text"));
                ac.setCreate_at(rs.getTimestamp("created_at"));
                ac.setIs_active(rs.getBoolean("is_active"));
                list.add(ac);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ======================================================
    // LẤY MỘT LỆNH THEO ID
    // ======================================================
    public AllowCommand getById(int cmdId) {
        String sql = "SELECT * FROM AllowedCommands WHERE cmd_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cmdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AllowCommand ac = new AllowCommand();
                    ac.setCmd_id(rs.getInt("cmd_id"));
                    ac.setUser_id(rs.getInt("user_id"));
                    ac.setCommand_text(rs.getString("command_text"));
                    ac.setCreate_at(rs.getTimestamp("created_at"));
                    ac.setIs_active(rs.getBoolean("is_active"));
                    return ac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Not found or error
    }

    // ======================================================
    // LẤY THEO USER (USER CHỈ THẤY LỆNH CỦA RIÊNG MÌNH)
    // ======================================================
    public List<AllowCommand> getByUserId(int userId) {
        List<AllowCommand> list = new ArrayList<>();
        String sql = "SELECT * FROM AllowedCommands WHERE user_id = ? AND is_active = 1";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AllowCommand ac = new AllowCommand();
                ac.setCmd_id(rs.getInt("cmd_id"));
                ac.setUser_id(rs.getInt("user_id"));
                ac.setCommand_text(rs.getString("command_text"));
                ac.setCreate_at(rs.getTimestamp("created_at"));
                ac.setIs_active(rs.getBoolean("is_active"));
                list.add(ac);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ======================================================
    // THÊM LỆNH MỚI (CHỈ ADMIN)
    // ======================================================
    public boolean add(AllowCommand ac, int adminId) {

        // Check for duplicate command
        String checkSql = "SELECT COUNT(*) FROM AllowedCommands WHERE command_text = ?";
        try (Connection conn = getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, ac.getCommand_text());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Duplicate found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO AllowedCommands (user_id, command_text, is_active, created_at) VALUES (?, ?, ?, GETDATE())";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ac.getUser_id());
            ps.setString(2, ac.getCommand_text());
            ps.setBoolean(3, ac.getIs_active());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ======================================================
    // XOÁ LỆNH
    // ======================================================
    public boolean delete(int cmdId, int adminId) {
        String sql = "DELETE FROM AllowedCommands WHERE cmd_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cmdId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ======================================================
    // UPDATE LỆNH
    // ======================================================
    public boolean update(AllowCommand ac, int adminId) {
        String sql = "UPDATE AllowedCommands SET command_text = ?, is_active = ? WHERE cmd_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ac.getCommand_text());
            ps.setBoolean(2, ac.getIs_active());
            ps.setInt(3, ac.getCmd_id());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ======================================================
    // KIỂM TRA LỆNH CÓ ĐƯỢC PHÉP CHẠY KHÔNG
    // ======================================================
    public List<AllowCommand> getAllActiveCommands() {
        List<AllowCommand> list = new ArrayList<>();
        String sql = "SELECT * FROM AllowedCommands WHERE is_active = 1";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AllowCommand ac = new AllowCommand();
                ac.setCmd_id(rs.getInt("cmd_id"));
                ac.setUser_id(rs.getInt("user_id"));
                ac.setCommand_text(rs.getString("command_text"));
                ac.setCreate_at(rs.getTimestamp("created_at"));
                ac.setIs_active(rs.getBoolean("is_active"));
                list.add(ac);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean isCommandAllowed(String inputCommand, boolean isAdmin) {

        if (isAdmin)
            return true;

        inputCommand = inputCommand.trim().toLowerCase();

        List<AllowCommand> list = getAllActiveCommands();

        for (AllowCommand ac : list) {
            if (ac.getCommand_text().trim().toLowerCase().equals(inputCommand)) {
                return true;
            }
        }

        return false;
    }
}
