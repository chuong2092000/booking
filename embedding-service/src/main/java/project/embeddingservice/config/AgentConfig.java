package project.embeddingservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.embeddingservice.service.HotelService;

@Configuration
public class AgentConfig {
    @Bean
    ChatClient superVisorAI(ChatClient.Builder builder, ChatMemory chatMemory, HotelService hotelService, VectorStore vectorStore) {
        String systemPrompt = "Bạn là AI Supervisor. Hãy tổng hợp thông tin từ các công cụ và trả lời người dùng.";
        return builder
                .defaultTools(hotelService)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
                )
                .build();
    }
}
