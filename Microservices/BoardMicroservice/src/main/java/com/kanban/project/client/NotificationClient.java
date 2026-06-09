package com.kanban.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:8083")
public interface NotificationClient {
    @PostMapping("/api/internal/notifications/boards/{boardId}")
    void shareFullBoardUpdate(@PathVariable("boardId") Long boardId, @RequestBody Object fullBoardPayload);
}