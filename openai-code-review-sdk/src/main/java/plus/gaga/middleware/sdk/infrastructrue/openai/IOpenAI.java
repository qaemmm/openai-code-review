package plus.gaga.middleware.sdk.infrastructrue.openai;

import plus.gaga.middleware.sdk.infrastructrue.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrastructrue.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO chatCompletionRequestDTO) throws Exception;
}
