package io.crops.warmletter.domain.badword.service;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.dto.response.UpdateBadWordResponse;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.BadWordContainsException;
import io.crops.warmletter.domain.badword.exception.BadWordNotFoundException;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadWordService {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가

    private static final String BAD_WORD_KEY = "bad_word";
    private static final String BAD_WORD_PATTERN = "[^가-힣a-zA-Z0-9]";

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

        redisTemplate.opsForHash().put(BAD_WORD_KEY, badWord.getId().toString(), word);
    }


    @Transactional
    public void updateBadWordStatus(Long badWordId, UpdateBadWordStatusRequest request) {
        BadWord badWord = badWordRepository.findById(badWordId)
                .orElseThrow(BadWordNotFoundException::new);
        badWord.updateStatus(request.isUsed());

        if (request.isUsed()) {
            redisTemplate.opsForHash().put(BAD_WORD_KEY,badWordId.toString(), badWord.getWord());
        } else {
            redisTemplate.opsForHash().delete(BAD_WORD_KEY,badWordId.toString(), badWord.getWord());
        }
    }

    public List<Map<String, String>> getBadWords() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(BAD_WORD_KEY);
        return entries.entrySet().stream()
                .map(e -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", e.getKey().toString());
                    map.put("word", e.getValue().toString());
                    return map;
                })
                .toList();
    }

    @Transactional
    public UpdateBadWordResponse updateBadWord(Long id, UpdateBadWordRequest request) {
        BadWord badWord = badWordRepository.findById(id)
                .orElseThrow(BadWordNotFoundException::new);

        String newWord = request.getWord();

        if (!badWord.getWord().equals(newWord) && badWordRepository.existsByWord(newWord)) {
            throw new DuplicateBadWordException();
        }

        badWord.updateWord(newWord);
        badWordRepository.save(badWord);

        if (badWord.isUsed()) {
            redisTemplate.opsForHash().put(BAD_WORD_KEY, badWord.getId().toString(), badWord.getWord());
        }
        return new UpdateBadWordResponse(badWord.getWord());
    }






    //필터링
    public void validateText(String text) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(BAD_WORD_KEY);

        Set<String> badWords = entries.values().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        String sanitizedText = text.replaceAll(BAD_WORD_PATTERN, "");


        for (String badWord : badWords) {
            // 금칙어도 혹시 특수문자 있을 수 있으니까 정제
            String sanitizedBadWord = badWord.replaceAll("[^가-힣a-zA-Z0-9]", "");

            if (sanitizedText.contains(sanitizedBadWord)) {
                throw new BadWordContainsException();
            }
        }
    }
}
