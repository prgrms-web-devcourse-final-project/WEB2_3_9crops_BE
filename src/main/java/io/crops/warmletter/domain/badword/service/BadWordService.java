package io.crops.warmletter.domain.badword.service;


import io.crops.warmletter.domain.badword.dto.request.BadWordRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.DuplicateBannedWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadWordService {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가

    public void saveModerationWord(BadWordRequest request) {
        String word = request.getWord();

        boolean exists = badWordRepository.existsByWord(word);
        if (exists) {
            throw new DuplicateBannedWordException(); // 변경
        }

        BadWord badWord = BadWord.builder()
                .word(word)
                .build();

        badWordRepository.save(badWord);

        redisTemplate.opsForSet().add("banned_words", word);
    }

}
