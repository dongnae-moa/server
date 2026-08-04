package zaman.dongnaemoa.global.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import team.themoment.sdk.exception.ExpectedException;

@Component
@RequiredArgsConstructor
public class MultipartJsonParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public <T> T parse(String json, Class<T> type) {
        T value;
        try {
            value = objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new ExpectedException("요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        Set<ConstraintViolation<T>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new ExpectedException(message, HttpStatus.BAD_REQUEST);
        }

        return value;
    }
}
