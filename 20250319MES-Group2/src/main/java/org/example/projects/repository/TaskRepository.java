package org.example.projects.repository;

import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProcessProductionLine(ProductionLine productionLine);
}
