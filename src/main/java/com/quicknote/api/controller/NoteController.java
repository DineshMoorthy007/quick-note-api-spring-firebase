package com.quicknote.api.controller;

import com.quicknote.api.dto.NoteRequestDTO;
import com.quicknote.api.model.Note;
import com.quicknote.api.service.NoteService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ResponseEntity<List<Note>> getNotesByUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String userId = resolveUserIdFromAuthorization(authorization);
        return ResponseEntity.ok(noteService.getNotesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestBody NoteRequestDTO request
    ) {
        request.setUserId(resolveUserIdFromAuthorization(authorization));
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable("id") String id, @RequestBody NoteRequestDTO request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<Note> togglePinNote(@PathVariable("id") String id) {
        return ResponseEntity.ok(noteService.togglePinNote(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable("id") String id) {
        noteService.deleteNote(id);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Note deleted successfully");
        response.put("id", id);

        return ResponseEntity.ok(response);
    }

    private String resolveUserIdFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        }

        String token = authorization.substring(7).trim();
        String prefix = "mock-token-";

        if (!token.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid authentication token.");
        }

        String remainder = token.substring(prefix.length());
        int lastDash = remainder.lastIndexOf('-');
        if (lastDash <= 0) {
            throw new IllegalArgumentException("Invalid authentication token.");
        }

        return remainder.substring(0, lastDash);
    }
}
