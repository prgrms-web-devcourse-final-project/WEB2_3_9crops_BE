 package io.crops.warmletter.domain.share.dto.request;

 import io.crops.warmletter.domain.share.entity.ShareProposal;
 import jakarta.validation.constraints.NotEmpty;
 import jakarta.validation.constraints.NotNull;
 import lombok.*;

 import java.util.List;

 @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareProposalRequest {
     @NotEmpty
     private List<Long> letters;      // 공유할 편지 ID 목록
     @NotNull
     private Long requesterId;        // 요청자 ID
     @NotNull
     private Long recipientId;        // 수신자 ID
     private String message;          // 공유 요청 메시지

     // testcode
     public ShareProposalRequest(List<Long> letters, Long requesterId, Long recipientId, String message) {
         this.letters = letters;
         this.requesterId = requesterId;
         this.recipientId = recipientId;
         this.message = message;
     }

     public ShareProposal toEntity() {
         return ShareProposal.builder()
                 .requesterId(requesterId)
                 .recipientId(recipientId)
                 .message(message)
                 .build();
     }

}