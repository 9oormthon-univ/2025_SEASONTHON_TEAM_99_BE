package seasonton.youthPolicy.global.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.global.dto.S3DTO;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.exception.GeneralException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;


    // 파일 업로드
    public S3DTO.UploadResult uploadFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "_" + originalName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = minioUrl + "/" + bucketName + "/" + storedName;

            return new S3DTO.UploadResult(originalName, storedName, fileUrl);

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    // 파일 삭제
    public void deleteFile(String storedName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedName)
                            .build()
            );
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.FILE_DELETE_FAILED);
        }
    }
}
