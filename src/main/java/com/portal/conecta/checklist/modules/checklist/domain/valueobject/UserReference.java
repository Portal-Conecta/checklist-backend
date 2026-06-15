package com.portal.conecta.checklist.modules.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;

import java.util.UUID;

/**
 * Value object que representa a referencia do usuario autenticado.
 *
 * <p>O UUID e a informacao persistivel. Nome, email, tipo e status podem ser
 * preenchidos a partir do Hub em fluxos de leitura, sem duplicar a fonte de
 * verdade dentro do Checklist.</p>
 */
@Embeddable
@Getter
public class UserReference {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Transient
    private String name;

    @Transient
    private String email;

    @Transient
    private String typeUser;

    @Transient
    private Boolean active;

    protected UserReference() {
    }

    public UserReference(UUID userId) {
        this(userId, null, null, null, null);
    }

    public UserReference(UUID userId, String name, String email, String typeUser, Boolean active) {
        if (userId == null) {
            throw new IllegalArgumentException("O ID do usuario esta nulo");
        }
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.typeUser = typeUser;
        this.active = active;
    }
}
