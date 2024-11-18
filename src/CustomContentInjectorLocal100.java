

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.Content;

import java.util.List;

public class CustomContentInjectorLocal100 implements ContentInjector {

    @Override
    public UserMessage inject(List<Content> contents, UserMessage chatMessage) {
        // Prepend retrieved content to the user's query
        String retrievedText = contents.stream()
                .map(Content::textSegment)
                .map(segment -> segment.text())
                .reduce("", (a, b) -> a + "\n" + b);

        String augmentedText = "You MUST answer the question based ONLY on the following context:\n"
                + retrievedText
                + "\n\nUser Query:\n" + chatMessage.text();
        return UserMessage.from(augmentedText);
    }
}