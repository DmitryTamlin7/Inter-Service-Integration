package com.integral.controller;

import com.integral.service.WorkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkController.class)
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkService workService;

    @Test
    void uploadWork_Success200() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "diploma.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Тестовое содержимое файла".getBytes()
        );

        mockMvc.perform(multipart("/works/upload")
                        .file(mockFile)
                        .param("studentName", "Иван Иванов")
                        .param("workId", "101"))
                .andExpect(status().isOk());

        verify(workService, times(1)).uploadWork(any(MockMultipartFile.class), anyString());
    }

    @Test
    void uploadWork_MissingFileBadRequest() throws Exception {
        mockMvc.perform(multipart("/works/upload")
                        .param("studentName", "Иван Иванов")
                        .param("workId", "101"))
                .andExpect(status().isBadRequest());
    }
}