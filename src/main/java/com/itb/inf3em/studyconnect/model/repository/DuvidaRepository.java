package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Duvida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuvidaRepository extends JpaRepository<Duvida, Long> {

    List<Duvida> findByTrilhaIdOrderByCriadaEmDesc(Long trilhaId);

    List<Duvida> findByAulaIdOrderByCriadaEmDesc(Long aulaId);

    List<Duvida> findByAlunoIdAndAulaIdOrderByCriadaEmDesc(Long alunoId, Long aulaId);

    List<Duvida> findByAlunoIdOrderByCriadaEmDesc(Long alunoId);

    long countByTrilhaIdAndStatus(Long trilhaId, String status);

    void deleteByAlunoId(Long alunoId);

    void deleteByTrilhaId(Long trilhaId);
}
