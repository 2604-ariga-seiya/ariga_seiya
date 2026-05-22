package com.example.ariga_seiya.reopsitory;

import com.example.ariga_seiya.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    List<Task> findByCreatedDateBetweenOrderByLimitDateAsc(LocalDateTime start, LocalDateTime end);
}
