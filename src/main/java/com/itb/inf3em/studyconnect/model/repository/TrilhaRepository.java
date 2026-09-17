package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Trilha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrilhaRepository extends JpaRepository<Trilha, Long> {

    /**
     * Find all trilhas created by a specific teacher.
     */
    List<Trilha> findByProfessorId(Long professorId);

    /**
     * Find trilhas by tipo.
     */
    List<Trilha> findByTipo(String tipo);
}