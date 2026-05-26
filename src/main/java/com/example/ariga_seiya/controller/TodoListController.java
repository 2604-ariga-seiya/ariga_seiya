package com.example.ariga_seiya.controller;

import com.example.ariga_seiya.controller.form.TaskForm;
import com.example.ariga_seiya.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Controller
public class TodoListController {

    @Autowired
    TaskService taskService;

    public static final String ERROR_MESSAGE = "データの更新に失敗しました。もう一度やり直すか、トップページに戻ってください。";

    /*
     * 投稿内容表示処理
     */
    @GetMapping
    public ModelAndView top(){

        log.info("[TaskController] Received request to display top page.");

        ModelAndView mav = new ModelAndView();
        LocalDate today = LocalDate.now();
        // 投稿を全件取得
        List<TaskForm> taskList = taskService.findAllTask();

        // 画面遷移先を指定
        mav.setViewName("/top");
        // 投稿データオブジェクトを保管
        mav.addObject("taskList", taskList);
        mav.addObject("todayDate", today);

        return mav;
    }

    /*
     * 絞り込み機能
     */
    @GetMapping("/search")
    public ModelAndView search(
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr){

        log.info("[TaskController] Received request to search. status: {}, content: {}, startDate: {}, endDate: {}", status, content, startDateStr, endDateStr);

        ModelAndView mav = new ModelAndView();
        mav.setViewName("/top"); // 遷移先はトップ画面

        if (content == null || content.isBlank()){

            mav.addObject("errorMessage", "タスク内容は必ず入力してください。");
            mav.addObject("taskList", taskService.findAllTask());
            mav.addObject("todayDate", LocalDate.now());

            mav.addObject("selectedStatus", status); // 画面側で選択状態を維持するため
            mav.addObject("content", content);       // 画面側に入力値を維持するため
            mav.addObject("startDate", startDateStr);
            mav.addObject("endDate", endDateStr);

            return mav;
        }

        mav.addObject("todayDate", LocalDate.now());
        mav.addObject("selectedStatus", status);
        mav.addObject("content", content);
        mav.addObject("startDate", startDateStr);
        mav.addObject("endDate", endDateStr);

        try {
            List<TaskForm> taskList = taskService.searchTasks(status, content, startDateStr, endDateStr);
            mav.addObject("taskList", taskList);

        }catch(DateTimeParseException e){
            log.warn("[TaskController] Invalid date format submitted: {}", e.getMessage());
            mav.addObject("errorMessage", "不正なパラメータです。");

            mav.addObject("taskList", taskService.findAllTask());
        }

        return mav;
    }

    /*
     * ステータス更新
     */
    @PostMapping("/tasks/update-status/{id}")
    public ModelAndView updateStatus(
            @PathVariable(name = "id") String id,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "status") Integer status) {

        log.info("[TaskController] Received request to update status. ID: {}, New Status: {}", id, status);

        if (id == null || !id.matches("^[0-9]+$")) {
            log.warn("[TaskController] Invalid ID format: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            return new ModelAndView("redirect:/");
        }

        int taskId = Integer.parseInt(id);

        try {
            taskService.updateStatus(taskId, status);

        } catch (Exception e) {
            log.error("[TaskController] Status update failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "ステータスの更新に失敗しました。");
        }

        return new ModelAndView("redirect:/");
    }

    /*
     * 新規投稿画面表示
     */
    @GetMapping("/new")
    public ModelAndView newContent(){
        log.info("[TaskController] Received request to display new task page.");

        ModelAndView mav = new ModelAndView();
        // form用の空のentityを準備
        TaskForm taskForm = new TaskForm();
        // 画面遷移先を指定
        mav.setViewName("/new");
        // 準備した空のformを保管
        mav.addObject("formModel", taskForm);
        return mav;
    }

    /*
     * 新規投稿処理
     */
    @PostMapping("/add")
    public ModelAndView addContent(
            @Validated @ModelAttribute("formModel") TaskForm taskForm,
            BindingResult result){

        log.info("[TaskController] Received request to add new task. Content: {}", taskForm.getContent());

        if(result.hasErrors()){
            log.warn("[TaskController] Validation error occurred on adding task.");

            ModelAndView mav = new ModelAndView();
            mav.setViewName("new");
            // 入力エラー情報が含まれたフォームオブジェクトをそのまま画面に送り返す
            mav.addObject("formModel", taskForm);
            return mav;
        }

        // 投稿をテーブルに格納
        try {
            taskService.saveTask(taskForm);
            log.info("[TaskController] Task added successfully. Redirecting to root.");
        } catch (IllegalArgumentException e) {
            log.warn("[TaskController] Add failed. Internal error: {}", e.getMessage());

            // エラー時は新規登録画面（new）に戻す
            ModelAndView mav = new ModelAndView();
            mav.setViewName("new");
            mav.addObject("formModel", taskForm); // 入力内容を保持
            mav.addObject("errorMessage", "ERROR_MESSAGE");

            return mav;
        }

        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * タスク編集画面初期表示処理
     */
    @GetMapping("/edit/{id}")
    public ModelAndView editTask(
            @PathVariable String id,
            RedirectAttributes redirectAttributes){

        log.info("[TaskController] Requested edit for TaskID: {}", id);

        if (id == null || !id.matches("^[0-9]+$")) {
            log.warn("[TaskController] Invalid ID format: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            return new ModelAndView("redirect:/");
        }

        ModelAndView mav = new ModelAndView();
        int taskId = Integer.parseInt(id);
        TaskForm taskForm = taskService.findTaskFormById(taskId);

        if (taskForm == null) {
            log.warn("[TaskController] Invalid edit access - TaskID: {} does not exist.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            // rootへリダイレクト
            return new ModelAndView("redirect:/");
        }

        mav.setViewName("/edit");
        // 投稿データオブジェクトを保管
        mav.addObject("formModel", taskForm);

        log.info("[TaskController] Edit screen displayed. TaskID: {}", id);

        return mav;
    }

    /*
     * 編集反映処理
     */
    @PutMapping("/update/{id}")
    public ModelAndView updateContent(
            @PathVariable String id,
            @Validated @ModelAttribute("formModel") TaskForm task,
            BindingResult result,
            RedirectAttributes redirectAttributes){

        log.info("[TaskController] Received request to update task - ID: {}, New Content: {}", id, task.getContent());

        if (id == null || !id.matches("^[0-9]+$")) {
            log.warn("[TaskController] Invalid ID format: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            return new ModelAndView("redirect:/");
        }

        ModelAndView mav = new ModelAndView();

        if(result.hasErrors()){
            log.warn("[TaskController] Validation error occurred on editing task.");

            mav.setViewName("edit");
            // 入力エラー情報が含まれたフォームオブジェクトをそのまま画面に送り返す
            mav.addObject("formModel", task);
            return mav;
        }

        // UrlParameterのidを更新するentityにセット
        int taskId = Integer.parseInt(id);
        task.setId(taskId);

        try{
            // 編集した投稿を更新
            taskService.saveTask(task);
            log.info("[TaskController] Task update successful. Redirecting to root. ID: {}", id);

        }catch (IllegalArgumentException e){
            log.warn("[TaskController] Update failed. Internal error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("[Controller] Handled exception: {}", e.getMessage());

            mav.setViewName("edit");
            mav.addObject("errorMessage", ERROR_MESSAGE);

            return mav;
        }

        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * タスク削除処理
     */
    @DeleteMapping("/delete/{id}")
    public ModelAndView deleteTask(@PathVariable String id, RedirectAttributes redirectAttributes){
        log.info("[TaskController] Received request to delete task - ID: {}", id);

        if (id == null || !id.matches("^[0-9]+$")) {
            log.warn("[TaskController] Invalid ID format: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            return new ModelAndView("redirect:/");
        }


        int taskId = Integer.parseInt(id);
        TaskForm taskForm = taskService.findTaskFormById(taskId);

        if (taskForm == null) {
            log.warn("[TaskController] Invalid edit access - TaskID: {} does not exist.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "不正なパラメータです。");
            // rootへリダイレクト
            return new ModelAndView("redirect:/");
        }

        taskService.deleteTask(taskId);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }
}
