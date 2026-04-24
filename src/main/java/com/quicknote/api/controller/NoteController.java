package com.quicknote.api.controller;

import com.quicknote.api.dto.NoteRequestDTO;
import com.quicknote.api.model.Note;
import com.quicknote.api.service.NoteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ResponseEntity<List<Note>> getNotesByUser(@RequestParam String userId) {
        return ResponseEntity.ok(noteService.getNotesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody NoteRequestDTO request) {
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
    public ResponseEntity<Void> deleteNote(@PathVariable("id") String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
