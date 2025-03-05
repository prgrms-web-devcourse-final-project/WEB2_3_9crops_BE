package io.crops.warmletter.global.schedule;


import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AutoCancelSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;
    private final LetterRepository letterRepository;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void runAutoMatchingCancelJob()throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String date = dateFormat.format(new Date());

        log.info("---------자동 매칭 취소 처리 시작 : {}---------", date);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("autoCancelJob"), jobParameters);
    }

//    @Transactional
//    @Scheduled(cron = "0 */3 * * * *", zone = "Asia/Seoul")
//    public void checkAndDeleteExpiredMatchings() {
//        log.info("수동 만료 처리 시작");
//        List<LetterTemporaryMatching> expiredMatchings =
//                letterTemporaryMatchingRepository.findAll().stream()
//                        .filter(m -> m.getReplyDeadLine().isBefore(LocalDateTime.now()) ||
//                                m.getReplyDeadLine().isEqual(LocalDateTime.now()))
//                        .collect(Collectors.toList());
//
//        log.info("찾은 만료 매칭 수: {}", expiredMatchings.size());
//
//        for (LetterTemporaryMatching matching : expiredMatchings) {
//            log.info("매칭 ID: {}, 기한: {}, 현재: {}",
//                    matching.getId(), matching.getReplyDeadLine(), LocalDateTime.now());
//
//            // 편지 처리
//            letterRepository.findById(matching.getLetterId())
//                    .ifPresent(letter -> {
//                        letter.updateLetterType(LetterType.RANDOM);
//                        letterRepository.save(letter);
//                        log.info("편지 타입 변경됨: {}", letter.getId());
//                    });
//        }
//
//        // 삭제 처리
//        if (!expiredMatchings.isEmpty()) {
//            letterTemporaryMatchingRepository.deleteAll(expiredMatchings);
//            log.info("매칭 {} 개 삭제됨", expiredMatchings.size());
//        }
//    }
}
