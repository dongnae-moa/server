package zaman.dongnaemoa.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.user.dto.LoginRequest;
import zaman.dongnaemoa.domain.user.dto.LoginResponse;
import zaman.dongnaemoa.domain.user.dto.SignUpRequest;
import zaman.dongnaemoa.domain.user.dto.SignUpResponse;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.jwt.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider);
    }

    @Test
    @DisplayName("이메일과 닉네임이 중복되지 않으면 회원가입에 성공한다")
    void signUp_success() {
        SignUpRequest request = new SignUpRequest("new@test.com", "password123", "newnick");
        User savedUser = User.builder()
                .email(request.email())
                .password("encodedPassword")
                .nickname(request.nickname())
                .neighborhood(null)
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignUpResponse response = authService.signUp(request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.nickname()).isEqualTo(request.nickname());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 CONFLICT 예외를 던진다")
    void signUp_duplicateEmail_throwsConflict() {
        SignUpRequest request = new SignUpRequest("dup@test.com", "password123", "nick");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("이메일");
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 CONFLICT 예외를 던진다")
    void signUp_duplicateNickname_throwsConflict() {
        SignUpRequest request = new SignUpRequest("new@test.com", "password123", "duplicatenick");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByNickname(request.nickname())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("닉네임");
    }

    @Test
    @DisplayName("이메일과 비밀번호가 올바르면 로그인에 성공하고 토큰을 발급한다")
    void login_success() {
        LoginRequest request = new LoginRequest("user@test.com", "password123");
        User user = User.builder()
                .email(request.email())
                .password("encodedPassword")
                .nickname("nick")
                .neighborhood(null)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .thenReturn("access-token");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.nickname()).isEqualTo("nick");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 UNAUTHORIZED 예외를 던진다")
    void login_emailNotFound_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("missing@test.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ExpectedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 UNAUTHORIZED 예외를 던진다")
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("user@test.com", "wrongPassword");
        User user = User.builder()
                .email(request.email())
                .password("encodedPassword")
                .nickname("nick")
                .neighborhood(null)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ExpectedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이메일과 잘못된 비밀번호는 동일한 에러 메시지를 반환한다 (사용자 열거 방지)")
    void login_bothFailureCases_haveSameMessage() {
        LoginRequest missingEmailRequest = new LoginRequest("missing@test.com", "password123");
        when(userRepository.findByEmail(missingEmailRequest.email())).thenReturn(Optional.empty());

        String missingEmailMessage = catchExpectedExceptionMessage(() -> authService.login(missingEmailRequest));

        LoginRequest wrongPasswordRequest = new LoginRequest("user@test.com", "wrongPassword");
        User user = User.builder()
                .email(wrongPasswordRequest.email())
                .password("encodedPassword")
                .nickname("nick")
                .neighborhood(null)
                .build();
        when(userRepository.findByEmail(wrongPasswordRequest.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        String wrongPasswordMessage = catchExpectedExceptionMessage(() -> authService.login(wrongPasswordRequest));

        assertThat(missingEmailMessage).isEqualTo(wrongPasswordMessage);
    }

    private String catchExpectedExceptionMessage(Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Expected ExpectedException to be thrown");
        } catch (ExpectedException e) {
            return e.getMessage();
        }
    }
}
