package com.integral.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import сom.integral.AnalysisApplication;
import сom.integral.controller.ReportController;
import сom.integral.domain.Report;
import сom.integral.repository.ReportRepository;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Optional;

@WebMvcTest(controllers = ReportController.class)
@ContextConfiguration(classes = {AnalysisApplication.class})
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportRepository reportRepository;

    @Test
    @DisplayName("Успешное получение отчета: 200")
    void getReportByWorkId200() throws  Exception {
        Report mockReport = new Report();
        mockReport.setId(1L);
        mockReport.setWorkId(13L);
        mockReport.setStatus("ПРИНЯТО");
        mockReport.setRemarks(Collections.emptyList());
        mockReport.setWordCloudPath("plots/cloud.png");

        when(reportRepository.findByWorkId(13L)).thenReturn(Optional.of(mockReport));
        mockMvc.perform(get("/reports/13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workId").value(13))
                .andExpect(jsonPath("$.status").value("ПРИНЯТО"))
                .andExpect(jsonPath("$.wordCloudPath").value("plots/cloud.png"));
    }

    @Test
    @DisplayName("Отчет не найден: статус 404")
    void getReportByWorkId404() throws Exception {
        when(reportRepository.findByWorkId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reports/999999"))
                .andExpect(status().isNotFound());
    }
}
