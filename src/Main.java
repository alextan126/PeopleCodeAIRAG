import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        String pdfFilePath = "NIGHTINGALE.pdf";
        int pagesPerChunk = 5; // Process 5 pages at a time

        // Chunk the PDF
        List<String> chunks = DocumentChunker.chunkPdfDocument(pdfFilePath, pagesPerChunk);

        // Initialize memory to store summaries
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20) // Retain the last 20 messages
                .build();

        // Initialize embedding store and model
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        OpenAiEmbeddingModel embeddingModel = EmbeddingProvider.createEmbeddingModel(apiKey);

        // Initialize RAG pipeline
        ContentInjector customInjector = new CustomContentInjectorLocal100();
        RAGPipeline pipeline = new RAGPipeline(chunks, 0.2,customInjector);
        ConversationalRetrievalChain ragChain = pipeline.createRagPipeline(apiKey, embeddingStore, embeddingModel);

        // Iterate through chunks
        for (String chunk : chunks) {
            String summary = ragChain.execute("Summarize this section.");
            System.out.println("Summary: " + summary);

            // Store summary in chat memory
            chatMemory.add(UserMessage.from("Summary of chunk:\n" + summary));
        }

        // After all chunks are processed, ask questions
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ask questions about the document (type 'exit' to quit):");

        while (true) {
            System.out.print("Your Question: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting. Goodbye!");
                break;
            }

            // Use RAG pipeline to answer based on memory
            String response = ragChain.execute(userInput);
            System.out.println("Response: " + response);
        }

        scanner.close();
    }
}
