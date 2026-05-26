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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    public List<TaskForm> findAllTask() {

        List<Task> results = taskRepository.findAllByOrderByLimitDateAsc();
        log.info("[TaskService] Found {} task for display.", results.size());
        return setTaskForm(results);
    }

    private List<TaskForm> setTaskForm(List<Task> tasks) {
        log.info("[TaskService] Converting Entity to Form - Count: {} items", tasks.size());

        List<TaskForm> taskList = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            TaskForm taskForm = new TaskForm();
            Task task = tasks.get(i);
            taskForm.setId(task.getId());
            taskForm.setContent(task.getContent());
            taskForm.setStatus(task.getStatus());
            taskForm.setLimitDate(task.getLimitDate().toLocalDate());
            taskForm.setCreatedDate(task.getCreatedDate());
            taskForm.setUpdatedDate(task.getUpdatedDate());
            taskList.add(taskForm);
        }
        return taskList;
    }

    public List<TaskForm> searchTasks(Integer status, String content, String startDateStr, String endDateStr){

        log.info("[TaskService] Starting search - Status: {}, Content: '{}', StartDateStr: '{}', EndDateStr: '{}'",
                status, content, startDateStr, endDateStr);

        LocalDateTime startDate;
        if (startDateStr == null || startDateStr.isBlank()) {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        } else {
            // 文字列を一度LocalDateとして解析し、一日の始まり（00:00:00）を結合する
            startDate = LocalDate.parse(startDateStr).atStartOfDay();
        }

        LocalDateTime endDate;
        if (endDateStr == null || endDateStr.isBlank()) {
            endDate = LocalDateTime.of(2100, 12, 31, 23, 59, 59);
        } else {
            // 文字列を一度LocalDateとして解析し、一日の終わり（23:59:59）を結合する
            endDate = LocalDate.parse(endDateStr).atTime(23, 59, 59);
        }

        log.info("[TaskService] Parsed Search Bounds - StartDate: {}, EndDate: {}", startDate, endDate);

        // 検索実行
        List<Task> results = taskRepository.findAllByStatusAndContentContainingAndLimitDateBetweenOrderByLimitDateAsc(status, content, startDate, endDate);

        // Formへの詰め替え
        List<TaskForm> formResults = setTaskForm(results);

        log.info("[TaskService] Search completed successfully. Found {} tasks.", formResults.size());

        return formResults;
    }

    @Transactional
    public void updateStatus(Integer id, Integer newStatus) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found. ID: " + id));

        task.setStatus(newStatus);

        task.setUpdatedDate(LocalDateTime.now());

        taskRepository.save(task);
    }

    /*
     * 保存処理（新規登録・更新）
     */
    public void saveTask(TaskForm reqTask) {

        if (reqTask.getLimitDate() != null) {
            // 現在の値を一度文字列(yyyy-MM-dd)として扱い、00:00:00 を結合して LocalDateTime に変換
            String dateStr = reqTask.getLimitDate().toString().substring(0, 10);

            LocalDateTime convertedDateTime = LocalDate.parse(dateStr).atStartOfDay();

            // 変換した時分秒付きのデータをセットし直す
            reqTask.setLimitDate(convertedDateTime.toLocalDate());
        }

        // 更新処理（id != 0）のとき、DBに存在するかチェック
        if (reqTask.getId() != null && reqTask.getId() != 0) {
            boolean exists = taskRepository.existsById(reqTask.getId());
            if (!exists) {
                // 存在しない場合はログを残して、安全にnullを返す
                log.warn("[TaskService] Update failed. Task ID: {} does not exist.", reqTask.getId());
                throw new IllegalArgumentException("Task ID " + reqTask.getId() + " not found.");
            }
        }

        // 1. 新規か更新かを判定してログを出し分ける
        if (reqTask.getId() != null && reqTask.getId() == 0) {
            log.info("[TaskService] Creating new Task - Content: {}", reqTask.getContent());
        } else {
            log.info("[TaskService] Updating existing Task - ID: {}, Content: {}",
                    reqTask.getId(), reqTask.getContent());
        }

        // Entityへの変換
        Task saveTask = setTaskEntity(reqTask);

        // DBへの保存実行
        taskRepository.save(saveTask);

        // 2. 正常終了を記録
        log.info("[TaskService] Task save operation completed successfully.");
    }

    /*
     * リクエストから取得した情報をEntityに設定
     */
    private Task setTaskEntity(TaskForm reqTask){
        log.info("[TaskService] Converting Form to Entity - ID: {}", reqTask.getId());

        Task task = new Task();

        if (reqTask.getId() != null && reqTask.getId() != 0) {
            task.setId(reqTask.getId());
        } else {
            // IDが null や 0 の時は、明示的に null（新規登録）としてJPAに引き渡す
            task.setId(null);
        }

        task.setLimitDate(reqTask.getLimitDate().atTime(23, 59, 59));
        task.setStatus(1);
        task.setContent(reqTask.getContent());
        return task;
    }

    /*
     * 投稿を1件取得
     */
    public TaskForm findTaskFormById(Integer id) {
        log.info("[TaskService] Fetching task for edit - TaskID: {}", id);

        List<Task> results = new ArrayList<>();
        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            log.warn("[TaskService] Task not found - TaskID: {}", id);
            return null;
        }

        results.add(task);
        List<TaskForm> taskList = setTaskForm(results);

        return taskList.get(0);
    }

    public void deleteTask(int id) {
        log.warn("[TaskService] Executing delete - TaskID: {}", id);

        taskRepository.deleteById(id);

        log.info("[TaskService] Delete completed successfully - TaskID: {}", id);
    }
}
