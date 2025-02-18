package io.crops.warmletter.domain.moderation.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Moderation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String word;

}
