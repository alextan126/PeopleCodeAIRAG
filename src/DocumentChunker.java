import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentChunker {

    public static List<String> chunkString(String document, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = document.split("\n\n"); // Split by paragraphs or sections

        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (chunk.length() + paragraph.length() > chunkSize) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
            }
            chunk.append(paragraph).append("\n");
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }
        return chunks;
    }

    public static List<String> chunkPdfDocument(String pdfFilePath, int chunkSize) throws IOException {
        // Load the PDF document
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            // Extract text from the PDF
            PDFTextStripper textStripper = new PDFTextStripper();
            String extractedText = textStripper.getText(document);

            // Chunk the extracted text
            return chunkText(extractedText, chunkSize);
        }
    }

    private static List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n"); // Split by paragraphs or sections

        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (chunk.length() + paragraph.length() > chunkSize) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
            }
            chunk.append(paragraph).append("\n");
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }
        return chunks;
    }
}