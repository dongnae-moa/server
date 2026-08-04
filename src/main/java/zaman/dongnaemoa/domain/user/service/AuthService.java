package zaman.dongnaemoa.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.themoment.sdk.exception.ExpectedException;
import zaman.dongnaemoa.domain.user.dto.LoginRequest;
import zaman.dongnaemoa.domain.user.dto.LoginResponse;
import zaman.dongnaemoa.domain.user.dto.SignUpRequest;
import zaman.dongnaemoa.domain.user.dto.SignUpResponse;
import zaman.dongnaemoa.domain.user.entity.User;
import zaman.dongnaemoa.domain.user.repository.UserRepository;
import zaman.dongnaemoa.global.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String LOGIN_FAIL_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ExpectedException("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new ExpectedException("이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .neighborhood(null)
                .build();

        User saved = userRepository.save(user);
        return SignUpResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ExpectedException(LOGIN_FAIL_MESSAGE, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ExpectedException(LOGIN_FAIL_MESSAGE, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        return LoginResponse.of(accessToken, user);
    }
}
