package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.response.SharePostResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.repository.ShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharePostServiceTest {

    @Mock
    private ShareRepository shareRepository;

    @InjectMocks
    private SharePostService sharePostService;

    private SharePost sharePost1;
    private SharePost sharePost2;

    @BeforeEach
    void setUp() {
        // 테스트에서 사용할 객체만 생성
        sharePost1 = new SharePost(1L, "게시글1", "to share my post");
        sharePost2 = new SharePost(2L, "게시글2", "to share my post1");
    }

    @Test
    @DisplayName("페이징된 공유 게시글 목록 반환")
    void getAllposts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<SharePost> posts = List.of(sharePost1, sharePost2);
        Page<SharePost> sharePostPage = new PageImpl<>(posts, pageable, posts.size());

        when(shareRepository.findAll(pageable)).thenReturn(sharePostPage);

        // when
        Page<SharePostResponse> result = sharePostService.getAllposts(pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).getTitle()).isEqualTo("게시글1"),  // 실제 데이터와 일치하도록 수정
                () -> assertThat(result.getContent().get(1).getTitle()).isEqualTo("게시글2"),  // 실제 데이터와 일치하도록 수정
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getNumber()).isEqualTo(0)
        );

        verify(shareRepository).findAll(pageable);
    }
}