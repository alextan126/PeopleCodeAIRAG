import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.List;


public class RAGPipeline {

    List<String> chunks;
    double temperature;
    ContentInjector contentInjector;

    public RAGPipeline(List<String> chunks, double temperature, ContentInjector contentInjector){
        this.chunks = chunks;
        this.temperature = temperature;
        this.contentInjector = contentInjector;
    }

    public ConversationalRetrievalChain createRagPipeline(
            String apiKey,
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        // Add chunks to the embedding store
        for (String str : chunks) {
            if (str == null || str.trim().isEmpty()) {
                // Skip null or empty chunks
                continue;
            }
            TextSegment textSegment = TextSegment.from(str.trim());
            Embedding embedding = embeddingModel.embed(textSegment).content();
            embeddingStore.add(embedding, textSegment);
        }

        // Create a ContentRetriever
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();

        // Create a RetrievalAugmentor
        var retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentInjector(contentInjector)
                .build();

        // Choose memory implementation
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(5) // Retain the last 5 messages
                .build();

        // Alternatively, use TokenWindowChatMemory
        //        .maxTokens(500) // Retain up to 500 tokens
        //        .build();

        // Create the Chat Model
        var chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .temperature(temperature)
                .maxTokens(750)
                .build();

        // Build the pipeline with memory
        return ConversationalRetrievalChain.builder()
                .retrievalAugmentor(retrievalAugmentor)
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory) // Add memory here
                .build();
    }

}
