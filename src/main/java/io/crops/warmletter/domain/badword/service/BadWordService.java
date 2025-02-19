package io.crops.warmletter.domain.badword.service;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.entity.BadWord;
import io.crops.warmletter.domain.badword.exception.BadWordNotFoundException;
import io.crops.warmletter.domain.badword.exception.DuplicateBadWordException;
import io.crops.warmletter.domain.badword.repository.BadWordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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

        badWord.updateStatus(request.getIsUsed());

        if (request.getIsUsed()) {
            redisTemplate.opsForSet().add("bad_word", badWord.getWord());
        } else {
            redisTemplate.opsForSet().remove("bad_word", badWord.getWord());
        }
    }


}
