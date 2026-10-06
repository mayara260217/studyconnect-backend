package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository é a camada que faz as consultas no banco de dados.
// O Spring Data JPA gera as queries automaticamente a partir dos nomes dos métodos.
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Busca todos os materiais de um usuário específico, do mais recente para o mais antigo
    List<Material> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    // Busca todos os materiais de uma matéria específica (ex: "Matemática")
    List<Material> findByMateriaIgnoreCaseOrderByCriadoEmDesc(String materia);

    // Busca todos os materiais de um tipo específico (ex: "PDF", "Resumo")
    List<Material> findByTipoIgnoreCaseOrderByCriadoEmDesc(String tipo);
}
