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
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.Trie;
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

    private static final String BAD_WORD_PATTERN = "[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\\s]";


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
                    // 여기서 id 변수를 선언합니다.
                    String id = e.getKey().toString();
                    map.put("id", id);
                    map.put("word", e.getValue().toString());
                    // DB에서 조회한 isUsed 값을 포함 (없으면 기본값 false)
                    Optional<BadWord> optional = badWordRepository.findById(Long.valueOf(id));
                    map.put("isUsed", optional.map(bw -> Boolean.toString(bw.isUsed())).orElse("false"));
                    return map;
                })
                .collect(Collectors.toList());
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

    @Transactional
    public void deleteBadWord(Long id) {
        BadWord badWord = badWordRepository.findById(id)
                .orElseThrow(BadWordNotFoundException:: new);

        badWordRepository.delete(badWord);

        redisTemplate.opsForHash().delete(BAD_WORD_KEY, id.toString());
    }



    //필터링
    public void validateText(String text) {
        // Redis에서 금칙어 데이터를 불러옴
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(BAD_WORD_KEY);

        // 금칙어 목록을 Set으로 변환
        Set<String> badWords = entries.values().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        // 아호코라식 트리(Trie) 생성 (단어 단위 매칭, 대소문자 구분 없이)
        Trie.TrieBuilder builder = Trie.builder().onlyWholeWords().caseInsensitive();
        for (String badWord : badWords) {
            builder.addKeyword(badWord);
        }
        Trie badWordTrie = builder.build();

        // 텍스트에서 특수문자만 제거하고, 공백은 그대로 유지 (공백 덕분에 단어가 분리됨)
        String sanitizedText = text.replaceAll(BAD_WORD_PATTERN, "");

        // 아호코라식 트리로 텍스트를 검사
        Collection<Emit> matches = badWordTrie.parseText(sanitizedText);
        // 금칙어가 발견되면 예외를 던짐
        if (!matches.isEmpty()) {
            throw new BadWordContainsException();
        }
    }


}
