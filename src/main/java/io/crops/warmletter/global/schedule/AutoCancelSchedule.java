package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AutoCancelSchedule {

    private final LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;
    private final LetterRepository letterRepository;

    @Transactional
    @Scheduled(cron = "0 */3 * * * *", zone = "Asia/Seoul")
    public void checkAndDeleteExpiredMatchings() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("--------- 자동 매칭 취소 처리 시작: {} ---------", currentTime);

        List<LetterTemporaryMatching> expiredMatchings = letterTemporaryMatchingRepository.findAll().stream()
                .filter(m -> m.getReplyDeadLine().isBefore(LocalDateTime.now()) ||
                        m.getReplyDeadLine().isEqual(LocalDateTime.now()))
                .collect(Collectors.toList());

        log.info("찾은 만료 매칭 수: {}", expiredMatchings.size());

        for (LetterTemporaryMatching matching : expiredMatchings) {
            log.info("매칭 ID: {}, 기한: {}, 현재: {}",
                    matching.getId(), matching.getReplyDeadLine(), LocalDateTime.now());

            // 편지 처리 - LetterType을 RANDOM으로 변경
            letterRepository.findById(matching.getLetterId())
                    .ifPresent(letter -> {
                        letter.updateLetterType(LetterType.RANDOM);
                        letterRepository.save(letter);
                    });
        }

        // 매칭 삭제 처리
        if (!expiredMatchings.isEmpty()) {
            letterTemporaryMatchingRepository.deleteAll(expiredMatchings);
            log.info("매칭 {}개 삭제 완료", expiredMatchings.size());
        } else {
            log.info("삭제할 만료 매칭이 없습니다.");
        }
        log.info("--------- 자동 매칭 취소 처리 완료 ---------");
    }
}
