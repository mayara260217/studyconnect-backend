package com.itb.inf3em.studyconnect.model.repository;

import com.itb.inf3em.studyconnect.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrderByCriadaEmDesc();
    List<Ticket> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
}
