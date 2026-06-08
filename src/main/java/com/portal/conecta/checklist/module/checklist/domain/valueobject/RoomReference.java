package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;

import java.util.UUID;

/**
 * Value object que representa a referencia de uma sala externa.
 *
 * <p>O UUID e o dado persistivel. Numero, tipo e status sao metadados obtidos
 * do Hub quando a aplicacao precisa exibir ou validar informacoes reais da
 * sala.</p>
 */
@Embeddable
@Getter
public class RoomReference {

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Transient
    private Integer number;

    @Transient
    private String typeRoom;

    @Transient
    private String status;

    protected RoomReference() {
    }

    public RoomReference(UUID roomId) {
        this(roomId, null, null, null);
    }

    public RoomReference(UUID roomId, Integer number, String typeRoom, String status) {
        if (roomId == null) {
            throw new IllegalArgumentException("O ID da sala esta nulo");
        }
        this.roomId = roomId;
        this.number = number;
        this.typeRoom = typeRoom;
        this.status = status;
    }
}
