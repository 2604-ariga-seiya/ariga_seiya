package com.example.ariga_seiya.service;

import com.example.ariga_seiya.entity.Task;
import com.example.ariga_seiya.controller.form.TaskForm;
import com.example.ariga_seiya.reopsitory.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    public List<TaskForm> findAllReport(String startDate, String endDate) {
        LocalDateTime start;
        LocalDateTime end;

        if(startDate != null && !startDate.isEmpty()){
            // 入力があれば 00:00:00 を付与
            start = LocalDate.parse(startDate).atStartOfDay();
        }else{
            start = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
        if(endDate != null && !endDate.isEmpty()){
            // 入力があれば 23:59:59 を付与
            end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }else{
            // 空なら「現在の日時」をセット
            end = LocalDateTime.now();
        }

        log.info("[TaskService] Calculated search range: {} to {}", start, end);

        List<Task> results = taskRepository.findByCreatedDateBetweenOrderByLimitDateAsc(start, end);
        log.info("[TaskService] Found {} reports for display.", results.size());
        return setTaskForm(results);
    }

    private List<TaskForm> setTaskForm(List<Task> tasks) {
        log.info("[TaskService] Converting Entity to Form - Count: {} items", tasks.size());

        List<TaskForm> reports = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            TaskForm taskForm = new TaskForm();
            Task task = tasks.get(i);
            taskForm.setId(task.getId());
            taskForm.setContent(task.getContent());
            taskForm.setLimitDate(task.getLimitDate());
            taskForm.setCreatedDate(task.getCreatedDate());
            taskForm.setUpdatedDate(task.getUpdatedDate());
            reports.add(taskForm);
        }
        return reports;
    }
}
