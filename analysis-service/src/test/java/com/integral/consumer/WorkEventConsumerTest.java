package com.integral.consumer;


import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import сom.integral.client.WordCloudClient;
import сom.integral.consumer.WorkEventConsumer;
import сom.integral.domain.Report;
import сom.integral.domain.ValidationReport;
import сom.integral.dto.WorkEventDto;
import сom.integral.repository.ReportRepository;
import сom.integral.service.ValidationService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkEventConsumerTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ValidationService validationService;

    @Mock
    private WordCloudClient wordCloudClient;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private WorkEventConsumer workEventConsumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workEventConsumer, "bucketName", "test-bucket");
    }

    @Test
    void processWorkEvent_Success() throws Exception {
        WorkEventDto eventDto = new WorkEventDto();
        eventDto.setWorkId(101L);
        eventDto.setStudentName("Иван Иванов");
        eventDto.setMinioPath("test.txt");

        StatObjectResponse mockStat = mock(StatObjectResponse.class);
        when(mockStat.size()).thenReturn(1024L);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockStat);

        ValidationReport mockReport = new ValidationReport();
        mockReport.setStatus("ПРИНЯТО");
        mockReport.setRemarks(Collections.emptyList());
        when(validationService.validate(anyString(), anyLong())).thenReturn(mockReport);

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        byte[] testData = "Test content".getBytes();
        when(mockResponse.readAllBytes()).thenReturn(testData);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        byte[] mockImage = new byte[]{1, 2, 3};
        when(wordCloudClient.generateWordCloud(anyString())).thenReturn(mockImage);

        workEventConsumer.processWorkEvent(eventDto);

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(1)).save(reportCaptor.capture());

        Report savedReport = reportCaptor.getValue();
        assertEquals(101L, savedReport.getWorkId());
        assertEquals("ПРИНЯТО", savedReport.getStatus());
        assertNotNull(savedReport.getWordCloudPath());
        assertTrue(savedReport.getWordCloudPath().startsWith("plots/cloud-"));
    }

    @Test
    void processWorkEvent_ValidationFailed() throws Exception {
        WorkEventDto eventDto = new WorkEventDto();
        eventDto.setWorkId(202L);
        eventDto.setStudentName("Петр Петров");
        eventDto.setMinioPath("bad.txt");

        StatObjectResponse mockStat = mock(StatObjectResponse.class);
        when(mockStat.size()).thenReturn(5000L);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockStat);

        ValidationReport mockReport = new ValidationReport();
        mockReport.setStatus("ОТКЛОНЕНО");
        mockReport.setRemarks(Collections.singletonList("Ошибка"));
        when(validationService.validate(anyString(), anyLong())).thenReturn(mockReport);

        workEventConsumer.processWorkEvent(eventDto);

        verify(minioClient, never()).getObject(any(GetObjectArgs.class));
        verify(wordCloudClient, never()).generateWordCloud(anyString());

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(1)).save(reportCaptor.capture());

        Report savedReport = reportCaptor.getValue();
        assertEquals(202L, savedReport.getWorkId());
        assertEquals("ОТКЛОНЕНО", savedReport.getStatus());
        assertNull(savedReport.getWordCloudPath());
    }
}