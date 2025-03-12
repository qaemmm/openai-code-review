package plus.gaga.middleware.sdk.infrastructrue.ollama;

public interface IOllama {

    String generateStreamRag(String model, String ragTag, String message);
}
