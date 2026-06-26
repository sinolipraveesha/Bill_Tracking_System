package com.example.billtrackingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.time.LocalDate;

@Service
public class GeminiService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String scanReceipt(MultipartFile file) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return getMockResponse();
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            String prompt = "Extract the document number (as 'docNo'), date (as 'date' in YYYY-MM-DD), and line items (as 'products') from this receipt. " +
                            "For each product, extract 'ian' (barcode or code if available, else a random 6 digit string), 'name', 'quantity' (as a number), 'unit' (e.g. 'pcs', 'kg', 'ltr', 'pkt'), and 'price' (as a number). " +
                            "Output STRICTLY a valid JSON object matching this schema. Do not include markdown code blocks like ```json.";

            String requestBody = "{" +
                    "\"contents\": [{" +
                    "\"parts\": [" +
                    "{\"text\": \"" + prompt + "\"}," +
                    "{\"inline_data\": {" +
                    "\"mime_type\": \"" + mimeType + "\"," +
                    "\"data\": \"" + base64Image + "\"" +
                    "}}" +
                    "]" +
                    "}]," +
                    "\"generationConfig\": {" +
                    "\"response_mime_type\": \"application/json\"" +
                    "}" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Gemini API Error: " + response.body());
                return getMockResponse();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            
            if (!textNode.isMissingNode()) {
                String result = textNode.asText().trim();
                if (result.startsWith("```json")) {
                    result = result.substring(7);
                }
                if (result.endsWith("```")) {
                    result = result.substring(0, result.length() - 3);
                }
                return result.trim();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return getMockResponse();
    }

    private String getMockResponse() {
        return "{\n" +
                "  \"docNo\": \"MOCK-" + System.currentTimeMillis() % 10000 + "\",\n" +
                "  \"date\": \"" + LocalDate.now() + "\",\n" +
                "  \"products\": [\n" +
                "    {\n" +
                "      \"ian\": \"987654\",\n" +
                "      \"name\": \"Mock AI Extracted Product\",\n" +
                "      \"quantity\": 2,\n" +
                "      \"unit\": \"pcs\",\n" +
                "      \"price\": 12.99\n" +
                "    },\n" +
                "    {\n" +
                "      \"ian\": \"123999\",\n" +
                "      \"name\": \"Another Extracted Item\",\n" +
                "      \"quantity\": 1.5,\n" +
                "      \"unit\": \"kg\",\n" +
                "      \"price\": 4.50\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
