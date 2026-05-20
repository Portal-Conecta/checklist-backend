package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class UserReference {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected UserReference() {
    }

    public UserReference(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("O ID do usuario esta nulo");
        }
        this.userId = userId;
    }
}
