# Runna

Runna là ứng dụng theo dõi sức khoẻ trên nền tảng Android, được thiết kế chuyên biệt cho việc quản lý các hoạt động chạy bộ, ghi nhận chi tiết các buổi tập và hỗ trợ theo dõi tiến độ luyện tập mỗi ngày. Ứng dụng mang đến một giao diện trực quan giúp người dùng dễ dàng theo dõi quãng đường, thời gian, tốc độ (pace), lượng calo tiêu thụ, xem lại lịch sử các buổi chạy và tham gia các thử thách chạy bộ một cách dễ dàng, tất cả được tích hợp trong một ứng dụng duy nhất.

## Tính năng nổi bật

1. **Định vị & Theo dõi GPS trực tiếp**: Ghi nhận và hiển thị chính xác lộ trình của người chạy theo thời gian thực trên bản đồ tương tác thông qua hệ thống OpenStreetMap.
2. **Đa dạng chế độ tập luyện**: Hỗ trợ nhiều mục tiêu khác nhau như Chạy nhẹ nhàng (Easy Run), Chạy đường dài (Long Run), Chạy biến tốc (Interval), và Đi bộ (Walking).
3. **Tính toán thống kê chi tiết**: Ứng dụng tự động đo lường các chỉ số quan trọng gồm có quãng đường, thời gian chạy, pace trung bình và lượng calo đốt cháy. Hiển thị thông số tổng kết (Summary) đầy đủ sau mỗi lần chạy.
4. **Đồng bộ dữ liệu đám mây an toàn**: Mọi thông tin người dùng, lịch sử cá nhân đến dữ liệu các thử thách đều được tự động lưu trữ và đồng bộ hóa đám mây với Firebase Firestore.
5. **Lịch sử hoạt động**: Tính năng lưu trữ tự động toàn bộ lịch sử các bài tập đã hoàn thành, cho phép xem thống kê tổng quan trực tiếp trên màn hình Profile.
6. **Hệ thống thử thách (Challenges)**: Khám phá các thử thách chạy bộ công cộng, hoặc tiến hành tạo thử thách mới, tham gia thử thách nhanh chóng. App sẽ cập nhật tiến độ tự động ngay khi quá trình chạy kết thúc.
7. **Tài khoản cá nhân**: Hỗ trợ đăng ký và đăng nhập bảo mật qua Email/Mật khẩu. Dễ dàng đổi mật khẩu và quản lý hồ sơ người dùng tiện lợi.

## Công nghệ phát triển (Build With)

*   **[Kotlin](https://kotlinlang.org/)**: Ngôn ngữ lập trình chính được tối ưu hóa cho hệ điều hành Android.
*   **Android XML & UI**: Xây dựng layout với XML bám sát và chuẩn xác theo bản thiết kế giao diện cao cấp trên Figma.
*   **[OpenStreetMap (OSMDroid)](https://github.com/osmdroid/osmdroid)**: Thư viện nguồn mở thay thế tuyệt vời cho Google Maps, cung cấp khả năng hiển thị bản đồ mượt mà và vẽ cung đường người dùng chạy.
*   **[Figma](https://www.figma.com/)**: Đóng vai trò là công cụ sáng tạo UI/UX và thiết kế trải nghiệm người dùng hoàn chỉnh.
*   **[Firebase Authentication](https://firebase.google.com/docs/auth)**: Tích hợp hệ thống phân quyền và xác thực người dùng.
*   **[Cloud Firestore](https://firebase.google.com/docs/firestore)**: Đóng vai trò làm sơ sở dữ liệu trực tuyến, giúp truy xuất nhanh hồ sơ luyện tập và tiến trình tham gia các challenge của user.

## Các màn hình chính

*   Đăng nhập (Login) / Đăng ký (Register) / Hoàn tất hồ sơ (Complete Profile)
*   Trang chủ (Home)
*   Bắt đầu chạy (Start Running) / Theo dõi buổi chạy (Running Session) / Tổng kết buổi chạy (Summary)
*   Lịch sử theo dõi (Running History)
*   Thử thách (Challenges) / Chi tiết thử thách (Challenge Details) / Tạo thử thách mới (Create Challenge)
*   Cài đặt người dùng (Profile Settings)

## Hướng dẫn chạy dự án

1.  Mở dự án này bằng **Android Studio**.
2.  Sau khi clone code về, bảo đảm bạn đã tải xuống và lấy tệp `app/google-services.json` đặt vào bên trong thư mục `app` để kết nối vào Firebase.
3.  Vào console dự án Firebase và kích hoạt tính năng **Firebase Authentication** cùng dự án cơ sở dữ liệu **Cloud Firestore**.
4.  Chạm vào biểu tượng *Sync Project with Gradle Files* và đợi hệ thống tải toàn bộ các thư viện (dependencies) cần thiết.
5.  Thực hiện lệnh Build và chạy ứng dụng trực tiếp trên thiết bị giả lập (Android Emulator) hoặc thiết bị thật.

---

> **Ghi chú:** Dự án **Runna** chú trọng vào việc đem đến trải nghiệm ứng dụng chạy bộ thật mượt mà nhưng đầy đủ các chức năng thiết yếu. Ứng dụng đặc biệt sử dụng bản đồ mã nguồn mở OpenStreetMap cho quá trình định vị thay cho thẻ truyền thống. Về mảng thiết kế frontend, tất cả layout đều được implement chỉn chu theo nguyên tắc "Pixel Perfect" nhằm bám sát với thiết kế (UI) trên Figma.
