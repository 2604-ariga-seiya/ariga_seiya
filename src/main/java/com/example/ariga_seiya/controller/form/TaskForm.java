package com.example.ariga_seiya.controller.form;

import com.example.ariga_seiya.entity.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class TaskForm {
    private Integer id;
    @NotBlank(message = "タスクを入力してください")
    @Size(max = 140, message = "タスクは140文字以内で入力してください")
    private String content;
    private Integer status;
    @FutureOrPresent(message = "無効な日付です")
    @NotNull(message = "期限を設定してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate limitDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}