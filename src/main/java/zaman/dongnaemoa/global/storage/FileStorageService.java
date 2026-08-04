package zaman.dongnaemoa.global.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.sdk.exception.ExpectedException;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final String publicBaseUrl;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir,
                               @Value("${file.public-base-url:http://localhost:8080/files}") String publicBaseUrl) {
        this.uploadDir = Path.of(uploadDir);
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

        String extension = extractExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(storedName));
        } catch (IOException e) {
            throw new ExpectedException("이미지 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return publicBaseUrl + "/" + storedName;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
