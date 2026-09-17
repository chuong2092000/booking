package project.embeddingservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.embeddingservice.entity.ChatMessageEntity;
import project.embeddingservice.repo.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMemoryConfig implements ChatMemory {

    private final ChatMessageRepository repository;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessageEntity> entities = messages.stream().map(msg ->
                ChatMessageEntity.builder()
                        .conversationId(conversationId)
                        .roleType(msg.getMessageType().getValue())
                        .content(msg.getText())
                        .createdAt(LocalDateTime.now())
                        .build()
        ).collect(Collectors.toList());

        repository.saveAll(entities);
    }

    @Override
    public List<Message> get(String conversationId) {

        int lastN = 10;

        List<ChatMessageEntity> entities = repository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        int startIndex = Math.max(0, entities.size() - lastN);
        List<ChatMessageEntity> recentEntities = entities.subList(startIndex, entities.size());

        // Map Entity ngược lại thành Message của Spring AI
        return recentEntities.stream().map(entity -> {
            if ("USER".equalsIgnoreCase(entity.getRoleType())) {
                return new UserMessage(entity.getContent());
            } else {
                return new AssistantMessage(entity.getContent());
            }
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }
}
