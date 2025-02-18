package io.crops.warmletter.domain.letter.entity;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.DeliveryStatus;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Letters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // 편지아이디 (PK, auto_increment)

    private Long writerId;           // 보낸사람 (writer_id)
    private Long receiverId;         // 받는사람 (receiver_id)
    private Long parentLetterId;     // 부모 편지아이디 (parent_letter_id)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LetterType letterType;   // 편지유형 (enum: 지점편지, 템플릿편지 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;       // 편지 분류 (enum: 쿠폰, 응원, 그외 등)

    private String title;            // 제목
    private String content;          // 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;  // 배송 상태 (IN_DELIVERY, DELIVERED 등)

    @Column(nullable = false)
    private LocalDateTime deliveryStartedAt;    // 배송 시작 시간

    @Column(nullable = false)
    private LocalDateTime deliveryCompletedAt;  // 배송 도착 시간

    private Boolean isRead;          // 열람 여부 (YES/NO 대신 boolean 처리)

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성시간

    @Enumerated(EnumType.STRING)
    private PaperType paperType;     // 편지지 유형 등(필요시 enum 정의)

    private Boolean isActive;        // 활성화 여부

    @Builder
    public Letters(Long writerId,
                   Long receiverId,
                   Long parentLetterId,
                   LetterType letterType,
                   Category category,
                   String title,
                   String content,
                   DeliveryStatus deliveryStatus,
                   LocalDateTime deliveryStartedAt,
                   LocalDateTime deliveryCompletedAt,
                   Boolean isRead,
                   LocalDateTime createdAt,
                   PaperType paperType,
                   Boolean isActive) {
        this.writerId = writerId;
        this.receiverId = receiverId;
        this.parentLetterId = parentLetterId;
        this.letterType = letterType;
        this.category = category;
        this.title = title;
        this.content = content;
        this.deliveryStatus = deliveryStatus;
        this.deliveryStartedAt = deliveryStartedAt;
        this.deliveryCompletedAt = deliveryCompletedAt;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.paperType = paperType;
        this.isActive = isActive;
    }
}
