package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import java.util.UUID;
/**
 * Value object que representa a referencia de uma sala externa.
 *
 * <p>Usado para manter o dominio dependente apenas do identificador da sala,
 * enquanto os dados completos continuam sendo responsabilidade do Hub.</p>
 */
@Embeddable
@Getter
public class RoomReference {

@Column(name = "room_id",nullable = false)
    private UUID roomid;


    protected RoomReference(){}

    public RoomReference(UUID roomid){
        if (roomid == null){
            throw new IllegalArgumentException("O ID da sala esta nulo");
        }
        this.roomid = roomid;
    }

}
