IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'RemoteControlDB')
BEGIN
    CREATE DATABASE RemoteControlDB;
END
GO

USE RemoteControlDB;
GO

-- DROP theo thứ tự: Bảng con trước, bảng cha sau
IF OBJECT_ID('dbo.CommandHistory', 'U') IS NOT NULL DROP TABLE dbo.CommandHistory;
IF OBJECT_ID('dbo.ServerConfig', 'U') IS NOT NULL DROP TABLE dbo.ServerConfig;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(500) NOT NULL,
    full_name NVARCHAR(100),
    email NVARCHAR(100),
    phone NVARCHAR(20),
    is_admin BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT CHK_Users_Username_Length CHECK (LEN(username) >= 3)
);

CREATE INDEX idx_Users_username ON Users(username);
CREATE INDEX idx_Users_email ON Users(email);
GO


CREATE TABLE CommandHistory (
    history_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    server_id INT,
    server_ip NVARCHAR(50) NOT NULL,
    command NVARCHAR(MAX) NOT NULL,
    result NVARCHAR(MAX),
    error_message NVARCHAR(MAX),
    client_ip NVARCHAR(50),
    CONSTRAINT FK_CommandHistory_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_CommandHistory_server_id 
        FOREIGN KEY (server_id) 
        REFERENCES ServerConfig(server_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE INDEX idx_CommandHistory_user_id ON CommandHistory(user_id);
CREATE INDEX idx_CommandHistory_server_id ON CommandHistory(server_id);
CREATE INDEX idx_CommandHistory_server_ip ON CommandHistory(server_ip);
GO

USE RemoteControlDB;
GO

-- Thêm người dùng quản trị
INSERT INTO Users (username, password_hash, full_name, email, phone, is_admin)
VALUES 
(N'admin', CONVERT(NVARCHAR(500), HASHBYTES('SHA2_256', 'admin123'), 2), N'Quản trị viên', N'admin@remote.com', N'0909123456', 1),

GO

INSERT INTO CommandHistory (user_id, server_id, server_ip, command, result, client_ip)
VALUES
(1, 1, N'192.168.1.10', N'dir', N'Danh sách thư mục: Documents, Downloads, Desktop', N'192.168.1.5'),
(2, 3, N'127.0.0.1', N'ipconfig', N'IPv4 Address: 127.0.0.1', N'192.168.1.22'),
(3, 2, N'192.168.1.11', N'ping google.com', N'Kết quả: 4 gói tin thành công', N'10.0.0.15');
GO

-- Bảng chứa các lệnh được phép thực thi
IF OBJECT_ID('dbo.AllowedCommands', 'U') IS NOT NULL DROP TABLE dbo.AllowedCommands;
GO

CREATE TABLE AllowedCommands (
    cmd_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL, -- người tạo (admin)
    command_text NVARCHAR(200) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    is_active BIT DEFAULT 1,

    CONSTRAINT FK_AllowedCommands_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT UQ_AllowedCommands_Command UNIQUE (command_text)
);
GO

CREATE INDEX idx_AllowedCommands_UserId ON AllowedCommands(user_id);
GO
-- Bổ sung nhiều lệnh an toàn cho Remote Execution
INSERT INTO AllowedCommands (user_id, command_text) VALUES
(1, 'ipconfig'),
(1, 'ipconfig /all'),
(1, 'ping'),
(1, 'ping google.com'),
(1, 'tracert'),
(1, 'netstat'),
(1, 'netstat -ano'),
(1, 'hostname'),
(1, 'whoami'),
(1, 'systeminfo'),
(1, 'tasklist'),
(1, 'tasklist /v'),
(1, 'tasklist /fi "status eq running"'),
(1, 'wmic cpu get name'),
(1, 'wmic memorychip get capacity'),
(1, 'wmic logicaldisk get size,freespace,caption'),
(1, 'dir'),
(1, 'dir /a'),
(1, 'dir /b'),
(1, 'dir /s'),
(1, 'cd'),
(1, 'type'),
(1, 'echo'),
(1, 'ver'),
(1, 'path'),
(1, 'chdir'),
(1, 'cls'),
(1, 'fsutil fsinfo drives'),
(1, 'query user'),
(1, 'net user'),
(1, 'net localgroup'),
(1, 'net localgroup administrators'),
(1, 'getmac'),
(1, 'arp -a'),
(1, 'route print'),
(1, 'powershell Get-Process'),
(1, 'powershell Get-Service'),
(1, 'powershell Get-NetIPConfiguration'),
(1, 'powershell Get-HotFix'),
(1, 'powershell Get-EventLog -LogName Application -Newest 20');
GO
