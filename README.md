# Hướng dẫn Sử dụng và Cài đặt Remote Command Execution (RCE)

## Giới thiệu
**Remote Command Execution (RCE)** là một hệ thống ứng dụng Client-Server cho phép người dùng thực thi các lệnh hệ thống từ xa, giám sát hoạt động và giao tiếp qua mạng. Hệ thống được viết bằng ngôn ngữ Java và đã được đóng gói thành định dạng `.exe` để dễ dàng sử dụng trên Windows.

## Thành phần hệ thống
1.  **Server (`Server.exe`)**: Máy chủ trung tâm, chịu trách nhiệm quản lý kết nối, xác thực người dùng và điều phối lệnh.
2.  **Client (`Client.exe`)**: Ứng dụng phía người dùng. Tùy thuộc vào tài khoản đăng nhập, người dùng sẽ có quyền:
    -   **User thường**: Gửi yêu cầu thực thi lệnh, chat.
    -   **Admin**: Giám sát danh sách người dùng, xem nhật ký hoạt động (Logs), gửi lệnh quản trị.

---

## Yêu cầu hệ thống
-   **Hệ điều hành**: Windows 10/11.
-   **Java**: Cần cài đặt **Java Runtime Environment (JRE) phiên bản 8 trở lên**.
    -   Nếu chưa có, tải tại: [java.com/download](https://www.java.com/download)

---

## Hướng dẫn Cài đặt & Chạy ứng dụng

### 1. Chuẩn bị
Tải về và giải nén toàn bộ thư mục dự án. Đảm bảo cấu trúc thư mục được giữ nguyên như sau để ứng dụng hoạt động chính xác:

```text
Thư mục gốc/
├── Client.exe
├── Server.exe
├── client_config.xml
├── server_config.xml
├── Remote-Command-Execution/   <-- Chứa thư viện và mã nguồn Client
│   └── dist/
│       ├── Remote_Command_Execution.jar
│       └── lib/
└── Server/                     <-- Chứa thư viện và mã nguồn Server
    └── dist/
        ├── Server.jar
        └── lib/
```

> **Lưu ý quan trọng**: Không tách rời file `.exe` ra khỏi thư mục gốc vì nó cần truy cập các file thư viện trong thư mục `dist`.

### 2. Chạy Server
1.  Nhấn đúp vào `Server.exe`.
2.  Giao diện Server sẽ hiện lên.
3.  Nhấn nút **Start Server** để bắt đầu lắng nghe kết nối (mặc định Port 12345).

### 3. Chạy Client
1.  Nhấn đúp vào `Client.exe`.
2.  Tại màn hình đăng nhập:
    -   **Server**: Nhập địa chỉ IP của máy chạy Server (nhập `localhost` nếu chạy trên cùng máy).
    -   **Port**: `12345` (hoặc port bạn đã cấu hình trên Server).
    -   **Username/Password**: Nhập tài khoản đã đăng ký hoặc được cấp.
        -   *Tài khoản Admin (ví dụ)*: `admin2` / `test` (tùy thuộc vào dữ liệu có sẵn).
3.  Nhấn **Login**.

---

## Hướng dẫn Đóng gói lại (Packaging) bằng Launch4j

Nếu bạn muốn đóng gói lại dự án (ví dụ: khi di chuyển sang máy khác mà bị lỗi đường dẫn, hoặc sau khi cập nhật code), hãy làm theo các bước sau:

### Bước 1: Chuẩn bị Launch4j
-   Tải và cài đặt [Launch4j](http://launch4j.sourceforge.net/).

### Bước 2: Cấu hình Đường dẫn (Quan trọng)
Hiện tại, các file cấu hình `.xml` có thể đang sử dụng **đường dẫn tuyệt đối** (ví dụ: `E:\Lap trinh NET\...`). Để dự án có thể chạy trên mọi máy tính (Portable), bạn nên sửa lại cấu hình sử dụng **đường dẫn tương đối**.

1.  Mở `launch4j`.
2.  Nhấn **Open** (biểu tượng thư mục) và chọn file `server_config.xml` hoặc `client_config.xml`.

### Bước 3: Chỉnh sửa các thông số
Tại tab **Basic**:
-   **Output file**: Đặt tên file đầu ra (ví dụ: `Server.exe`).
-   **Jar**: Chọn đường dẫn đến file `.jar` tương ứng.
    -   Server: `Server\dist\Server.jar`
    -   Client: `Remote-Command-Execution\dist\Remote_Command_Execution.jar`
    -   *Mẹo*: Xóa hết phần đường dẫn ổ đĩa phía trước, chỉ để lại đường dẫn bắt đầu từ thư mục hiện tại để tạo đường dẫn tương đối.

Tại tab **Classpath** (nếu chọn chế độ "Don't wrap the jar"):
-   Kiểm tra mục **Classpath**. Đảm bảo các đường dẫn thư viện (`lib/...`) là chính xác.
-   Nên chuyển sang đường dẫn tương đối để tránh lỗi khi sang máy khác. Ví dụ thay vì `E:\Project\Server\dist\lib\mssql...jar`, hãy sửa thành `Server\dist\lib\mssql...jar`.

### Bước 4: Tạo file .exe
-   Nhấn nút **Build wrapper** (biểu tượng bánh răng) trên thanh công cụ.
-   Launch4j sẽ tạo ra file `.exe` mới ghi đè lên file cũ.

Lặp lại quy trình cho cả Server và Client.
