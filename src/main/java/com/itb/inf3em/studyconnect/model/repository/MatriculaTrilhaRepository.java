package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.MatriculaTrilha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaTrilhaRepository extends JpaRepository<MatriculaTrilha, Long> {

    List<MatriculaTrilha> findByAlunoIdAndAtivoTrue(Long alunoId);

    List<MatriculaTrilha> findByTrilhaIdAndAtivoTrue(Long trilhaId);

    Optional<MatriculaTrilha> findByAlunoIdAndTrilhaId(Long alunoId, Long trilhaId);

    boolean existsByAlunoIdAndTrilhaIdAndAtivoTrue(Long alunoId, Long trilhaId);

    long countByTrilhaIdAndAtivoTrue(Long trilhaId);

    long countByAtivoTrue();

    void deleteByAlunoId(Long alunoId);

    /** Returns [trilhaId, count] pairs for all trilhas with active matriculas */
    @Query("SELECT m.trilhaId, COUNT(m) FROM MatriculaTrilha m WHERE m.ativo = true GROUP BY m.trilhaId")
    List<Object[]> countAlunosPorTrilha();
}
