package io.crops.warmletter.domain.badword.service;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.BadWordContainsException;
import io.crops.warmletter.domain.badword.exception.BadWordNotFoundException;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BadWordService {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가

    public void createBadWord(CreateBadWordRequest request) {
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

        redisTemplate.opsForSet().add("bad_word", word);
    }


    @Transactional
    public void updateBadWordStatus(Long badWordId, UpdateBadWordStatusRequest request) {
        BadWord badWord = badWordRepository.findById(badWordId)
                .orElseThrow(BadWordNotFoundException::new);


        badWord.updateStatus(request.isUsed());

        if (request.isUsed()) {
            redisTemplate.opsForSet().add("bad_word", badWord.getWord());
        } else {
            redisTemplate.opsForSet().remove("bad_word", badWord.getWord());
        }
    }

    //필터링
    public void validateText(String text) {
        Set<String> badWords = redisTemplate.opsForSet().members("bad_word");

        // 입력 문장에서 특수문자, 공백 제거
        String sanitizedText = text.replaceAll("[^가-힣a-zA-Z0-9]", "");

        for (String badWord : badWords) {
            // 금칙어도 혹시 특수문자 있을 수 있으니까 정제
            String sanitizedBadWord = badWord.replaceAll("[^가-힣a-zA-Z0-9]", "");

            if (sanitizedText.contains(sanitizedBadWord)) {
                throw new BadWordContainsException();
            }
        }
    }



}
