package project.embeddingservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.embeddingservice.entity.ChatMessageEntity;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    void deleteByConversationId(String conversationId);
}
