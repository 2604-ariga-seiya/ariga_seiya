package com.example.ariga_seiya.entity;

import java.time.LocalDateTime;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
@Data // Lombok: getter, setter, toString, equals, hashCode を自動生成
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // nextval('tasks_id_seq') に対応
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "limit_date", nullable = false)
    private LocalDateTime limitDate;

    @Column(name = "created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    public boolean isExpired() {
        if (this.limitDate == null) {
            return false;
        }

        java.time.LocalDate taskDate = this.limitDate.toLocalDate();
        java.time.LocalDate today = java.time.LocalDate.now();

        return taskDate.isBefore(today);
    }
}
