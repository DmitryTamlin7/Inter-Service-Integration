package сom.integral.domain;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Data
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", unique = true, nullable = false)
    private Long workId;

    @Column(name = "status", nullable = false)
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_remarks", joinColumns = @JoinColumn(name = "report_id"))
    private List<String> remarks = new ArrayList<>();

    @Column(name = "word_cloud_path")
    private String wordCloudPath;

}
