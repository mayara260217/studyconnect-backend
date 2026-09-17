package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.ProgressoAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressoAulaRepository extends JpaRepository<ProgressoAula, Long> {

    Optional<ProgressoAula> findByAlunoIdAndAulaId(Long alunoId, Long aulaId);

    List<ProgressoAula> findByAlunoIdAndConcluidaTrue(Long alunoId);

    List<ProgressoAula> findByAlunoIdAndAulaIdInAndConcluidaTrue(Long alunoId, List<Long> aulaIds);

    long countByAlunoIdAndAulaIdInAndConcluidaTrue(Long alunoId, List<Long> aulaIds);

    void deleteByAlunoId(Long alunoId);
}
