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

    @Scheduled(cron = "10 */1 * * * *", zone = "Asia/Seoul")
    public void runAutoMatchingCancelJob()throws Exception {
        log.info("[runAutoMatchingCancelJob] 실행");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String date = dateFormat.format(new Date());

        log.info("---------자동 매칭 취소 처리 시작 : {}---------", date);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("autoCancelJob"), jobParameters);
    }
}
