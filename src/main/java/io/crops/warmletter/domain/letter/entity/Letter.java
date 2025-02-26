package io.crops.warmletter.domain.letter.entity;

import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "letters")
public class Letter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // 편지아이디 (PK, auto_increment)
    //어그리게이트 - 간접연관관계
    private Long writerId;           // 보낸사람 (writer_id) 내아이디
    private Long receiverId;         // 받는사람 (receiver_id) 맴버와 연결해야 함.

    private Long parentLetterId;     // 상위 편지아이디 (parent_letter_id)

    @Enumerated(EnumType.STRING)
    private LetterType letterType;   // 편지유형 (enum: 지점편지, 템플릿편지 등)

    @Enumerated(EnumType.STRING)
    private Category category;       // 편지 분류 (enum: 쿠폰, 응원, 그외 등)

    private String title;            // 제목
    private String content;          // 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;  // 배송 상태 (IN_DELIVERY, DELIVERED 등)

    @Column(nullable = false)
    private LocalDateTime deliveryStartedAt;    // 배송 시작 시간

    @Column(nullable = false)
    private LocalDateTime deliveryCompletedAt;  // 배송 도착 시간

    private boolean isRead;          // 열람 여부 (YES/NO 대신 boolean 처리)

    @Enumerated(EnumType.STRING)
    private FontType fontType;

    @Enumerated(EnumType.STRING)
    private PaperType paperType;     // 편지지 유형 등(필요시 enum 정의)

    private boolean isActive;        // 활성화 여부


    //빌더에 receiverId, parentLetterId가 있으면 편지 아이디 받고, 이걸 parentLetterId 에 넣자  / 상위편지아이디가 현재 편지아이디로 넣어짐
    @Builder
    public Letter(Long writerId, Long receiverId,
                   Long parentLetterId, LetterType letterType,
                   Category category, String title, String content,Status status,
                   FontType fontType, PaperType paperType) {

        this.writerId = writerId;
        this.receiverId = receiverId;
        this.parentLetterId = parentLetterId;
        this.letterType = letterType;
        this.category = category;
        this.title = title;
        this.content = content;
        this.status = status;
        this.deliveryStartedAt = LocalDateTime.now();
        this.deliveryCompletedAt = LocalDateTime.now().plusHours(1); //편지 생성 시, 답장 시 한 시간 뒤에 도착
        this.isRead = false;
        this.fontType = fontType;
        this.paperType = paperType;
        this.isActive = true; // 기본값: 활성 상태
    }

    public void inactive() {
        this.isActive = false; // 신고시 비활성 상태
    }

    public void updateLetterType(LetterType letterType) {
        this.letterType = letterType;
    }

}
