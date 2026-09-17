package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    /**
     * Find a turma by its unique codigo (class code).
     * Used for joining classes via code on the frontend.
     */
    Optional<Turma> findByCodigo(String codigo);

    /**
     * Find all turmas created by a specific teacher.
     */
    List<Turma> findByProfessorId(Long professorId);

    /**
     * Check if a codigo already exists (for validation before creating new turma).
     */
    boolean existsByCodigo(String codigo);
}
