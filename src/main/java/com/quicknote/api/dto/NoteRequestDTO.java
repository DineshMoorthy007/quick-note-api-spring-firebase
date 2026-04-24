package com.quicknote.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequestDTO {

    private String id;
    private String title;
    private String content;
    private String userId;
    private String createdAt;

    @JsonProperty("isPinned")
    private Boolean isPinned;
}
