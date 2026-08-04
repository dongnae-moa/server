package zaman.dongnaemoa.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import zaman.dongnaemoa.domain.user.dto.LoginRequest;
import zaman.dongnaemoa.domain.user.dto.SignUpRequest;
import zaman.dongnaemoa.support.PostgresTestContainerSupport;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerTest extends PostgresTestContainerSupport {

    @Autowired
    private TestRestTemplate restTemplate;

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@test.com";
    }

    private String uniqueNickname() {
        return "nick" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("유효한 요청으로 회원가입하면 201과 CommonApiResponse 포맷을 반환한다")
    void signUp_validRequest_returns201() {
        SignUpRequest request = new SignUpRequest(uniqueEmail(), "password123", uniqueNickname());

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/auth/signup", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("status", "CREATED");
        assertThat(response.getBody()).containsEntry("code", 201);
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
    void signUp_invalidEmail_returns400() {
        SignUpRequest request = new SignUpRequest("not-an-email", "password123", uniqueNickname());

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/auth/signup", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("비밀번호가 너무 짧으면 400을 반환한다")
    void signUp_shortPassword_returns400() {
        SignUpRequest request = new SignUpRequest(uniqueEmail(), "short", uniqueNickname());

        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/auth/signup", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("회원가입 후 올바른 자격증명으로 로그인하면 200과 토큰을 반환한다")
    void login_afterSignUp_returns200WithToken() {
        String email = uniqueEmail();
        SignUpRequest signUpRequest = new SignUpRequest(email, "password123", uniqueNickname());
        restTemplate.postForEntity("/v1/auth/signup", signUpRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest(email, "password123");
        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/auth/login", loginRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("accessToken")).isNotNull();
        assertThat(data.get("tokenType")).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401을 반환한다")
    void login_wrongPassword_returns401() {
        String email = uniqueEmail();
        SignUpRequest signUpRequest = new SignUpRequest(email, "password123", uniqueNickname());
        restTemplate.postForEntity("/v1/auth/signup", signUpRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest(email, "incorrectPassword");
        ResponseEntity<Map> response = restTemplate.postForEntity("/v1/auth/login", loginRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
