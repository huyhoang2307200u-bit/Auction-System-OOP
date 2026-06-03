# Auction Management System (Hệ thống Quản lý Đấu giá trực tuyến)

Hệ thống quản lý và tham gia đấu giá trực tuyến được phát triển trên nền tảng Java, áp dụng chặt chẽ các nguyên lý lập trình hướng đối tượng (OOP) và các Design Patterns phổ biến. Hệ thống hoạt động theo mô hình Client-Server nhằm đảm bảo tính đồng bộ dữ liệu thời gian thực cho các phiên đấu giá.

---

## 1. Mô tả bài toán & Phạm vi hệ thống

### Bài toán
Trong các phiên đấu giá truyền thống, việc cập nhật giá và thông báo trạng thái gặp nhiều hạn chế về không gian và thời gian. Hệ thống đấu giá trực tuyến này giải quyết bài toán kết nối giữa **Seller (Người bán)** và **Bidder (Người mua/Người đấu giá)** thông qua một máy chủ trung tâm (**Server**).

### Phạm vi hệ thống
* **Quản lý người dùng:** Đăng ký, đăng nhập, phân quyền (Bidder, Seller, Admin).
* **Quản lý ví tài chính:** Nạp tiền, trừ tiền khi đấu giá thành công, hoàn tiền khi có người trả giá cao hơn.
* **Quản lý vật phẩm đấu giá:** Phân loại sản phẩm (Đồ điện tử, Xe cộ, Tranh ảnh/Nghệ thuật), thiết lập giá khởi điểm, bước giá.
* **Phiên đấu giá thời gian thực:** Cho phép nhiều Bidder cùng tham gia đặt giá vào một phòng đấu giá, hỗ trợ tính năng tự động đấu giá (Auto Bid).
* **Hệ thống thông báo:** Gửi thông báo thời gian thực khi có người trả giá mới, khi phiên đấu giá kết thúc hoặc khi nạp tiền thành công.

---

## 2. Công nghệ sử dụng & Yêu cầu môi trường

### Công nghệ sử dụng
* **Ngôn ngữ cốt lõi:** Java (JDK 17 trở lên)
* **Giao diện người dùng (Client UI):** JavaFX v17+ (kèm FXML cho cấu trúc giao diện)
* **Quản lý mã nguồn & Thư viện:** Maven (được cấu hình qua `pom.xml`)
* **Cơ sở dữ liệu:** MySQL (Hệ quản trị cơ sở dữ liệu quan hệ, kết nối cổng 3306)
* **Giao thức mạng:** Java Socket (TCP) phục vụ truyền tải dữ liệu dạng Request/Response qua các DTO Custom công khai.

### Yêu cầu cài đặt môi trường
* **Java Development Kit (JDK):** Phiên bản 17 hoặc cao hơn.
* **Apache Maven:** Phiên bản 3.8+ (đã được tích hợp sẵn trong IntelliJ IDEA).
* **MySQL Server:** Đã được cài đặt và khởi chạy (qua XAMPP, Docker hoặc MySQL Workbench).

---

## 3. Cấu trúc thư mục dự án

Dự án được tổ chức theo mô hình phân lớp rõ ràng nhằm tách biệt trách nhiệm:

```text
Auction-System-OOP/
├── db/
│   └── schema.sql                # File cấu hình câu lệnh khởi tạo các bảng trong MySQL
├── src/
│   ├── main/
│   │   ├── java/com/auction/
│   │   │   ├── client/           # Xử lý kết nối Socket phía Client
│   │   │   ├── common/           # Các lớp DTO, Request, Response dùng chung
│   │   │   ├── controller/       # Bộ điều khiển giao diện JavaFX (Client)
│   │   │   ├── dto/              # Data Transfer Objects phục vụ đóng gói dữ liệu
│   │   │   ├── exception/        # Các Exception tùy chỉnh (Auth, InvalidBid,...)
│   │   │   ├── model/            # Các lớp thực thể (User, Item, Auction,...) áp dụng OOP
│   │   │   ├── server/           # Xử lý Logic phía Server (SocketServer, ClientHandler)
│   │   │   │   ├── controller/   # Điều hướng request tại Server
│   │   │   │   ├── dao/          # Tương tác trực tiếp với Database (Data Access Object)
│   │   │   │   └── service/      # Nghiệp vụ logic chính tại Server (Xử lý tiền, ví, đấu giá)
│   │   │   ├── service/          # Các dịch vụ hỗ trợ phía Client (Quản lý chuyển Scene, Observers)
│   │   │   └── util/             # Các công cụ bổ trợ (Tạo ID, Định dạng tiền, Kết nối DB)
│   │   └── resources/
│   │       ├── fxml/             # File thiết kế giao diện (Login, Register, AuctionList,...)
│   │       └── images/           # Tài nguyên hình ảnh, Logo dự án
│   └── test/                     # Các kịch bản Unit Test (JUnit) cho các dịch vụ cốt lõi
└── pom.xml                       # File cấu hình dependencies và build của Maven

4. Hướng dẫn chạy chương trình
Dự án hỗ trợ chạy thông qua dòng lệnh trên mọi hệ điều hành (Windows, Linux, macOS). Mở Terminal tại thư mục gốc của dự án và chạy theo thứ tự cụ thể sau:

Bước 1: Biên dịch và đóng gói dự án bằng Maven
Chạy lệnh sau để tải các thư viện cần thiết và biên dịch mã nguồn:

-mvn clean package -DskipTests

Bước 2: Khởi chạy ứng dụng Server
Server cần được chạy trước để mở cổng Socket lắng nghe kết nối từ Client.
-mvn exec:java -Dexec.mainClass="com.auction.server.ServerMain"

Bước 3: Khởi chạy ứng dụng Client
Mở một cửa sổ Terminal mới (giữ nguyên Terminal Server đang chạy) và thực hiện lệnh:
-mvn exec:java -Dexec.mainClass="com.auction.AppLauncher"

(Có thể mở nhiều Terminal Client khác nhau để giả lập nhiều người dùng cùng tham gia đấu giá).

5. Danh sách chức năng đã hoàn thành
-Hệ thống bảo mật & Tài khoản
[x] Đăng ký tài khoản mới (Xác thực trùng lặp Username).

[x] Đăng nhập hệ thống (Mã hóa/Kiểm tra quyền hạn).

[x] Quản lý thông tin số dư Ví (Nạp/Rút/Khấu trừ tự động).

-Chức năng Đấu giá (Core Logic)
[x] Seller: Tạo phòng đấu giá mới, thiết lập sản phẩm bằng ItemFactory.

[x] Bidder: Xem danh sách phiên đấu giá đang diễn ra (Trạng thái Active).

[x] Bidder: Đặt giá (Place Bid) trực tiếp (Hệ thống tự động kiểm tra bước giá và số dư ví).

[x] Auto Bid: Cấu hình tự động trả giá dựa trên ngân sách tối đa của người dùng.

-Hệ thống cốt lõi & Đồng bộ
[x] Kiến trúc TCP Socket đa luồng (ClientHandler), xử lý bất đồng bộ nhiều kết nối cùng lúc.

[x] Áp dụng Observer Pattern để cập nhật trạng thái phòng đấu giá và số dư ví theo thời gian thực.

[x] Lưu trữ dữ liệu an toàn, nhất quán qua SQLite.

6. Báo cáo & Video Demo
Link tải báo cáo chi tiết (PDF): https://drive.google.com/drive/folders/1WNPnGSOpPlVwuXZs97URbGevLfFjAIKd?usp=drive_link

Video Demo các chức năng hệ thống: https://drive.google.com/drive/folders/1WNPnGSOpPlVwuXZs97URbGevLfFjAIKd?usp=drive_link

