package zaman.dongnaemoa.global.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import zaman.dongnaemoa.global.ai.GroqQuestAnalyzer.QuestAnalysisResult;

class GroqQuestAnalyzerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void analyze_validResponse_parsesResult() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.groq.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        String groqResponse = """
                {"choices":[{"message":{"content":
                "{\\"minutes\\":6,\\"rewardPoint\\":30,\\"difficulty\\":\\"NORMAL\\",\\"checkpoints\\":[\\"확인1\\",\\"확인2\\"]}"
                }}]}""";
        server.expect(method(HttpMethod.POST))
                .andRespond(withSuccess(groqResponse, MediaType.APPLICATION_JSON));

        GroqQuestAnalyzer analyzer = new GroqQuestAnalyzer(builder, objectMapper, "test-key");

        QuestAnalysisResult result = analyzer.analyze("제목", "설명");

        assertThat(result.minutes()).isEqualTo(6);
        assertThat(result.rewardPoint()).isEqualTo(30);
        assertThat(result.difficulty()).isEqualTo("NORMAL");
        assertThat(result.checkpoints()).containsExactly("확인1", "확인2");
    }

    @Test
    void analyze_serverError_returnsFallback() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.groq.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST))
                .andRespond(withServerError());

        GroqQuestAnalyzer analyzer = new GroqQuestAnalyzer(builder, objectMapper, "test-key");

        QuestAnalysisResult result = analyzer.analyze("제목", "설명");

        assertThat(result).isEqualTo(QuestAnalysisResult.fallback());
    }

    @Test
    void analyze_blankApiKey_returnsFallbackWithoutCallingApi() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.groq.com");
        GroqQuestAnalyzer analyzer = new GroqQuestAnalyzer(builder, objectMapper, "");

        QuestAnalysisResult result = analyzer.analyze("제목", "설명");

        assertThat(result).isEqualTo(QuestAnalysisResult.fallback());
    }
}
