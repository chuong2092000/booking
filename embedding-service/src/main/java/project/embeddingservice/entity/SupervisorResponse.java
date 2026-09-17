package project.embeddingservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SupervisorResponse(
        @JsonPropertyDescription("Câu trả lời ngắn gọn để giao tiếp với người dùng. TUYỆT ĐỐI KHÔNG liệt kê danh sách, tên, hoặc chi tiết khách sạn ở đây vì giao diện (Frontend) sẽ tự hiển thị. Ví dụ: 'Dạ anh Chương, em đã tìm thấy các khách sạn phù hợp, anh tham khảo danh sách trên màn hình nhé!'")
        @JsonProperty("Response talk")
        String responseTalk,

        @JsonPropertyDescription("Dữ liệu thô (ví dụ: danh sách khách sạn, thông tin booking) mà các tools vừa trả về. Định dạng dưới dạng JSON object hoặc array để Frontend xử lý.")
        @JsonProperty("Response business")
        Object responseBusiness
) {
}
