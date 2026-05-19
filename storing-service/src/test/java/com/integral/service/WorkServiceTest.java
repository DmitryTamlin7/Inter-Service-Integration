package com.integral.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private com.integral.repository.WorkRepository workRepository;

    @InjectMocks
    private WorkService workService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workService, "bucketName", "works-bucket");
        ReflectionTestUtils.setField(workService, "exchangeName", "work-exchange");
        ReflectionTestUtils.setField(workService, "routingKey", "work.routing.key");
    }

    @Test
    void uploadSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "diploma.txt",
                "text/plain",
                "Hello World".getBytes()
        );
        String studentName = "Иван Иванов";


        com.integral.model.Work mockWork = mock(com.integral.model.Work.class);
        com.integral.model.WorkStatus mockStatus = mock(com.integral.model.WorkStatus.class);

        when(mockStatus.name()).thenReturn("PENDING");
        when(mockWork.getStatus()).thenReturn(mockStatus);
        when(workRepository.save(any(com.integral.model.Work.class))).thenReturn(mockWork);


        workService.uploadWork(mockFile, studentName);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        verify(workRepository, times(1)).save(any(com.integral.model.Work.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), eventCaptor.capture());

        Object sentEvent = eventCaptor.getValue();
        assertNotNull(sentEvent);
    }
}