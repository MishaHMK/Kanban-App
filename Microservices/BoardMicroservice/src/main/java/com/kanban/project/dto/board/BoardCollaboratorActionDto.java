package com.kanban.project.dto.board;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kanban.project.data.CollaboratorAction;

public record BoardCollaboratorActionDto(
        @JsonProperty("collaborator_id")
        Long collaboratorId,
        CollaboratorAction action) {}
