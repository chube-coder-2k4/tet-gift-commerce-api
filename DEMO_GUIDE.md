# 🚀 Tài liệu Báo cáo & Demo Đồ án (Tết Gift Commerce API) 

Tài liệu này tổng hợp toàn bộ **Luồng hoạt động (Data Flow)** cốt lõi của hệ thống và bộ **10 Câu hỏi - Trả lời (Q&A)** mang tính chuyên môn cao nhằm đối đáp với hội đồng hoặc Giảng viên khi thuyết trình Demo.

---

## 🧭 PHẦN 1: CÁC LUỒNG NGHIỆP VỤ CHÍNH (ALL FLOWS)

### 1. Luồng Xác thực (Authentication & Authorization Flow)
- **Đăng ký / Đăng nhập thường:** User gửi thông tin (Email/Password) -> Spring Security xác thực qua `AuthenticationManager`. Nếu đúng, hệ thống trả về bộ token `Access Token` & `Refresh Token` (JWT).  
- **Google OAuth2:** Chuyển hướng sang màn hình đăng nhập Google -> Lấy Auth Code -> Spring Boot gọi API Google lấy Profile -> Cập nhật/Tạo User trong DB -> Trả về JWT.
- **Phân quyền Role:** Các API Admin được bảo vệ bởi Interceptor/Filter chặn request và kiểm tra quyền (`hasRole('ADMIN')`) được decode từ JWT payload.

### 2. Luồng Khám phá Sản phẩm & Custom Combo (Shopping Flow)
- **Duyệt hàng:** Truy xuất Catalog, Product, Bundle và tính toán giá đã giảm (Discount) trả về ở dạng DTO (dữ liệu truyền tải).
- **Custom Combo:** Khách hàng không chỉ mua giỏ quà đóng sẵn mà có thể **tự thiết kế giỏ quà** (Custom Bundle) nhờ ghép nhiều Product phân mảnh lại. Tính toán giá tiền và lưu Bundle tạm thời vào DB/Redis lúc thêm vào Session mua hàng.

### 3. Luồng Giỏ hàng & Tạo đơn (Cart & Order Flow)
- **Cart Management:** Dữ liệu Giỏ Hàng gắn kết với Session User / DB. Các thao tác Thêm, Sửa số lượng được tính tự động (auto calculate total price).
- **Checkout / Order Initialization:** 
  1. Kiểm tra kho hàng (Nếu có tính logic tồn kho). 
  2. Map dữ liệu `CartItem` sang bảng `OrderItem` (Sao lưu snapshot giá bán hiện tại nhằm tránh sai lệch nếu sau này giá sản phẩm thay đổi).
  3. Xóa thông tin đã đặt trong `Cart`.
  4. Trạng thái Đơn hàng chuyển sang `PENDING`. 

### 4. Luồng Thanh toán VNPay & Xuất Hóa đơn PDF (Payment & Invoice Processing)
- **Thanh toán (VNPay):** User chọn thanh toán -> Backend tạo URL chuyển hướng sang VNPay kèm tham số (TotalAmount, TxnRef) và được ký mã băm (HMAC SHA512) -> Khách điền OTP/Thẻ.
- **Callback (Xử lý Kết quả):** VNPay trả kết quả thông qua redirect + (IPN IP Notify). Backend kiểm tra lại chữ ký (Checksum/Signature). Nếu hợp lệ -> Đổi trạng thái Payment sang `PAID` và Order sang `COMPLETED`.
- **Hóa Đơn PDF:** Khi thanh toán xong, luồng System Invoice sẽ được gọi. `Thymeleaf Context` truyền dữ liệu Đơn hàng vào mã HTML Template, sau đó sử dụng thư viện `flying-saucer-pdf` render từ HTML Text stream ra `File PDF` và đính kèm gửi Mail xác nhận cho người dùng bằng thư viện `JavaMailSender`.

### 5. Luồng Trợ lý ảo AI RAG Chatbot (AI Consultant Flow)
Hệ thống sử dụng **RAG (Retrieval-Augmented Generation)** để AI có "não" nhớ thông tin thay vì "chém gió".
1. **User hỏi:** "Tư vấn giỏ quà cho bố mẹ giá 1 triệu."
2. **Text Embedding:** Câu hỏi được đưa vào ONNX-Model `all-MiniLM-L6-v2` để mã hóa thành dãy số học Vector 384-chiều.
3. **Vector Search:** Quét DB `PGVector` tìm các Vector văn bản (Mô tả sản phẩm, danh mục) có khoảng cách Cosine sát nhất với câu Vector gốc. 
4. **LLM Prompting:** Gom các sản phẩm liên quan lại thành Context đưa vào Prompt hệ thống.
5. **AI Modeling:** Google LLM (Gemini 3-4B-IT) phân tích Prompt + Context nội bộ và tạo ra đoạn văn bản phản hồi thông minh, chuyên nghiệp.

---

## 🎯 PHẦN 2: 10 CÂU HỎI BẢO VỆ CHUYÊN MÔN KHI DEMO

Dưới đây là 10 câu hỏi Giảng viên thường hay hỏi để test độ hiểu sâu của sinh viên và cách trả lời "ăn điểm tuyệt đối".

**Câu 1: Tại sao em lại sử dụng kiến trúc RAG (PGVector + Spring AI) thay vì gọi trực tiếp API của ChatGPT/Gemini?**
> **Trả lời:** Nếu em gọi trực tiếp LLM, AI sẽ bị "ảo giác" (Hallucination) hoặc trả lời chung chung vì nó không có dữ liệu thực tế về sản phẩm, giá cả và tồn kho của cửa hàng Tết mình. Việc build cơ chế RAG (Retrieval-Augmented Generation) và PGVector giúp em tiêm (inject) tri thức của giỏ hàng E-Commerce vào Prompt. Nhờ vậy chatbot mới tư vấn chính xác sản phẩm mình đang bán.

**Câu 2: PGVector định hướng tìm kiếm (Vector Search) hoạt động như thế nào trong dự án của em?**
> **Trả lời:** Em sử dụng Postgres extension là `pgvector`. Khi lưu tên và mô tả sản phẩm, em dùng Model Sentence Transformers chuyển chữ thành kiểu `Vector(384 chiều)`. Khi người dùng chat, hệ thống biến câu chat thành Vector, sau đó gọi thuật toán tìm kiếm khoảng cách Cosine Distance (Cosine Similarity) để quét DB lấy xả những sản phẩm có ngữ nghĩa tương đồng nhất.

**Câu 3: Làm sao em đảm bảo tính toàn vẹn (Integrity) khi có phát sinh lỗi lúc Đặt Hàng? (Ví dụ: Thêm sản phẩm vào giỏ thì ok nhưng bước tạo Invoice bị crash ngầm?)**
> **Trả lời:** Em dùng Data Transaction. Em bổ sung Annotation `@Transactional(rollbackFor = Exception.class)` ở class Service. Mọi thao tác lưu DB như (Tạo Order -> Tạo OrderItem -> Clear Cart) đều nằm trong 1 Transaction. Nếu bước cuối bị lỗi ném Exception, JPA/Hibernate sẽ tự động rollback huỷ bỏ toàn bộ các bước lưu DB trước đó. Không xảy ra tình trạng "Tạo đơn xong rồi mà giỏ hàng vẫn còn nguyên".

**Câu 4: Khi tính tiền đơn hàng trong hệ thống E-commerce, kiểu dữ liệu nào được sử dụng và tại sao em không dùng kiểu Float/Double?**
> **Trả lời:** Em sử dụng (hoặc nên sử dụng) `BigDecimal` trong thiết kế Entity liên quan đến số tiền tệ (Money/Currency). Các kiểu nguyên thuỷ như `Float/Double` biểu diễn số thực bằng dấu phẩy động (floating-point) dẫn tới việc sai số khi cộng trừ nhân chia liên tiếp. Giá trị tiền bạc bắt buộc phải chính xác tuyệt đối nên dùng BigDecimal là tiêu chuẩn của dự án Fintech/E-Commerce.

**Câu 5: Trong tính năng thanh toán, làm sao phòng trào việc Hacker giả mạo URL VNPay gửi lên server để báo thành công?**
> **Trả lời:** VNPay sử dụng cơ chế băm chữ ký (Checksum Hash Secret). Khi VNPay quay lại Web/API của bên em, URL query param chứa `vnp_SecureHash`. Server Backend dùng chuỗi Khóa bí mật (Secret Key) hash băm (HMAC SHA512) toàn bộ những parameter nhận được và so sánh với cái VNPay truyền sang. Nếu chữ ký không khớp, chứng tỏ dữ liệu thanh toán đã bị bên thứ 3 giả mạo tham số. 

**Câu 6: Em sử dụng Redis trong hệ thống nhằm mục đích gì? Bộ nhớ đệm mang lại lợi thế gì?**
> **Trả lời:** E-commerce thường có lượng Read (Đọc) cực kì nhiều so với Write (Ghi) như Load danh mục, sản phẩm ở Trang chủ. Em đưa các Data hiếm chỉnh sửa đó vào Redis Caching (Lưu ở dạng key-value RAM in-memory). Redis có tốc độ siêu trễ (sub-millisecond), vì vậy nó giảm tải connection đến Postgres DB, tăng tốc cực độ khả năng phản hồi API.

**Câu 7: Quá trình thiết kế logic chức năng "Custom Combo" diễn ra như thế nào?**
> **Trả lời:** "Custom Combo" khác Bundle cứng ở chỗ em cho phép ghép `N` Product lẻ lại, gom vào 1 thực thể. Backend tiếp nhận mảng các productId, validate logic số lượng, tính tổng phụ phí (hoặc chiết khấu tuỳ chỉnh), sau đó tạo 1 Entity Bundle tạm thời hoặc lưu Meta-data thẳng vào trường `CartItem`. Quan trọng nhất là Snapshot thông tin giá tại lúc tạo combo, tránh việc giá Product lên xuống làm nát bill cũ.

**Câu 8: Tại sao em lại dùng thư viện MapStruct để Map Object thay vì ModelMapper phổ biến?**
> **Trả lời:** ModelMapper dùng tính năng Reflection ở Runtime để nội soi cấu trúc Class và copy dữ liệu, làm ăn mòn CPU và suy giảm performance khi hệ thống nhiều Model/DTO. MapStruct sinh ra mã map Java gốc (Generate Bytecode) ở Compile-time, dùng Get/Set như code thủ công, nên chạy an toàn Type-Safe và tốc độ ngang với code tay, tốt cho E-commerce scale lớn.

**Câu 9: Cơ chế sinh và xuất hóa đơn File định dạng PDF diễn ra ở quy trình nào? Bằng cách nào?**
> **Trả lời:** Quy trình là `Backend Render` (Server-side rendering HTML). Khi đơn hoàn tất, em nạp dữ liệu (Item List, Total, Tax) vào file Template `Thymeleaf`. Thymeleaf trả ra Document HTML hoành chỉnh. Sau đó thư viện Java `flying-saucer-pdf` nạp file HTML này lại để parse CSS Inline thành Stream File `.pdf`. Cuối cùng lưu trữ vào Cloud hoặc Gửi đính kèm qua Java SMTP Mail.

**Câu 10: Nếu mô hình E-commerce cần truy cập rất đông (tải nặng) thì em sẽ thiết kế hướng cải tiến gì tiếp theo? (Roadmap System Design)**
> **Trả lời:** (Trả lời để hiện tham vọng thiết kế hệ thống tốt) - Hiện tại là kiến trúc Monolithic. Nếu scale lớn, em sẽ tách RAG AI Service và Payment Service ra thành Microservices riêng để cân bằng tải qua API Gateway (Spring Cloud). Ngoài ra thay vì chọc Postgres để tìm kiếm Full-Text sản phẩm bằng tiếng Việt (`LIKE`), em sẽ apply `Elasticsearch` engine cho thanh search và thay Redis làm Cart Storage hoàn toàn. 
