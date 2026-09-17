package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findTop6ByOrderByIdDesc();

    /** Returns [tipoUsuario, count] */
    @Query("SELECT u.tipoUsuario, COUNT(u) FROM Usuario u GROUP BY u.tipoUsuario")
    List<Object[]> countByTipoUsuario();
}
