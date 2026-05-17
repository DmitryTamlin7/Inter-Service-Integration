package сom.integral.dto;

import lombok.Data;

@Data
public class WorkEventDto {
    private Long workId;
    private String studentName;
    private String originalFileName;
    private String minioPath;
    private String status;
    private String uploadedAt;
}
