package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {

    /**
     * Find all aulas for a specific trilha, ordered by ordem.
     */
    List<Aula> findByTrilhaIdOrderByOrdem(Long trilhaId);

    long countByTrilhaId(Long trilhaId);

    long countByTrilhaIdAndStatus(Long trilhaId, String status);
}