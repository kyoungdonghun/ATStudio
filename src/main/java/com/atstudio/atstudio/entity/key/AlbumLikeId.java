package com.atstudio.atstudio.entity.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class AlbumLikeId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long albumId;
}
