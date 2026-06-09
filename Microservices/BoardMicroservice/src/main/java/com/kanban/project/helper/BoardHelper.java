package com.kanban.project.helper;

import com.kanban.project.entity.Board;
import com.kanban.project.error.ForbiddenException;
import com.kanban.project.error.model.ExceptionMessage;
import org.springframework.stereotype.Component;

@Component
public class BoardHelper {
    public void verifyAccess(Board board, Long requesterId, boolean requireOwner) {
        boolean isOwner = board.getOwnerId().equals(requesterId);
        boolean isCollaborator = board.getCollaboratorIds().contains(requesterId);

        if (requireOwner &&!isOwner) {
            throw new ForbiddenException(ExceptionMessage.FORBIDDEN);
        }

        if (!isOwner &&!isCollaborator) {
            throw new ForbiddenException(ExceptionMessage.FORBIDDEN);
        }
    }
}
