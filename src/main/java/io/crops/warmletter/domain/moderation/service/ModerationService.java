package io.crops.warmletter.domain.moderation.service;


import io.crops.warmletter.domain.moderation.dto.request.ModerationRequest;
import io.crops.warmletter.domain.moderation.entity.Moderation;
import io.crops.warmletter.domain.moderation.exception.DuplicateBannedWordException;
import io.crops.warmletter.domain.moderation.repository.ModerationRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ModerationRepository moderationRepository;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가

    public void saveModerationWord(ModerationRequest request) {
        String word = request.getWord();

        boolean exists = moderationRepository.existsByWord(word);
        if (exists) {
            throw new DuplicateBannedWordException(); // 변경
        }

        Moderation moderation = Moderation.builder()
                .word(word)
                .build();

        moderationRepository.save(moderation);

        redisTemplate.opsForSet().add("banned_words", word);
    }

}
