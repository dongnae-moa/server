package zaman.dongnaemoa.global.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqQuestAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GroqQuestAnalyzer.class);

    private static final String MODEL = "llama-3.1-8b-instant";
    private static final String SYSTEM_PROMPT =
            "퀘스트 제목과 설명을 보고 아래 JSON 형식으로만 답해. 다른 말은 하지 마.\n"
                    + "{\"minutes\":정수,\"rewardPoint\":정수(10~200),\"difficulty\":\"EASY\"|\"NORMAL\"|\"HARD\","
                    + "\"checkpoints\":[\"문자열\",\"문자열\"]}\n"
                    + "checkpoints는 2~4개, 각 15자 이내의 한국어 확인 항목.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    @Autowired
    public GroqQuestAnalyzer(ObjectMapper objectMapper, @Value("${groq.api-key:}") String apiKey) {
        this(RestClient.builder().baseUrl("https://api.groq.com").requestFactory(defaultRequestFactory()),
                objectMapper, apiKey);
    }

    GroqQuestAnalyzer(RestClient.Builder restClientBuilder, ObjectMapper objectMapper, String apiKey) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.restClient = restClientBuilder.build();
    }

    private static JdkClientHttpRequestFactory defaultRequestFactory() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return requestFactory;
    }

    public QuestAnalysisResult analyze(String title, String description) {
        if (apiKey.isBlank()) {
            return QuestAnalysisResult.fallback();
        }
        try {
            String userMessage = "제목: " + title + "\n설명: " + (description == null ? "" : description);
            String responseBody = restClient.post()
                    .uri("/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(new ChatCompletionRequest(MODEL,
                            List.of(new ChatMessage("system", SYSTEM_PROMPT), new ChatMessage("user", userMessage)),
                            new ResponseFormat("json_object")))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return objectMapper.readValue(content, QuestAnalysisResult.class);
        } catch (Exception e) {
            log.warn("Groq 퀘스트 분석 실패, 기본값으로 대체합니다.", e);
            return QuestAnalysisResult.fallback();
        }
    }

    private record ChatCompletionRequest(
            String model, List<ChatMessage> messages, @JsonProperty("response_format") ResponseFormat responseFormat) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record ResponseFormat(String type) {
    }

    public record QuestAnalysisResult(Integer minutes, Integer rewardPoint, String difficulty, List<String> checkpoints) {
        public static QuestAnalysisResult fallback() {
            return new QuestAnalysisResult(10, 50, "NORMAL", List.of());
        }
    }
}
