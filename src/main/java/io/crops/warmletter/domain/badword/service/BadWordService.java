package io.crops.warmletter.domain.badword.service;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadWordService {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가

    public void savebadWord(CreateBadWordRequest request) {
        String word = request.getWord();

        boolean exists = badWordRepository.existsByWord(word);
        if (exists) {
            throw new DuplicateBadWordException();
        }

        BadWord badWord = BadWord.builder()
                .word(word)
                .isUsed(true)
                .build();

        badWordRepository.save(badWord);

        redisTemplate.opsForSet().add("banned_words", word);
    }

}
