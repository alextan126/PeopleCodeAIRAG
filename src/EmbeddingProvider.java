import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

public class EmbeddingProvider {

    public static OpenAiEmbeddingModel createEmbeddingModel(String apiKey) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .build();
    }
}