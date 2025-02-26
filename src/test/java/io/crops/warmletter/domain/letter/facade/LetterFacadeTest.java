package io.crops.warmletter.domain.letter.facade;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.service.LetterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LetterFacadeTest {


    @Mock
    private LetterService letterService;

    @InjectMocks
    private LetterFacade letterFacade;


    @Test
    public void createLetter_shouldDelegateToLetterService() {
        CreateLetterRequest request = CreateLetterRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        LetterResponse expectedResponse = LetterResponse.builder()
                .letterId(1L)
                .title("제목")
                .build();

        // letterService.createLetter(request)가 expectedResponse를 반환하도록 설정
        when(letterService.createLetter(request)).thenReturn(expectedResponse);

        // when: Facade의 createLetter 호출
        LetterResponse actualResponse = letterFacade.createLetter(request);

        // then: 반환된 값이 예상한 값과 동일한지, 그리고 내부 서비스의 메서드가 한 번 호출되었는지 검증
        assertEquals(expectedResponse, actualResponse);
        verify(letterService, times(1)).createLetter(request);
    }
}