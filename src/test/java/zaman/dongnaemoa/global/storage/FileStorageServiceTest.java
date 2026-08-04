package zaman.dongnaemoa.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import team.themoment.sdk.exception.ExpectedException;

class FileStorageServiceTest {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    private FileStorageService newService(Path uploadDir) {
        return new FileStorageService(uploadDir.toString(), "http://localhost:8080/files");
    }

    @Test
    void store_validPng_savesAndReturnsUrl(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        FileStorageService service = newService(tempDir);
        MockMultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", PNG_SIGNATURE);

        String url = service.store(file);

        assertThat(url).startsWith("http://localhost:8080/files/");
        assertThat(Files.list(tempDir).count()).isEqualTo(1);
    }

    @Test
    void store_exceedsMaxSize_throwsBadRequest(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        FileStorageService service = newService(tempDir);
        byte[] oversized = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("image", "big.png", "image/png", oversized);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    void store_signatureMismatch_throwsBadRequest(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        FileStorageService service = newService(tempDir);
        MockMultipartFile file = new MockMultipartFile("image", "fake.png", "image/png", "not-a-real-png".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("올바르지 않습니다");
    }

    @Test
    void store_disallowedContentType_throwsBadRequest(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        FileStorageService service = newService(tempDir);
        MockMultipartFile file = new MockMultipartFile(
                "image", "script.svg", "image/svg+xml", "<svg></svg>".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("지원하지 않는 이미지 형식");
    }

    @Test
    void store_disallowedExtension_throwsBadRequest(@org.junit.jupiter.api.io.TempDir Path tempDir) {
        FileStorageService service = newService(tempDir);
        MockMultipartFile file = new MockMultipartFile("image", "photo.png.exe", "image/png", PNG_SIGNATURE);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ExpectedException.class)
                .hasMessageContaining("지원하지 않는 파일 확장자");
    }
}
