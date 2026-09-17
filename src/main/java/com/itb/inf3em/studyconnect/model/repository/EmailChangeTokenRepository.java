package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.EmailChangeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailChangeTokenRepository extends JpaRepository<EmailChangeToken, Long> {
    Optional<EmailChangeToken> findByConfirmToken(String confirmToken);
    Optional<EmailChangeToken> findByUsuarioId(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
