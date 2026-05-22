package com.example.ariga_seiya.controller;

import com.example.ariga_seiya.controller.form.TaskForm;
import com.example.ariga_seiya.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
public class TodoListController {

    @Autowired
    TaskService taskService;

    /*
     * 投稿内容表示処理
     */
    @GetMapping
    public ModelAndView top(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate){

        log.info("[Controller] Received request to display top page.");

        ModelAndView mav = new ModelAndView();
        LocalDate today = LocalDate.now();
        // 投稿を全件取得
        List<TaskForm> taskList = taskService.findAllReport(startDate, endDate);

        // 画面遷移先を指定
        mav.setViewName("/top");
        // 投稿データオブジェクトを保管
        mav.addObject("taskList", taskList);
        mav.addObject("todayDate", today);
        // 画面に入力値を保持させるために再セット
        mav.addObject("startDate", startDate);
        mav.addObject("endDate", endDate);

        return mav;
    }

    @GetMapping("/search")
    public ModelAndView search(
            @RequestParam(name = "status", required = true) String stasus,
            @RequestParam(name = "content", required = true) String content){

        log.info("[Controller] Received request to search.");
    }
}
