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
     private List<Long> letterIds;      // 공유할 편지 ID 목록

     @NotNull
     private Long recipientId;        // 수신자 ID
     private String message;          // 공유 요청 메시지

     // testcode 없애고,다르게
     public ShareProposalRequest(List<Long> letterIds, Long recipientId, String message) {
         this.letterIds = letterIds;
         this.recipientId = recipientId;
         this.message = message;
     }

     public ShareProposal toEntity(Long requesterId) {
         return ShareProposal.builder()
                 .requesterId(requesterId)
                 .recipientId(recipientId)
                 .message(message)
                 .build();
     }

}