package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventPostController {
    private final EventPostService eventPostService;

    @PostMapping("/admin/event-posts")
    public void createEventPost(@RequestBody CreateEventPostRequest createEventPostRequest){
        eventPostService.createEventPost(createEventPostRequest);
    }
}
