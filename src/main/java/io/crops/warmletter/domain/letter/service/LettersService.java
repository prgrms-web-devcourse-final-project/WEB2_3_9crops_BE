package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.repository.LettersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LettersService {

    private final LettersRepository lettersRepository;
}
