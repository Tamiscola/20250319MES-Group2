package org.example.projects.repository;

import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @EntityGraph(attributePaths = "mId")
    Page<Material> findAll(Pageable pageable);

    Optional<Material> findById(Long mId);

    // Corrected method names to match the entity field name
    @Query("SELECT m FROM Material m WHERE m.mProcess = :mProcess AND m.mStatus = :mStatus")
    Page<Material> findByMProcessAndMStatus(@Param("mProcess") ProcessType mProcess,
                                            @Param("mStatus") Status mStatus,
                                            Pageable pageable);

    @Query("SELECT m FROM Material m WHERE m.mProcess = :mProcess")
    Page<Material> findByMProcess(@Param("mProcess") ProcessType mProcess, Pageable pageable);

    @Query("SELECT m FROM Material m WHERE m.mStatus = :mStatus")
    Page<Material> findByMStatus(@Param("mStatus") Status mStatus, Pageable pageable);

    @Query("SELECT m FROM Material m WHERE m.mProcess = :mProcess")
    List<Material> findByProcessType(@Param("mProcess") ProcessType mProcess);
}
