package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.PerfilAprendizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilAprendizadoRepository extends JpaRepository<PerfilAprendizado, Long> {
    Optional<PerfilAprendizado> findByAlunoId(Long alunoId);

    void deleteByAlunoId(Long alunoId);
}
