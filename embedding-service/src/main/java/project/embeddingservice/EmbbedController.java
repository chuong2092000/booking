package project.embeddingservice;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.embeddingservice.client.dto.uri.CommonUri;
import project.embeddingservice.entity.SupervisorResponse;

@RestController
@RequestMapping(CommonUri.VERSION + CommonUri.EMBEDDINGS)
@RequiredArgsConstructor
public class EmbbedController {


    private final ChatClient superVisorAI;

    private final VectorStore vectorStore;

    @GetMapping("/chat")
    public SupervisorResponse chat(@AuthenticationPrincipal String userId, @RequestParam String message) {

        return superVisorAI.prompt()
                .user(message)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, userId)
                        .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                ).call()
                .entity(SupervisorResponse.class);
    }


}
