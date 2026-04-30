# Product Specification: ReelsGo (High-Fidelity Clone)

Ứng dụng xem phim ngắn kịch tính (Drama Shorts) được tùy chỉnh theo thiết kế cao cấp từ video demo.

## Core Features (Final)

1. **Discovery Screen (Home):**
   - **Banner Carousel:** Hiển thị các phim tiêu biểu với dot indicator.
   - **Trending Section:** Danh sách phim dạng cuộn ngang (Horizontal) với poster sắc nét.
   - **New Releases:** Danh sách phim dạng dọc, mỗi item có nút "Watch Drama" màu đỏ đặc trưng.

2. **Vertical Video Feed (Feed Tab):**
   - **Interactive UI:** Giao diện xem phim toàn màn hình với nút Favorite (Tim), Series (Danh sách tập), và Speed Control (0.5x - 3x).
   - **Progress Control:** Seekbar tùy chỉnh hiển thị thời gian thực (mm:ss).

3. **Episodes Navigation:**
   - Bottom Sheet chứa danh sách tập phim với phân trang (Range tabs: 1-20, 21-40, v.v.).
   - Tích hợp quảng cáo Banner ngay trong Bottom Sheet.

4. **History & Library:**
   - Giao diện tab (History/Favorites) giúp quản lý danh sách phim đã xem và phim yêu thích.

## Screen Flow (Implemented)
`Splash -> Home (Discovery) -> Feed (Video Player) -> History -> Me (Profile)`

## UI Aesthetics
- **Theme:** Ultra Dark Mode (#121212).
- **Highlights:** Neon Green (#00E676) cho indicators, Vibrant Red (#FF4444) cho nút hành động.
- **Typography:** Bold hierarchy cho drama titles.
