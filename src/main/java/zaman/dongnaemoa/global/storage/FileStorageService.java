package zaman.dongnaemoa.global.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, byte[]> SIGNATURES_BY_MIME = Map.of(
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadDir;
    private final String publicBaseUrl;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir,
                               @Value("${file.public-base-url:http://localhost:8080/files}") String publicBaseUrl) {
        this.uploadDir = Path.of(uploadDir).normalize().toAbsolutePath();
        this.publicBaseUrl = publicBaseUrl;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉토리를 생성할 수 없습니다: " + this.uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExpectedException("이미지 파일이 비어 있습니다.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ExpectedException("이미지 파일은 5MB를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        byte[] signature = SIGNATURES_BY_MIME.get(contentType);
        if (signature == null) {
            throw new ExpectedException("지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ExpectedException("지원하지 않는 파일 확장자입니다.", HttpStatus.BAD_REQUEST);
        }

        byte[] header = readHeader(file, signature.length);
        if (!Arrays.equals(header, signature)) {
            throw new ExpectedException("이미지 파일 내용이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        String storedName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.getParent().equals(uploadDir)) {
            throw new ExpectedException("잘못된 파일 경로입니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new ExpectedException("이미지 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return publicBaseUrl + "/" + storedName;
    }

    private byte[] readHeader(MultipartFile file, int length) {
        try (var in = file.getInputStream()) {
            byte[] header = new byte[length];
            int read = in.readNBytes(header, 0, length);
            return read == length ? header : new byte[0];
        } catch (IOException e) {
            throw new ExpectedException("이미지 파일을 읽을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
