//package project.embeddingservice.ass;
//
//import dev.langchain4j.service.SystemMessage;
//import dev.langchain4j.service.UserMessage;
//import dev.langchain4j.service.spring.AiService;
//import project.embeddingservice.UserInfor;
//
//@AiService
//public interface TestAssistant {
//    @SystemMessage("""
//            Quy tắc phản hồi ĐẶC BIỆT QUAN TRỌNG:
//            Bạn KHÔNG được phép trả về văn bản tự do. Bạn CHỈ ĐƯỢC PHÉP trả về kết quả dưới định dạng JSON duy nhất như cấu trúc sau đây để hệ thống có thể đọc được tự động:
//            {
//              "value": "Thông tin phản hồi"
//            }
//            """)
//    UserInfor test(@UserMessage String userMessage);
//}
