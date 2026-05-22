package com.example.ariga_seiya.controller.form;

import com.example.ariga_seiya.entity.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.*;

@Setter
@Getter
public class TaskForm {
    private int id;
    @NotBlank(message = "タスクを入力してください")
    private String content;
    private Integer status;
    private LocalDateTime limitDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}

