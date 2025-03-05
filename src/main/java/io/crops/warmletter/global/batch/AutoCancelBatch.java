package io.crops.warmletter.global.batch;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.crops.warmletter.domain.letter.enums.LetterType;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.letter.repository.LetterTemporaryMatchingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class AutoCancelBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final LetterTemporaryMatchingRepository letterTemporaryMatchingRepository;
    private final LetterRepository letterRepository;

    @Bean
    public Job autoCancelJob() {
        return new JobBuilder("autoCancelJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(autoCancelStep())
                .build();
    }

    @Bean
    public Step autoCancelStep(){
        return new StepBuilder("autoCancelStep", jobRepository)
                .<LetterTemporaryMatching, LetterTemporaryMatching>chunk(5, platformTransactionManager)
                .reader(autoCancelReader())
                .processor(autoCancelProcessor())
                .writer(autoCancelWriter())
                .build();
    }

    public RepositoryItemReader<LetterTemporaryMatching> autoCancelReader() {
        LocalDateTime now = LocalDateTime.now();
        log.info("현재 시간: {}, 포맷팅된 시간: {}", now, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 직접 조회하여 문제 파악
        List<LetterTemporaryMatching> allMatchings = letterTemporaryMatchingRepository.findAll();
        log.info("전체 매칭 수: {}", allMatchings.size());

        for(LetterTemporaryMatching matching : allMatchings) {
            log.info("매칭 ID: {}, 기한: {}, 현재 시간 이전? {}",
                    matching.getId(),
                    matching.getReplyDeadLine(),
                    matching.getReplyDeadLine().isBefore(now));
        }

        RepositoryItemReader<LetterTemporaryMatching> reader = new RepositoryItemReaderBuilder<LetterTemporaryMatching>()
                .name("autoCancelReader")
                .pageSize(5)
                .methodName("findExpiredMatchings")
                .arguments(List.of(now))
                .repository(letterTemporaryMatchingRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
        // 조회된 데이터 로깅
        return reader;
    }

//    @Bean
//    public RepositoryItemReader<LetterTemporaryMatching> autoCancelReader() {
//        LocalDateTime now = LocalDateTime.now();
//        log.info("현재 시간: {}, 포맷팅된 시간: {}", now, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//
//        // 시간 비교 테스트를 위한 직접 조회
//        List<LetterTemporaryMatching> expiredMatches = letterTemporaryMatchingRepository.findAll()
//                .stream()
//                .filter(match -> {
//                    boolean isExpired = match.getReplyDeadLine().isBefore(now) || match.getReplyDeadLine().isEqual(now);
//                    log.info("매칭 ID: {}, 기한: {}, 현재시간: {}, 만료여부: {}",
//                            match.getId(), match.getReplyDeadLine(), now, isExpired);
//                    return isExpired;
//                })
//                .collect(Collectors.toList());
//
//        log.info("만료된 매칭 수(직접 조회): {}", expiredMatches.size());
//
//        return new RepositoryItemReaderBuilder<LetterTemporaryMatching>()
//                .name("autoCancelReader")
//                .repository(letterTemporaryMatchingRepository)
//                .methodName("findExpiredMatchings")
//                .arguments(now)
//                .pageSize(10)
//                .sorts(Map.of("id", Sort.Direction.ASC))
//                .build();
//    }

    @Bean
    public ItemProcessor<LetterTemporaryMatching, LetterTemporaryMatching> autoCancelProcessor() {
        return letterTempMatching -> {
            log.info("처리 중인 만료 매칭: ID={}, 기한={}",
                    letterTempMatching.getId(), letterTempMatching.getReplyDeadLine());

                    // 연관된 편지의 타입을 RANDOM으로 변경
            Letter letter = letterRepository.findById(letterTempMatching.getLetterId())
                    .orElse(null);

            if (letter != null) {
                letter.updateLetterType(LetterType.RANDOM);
                letterRepository.save(letter);
            }else{
                log.error("[ERROR]: 편지 아이디: {}를 찾을 수 없습니다", letterTempMatching.getLetterId());
            }
            return letterTempMatching;
        };
    }

    @Bean
    public RepositoryItemWriter<LetterTemporaryMatching> autoCancelWriter() {
        RepositoryItemWriter<LetterTemporaryMatching> writer = new RepositoryItemWriter<>();
        writer.setRepository(letterTemporaryMatchingRepository);
        writer.setMethodName("delete");
        return writer;
    }
}
