package com.kanban.project.NotificationCenter.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/notifications")
public class NotificationController {
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/boards/{boardId}")
    public void broadcastBoardUpdate(@PathVariable Long boardId, @RequestBody Object payload) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId, payload);
    }
}