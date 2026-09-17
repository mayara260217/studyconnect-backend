package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;

public record AuthenticatedUser(Long usuarioId, String email, TipoUsuario tipoUsuario) {
}
