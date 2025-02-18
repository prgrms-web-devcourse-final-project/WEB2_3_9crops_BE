package io.crops.warmletter.domain.letter.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class LettersMatching {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Letters letters;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id") //최초로 편지를 쓴 ID
//    private Member firstMember;
//
//
//    //내 아이디
//    private Member secondMember;

    private boolean isActive;

    private LocalDateTime matchedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime replyDeadLine;


}
