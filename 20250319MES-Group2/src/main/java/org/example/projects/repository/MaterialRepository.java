package org.example.projects.repository;

import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @EntityGraph(attributePaths = "mId")
    Page<Material> findAll(Pageable pageable);

    Optional<Material> findById(Long mId);

    Page<Material> findByMProcessAndMStatus(ProcessType process, Status status, Pageable pageable);

    Page<Material> findByMProcess(ProcessType process, Pageable pageable);

    Page<Material> findByMStatus(Status status, Pageable pageable);
}
