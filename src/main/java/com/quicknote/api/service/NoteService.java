package com.quicknote.api.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.quicknote.api.dto.NoteRequestDTO;
import com.quicknote.api.exception.ResourceNotFoundException;
import com.quicknote.api.model.Note;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private static final String NOTES_COLLECTION = "notes";

    private final Firestore firestore;

    @Autowired
    public NoteService(Firestore firestore) {
        this.firestore = firestore;
    }

    public Note createNote(NoteRequestDTO request) {
        String noteId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        Boolean isPinned = request.getIsPinned() != null ? request.getIsPinned() : Boolean.FALSE;

        Note note = new Note(
            noteId,
            request.getTitle(),
            request.getContent(),
            request.getUserId(),
            createdAt,
            isPinned
        );

        DocumentReference docRef = firestore.collection(NOTES_COLLECTION).document(noteId);
        waitFor(docRef.set(note));

        return note;
    }

    public List<Note> getNotesByUser(String userId) {
        ApiFuture<QuerySnapshot> future = firestore.collection(NOTES_COLLECTION)
            .whereEqualTo("userId", userId)
            .get();

        QuerySnapshot snapshot = waitFor(future);
        List<Note> notes = new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            Note note = document.toObject(Note.class);
            if (note != null) {
                note.setId(document.getId());
                notes.add(note);
            }
        }

        return notes;
    }

    public Note updateNote(String noteId, NoteRequestDTO request) {
        DocumentReference docRef = firestore.collection(NOTES_COLLECTION).document(noteId);
        DocumentSnapshot snapshot = waitFor(docRef.get());

        if (!snapshot.exists()) {
            throw new ResourceNotFoundException("Note not found with id: " + noteId);
        }

        Note existing = snapshot.toObject(Note.class);
        if (existing == null) {
            throw new ResourceNotFoundException("Note not found with id: " + noteId);
        }

        existing.setId(noteId);
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            existing.setUserId(request.getUserId());
        }
        if (request.getIsPinned() != null) {
            existing.setIsPinned(request.getIsPinned());
        }
        if (request.getCreatedAt() != null && !request.getCreatedAt().isBlank()) {
            existing.setCreatedAt(request.getCreatedAt());
        }

        waitFor(docRef.set(existing));
        return existing;
    }

    public Note togglePinNote(String noteId) {
        DocumentReference docRef = firestore.collection(NOTES_COLLECTION).document(noteId);
        DocumentSnapshot snapshot = waitFor(docRef.get());

        if (!snapshot.exists()) {
            throw new ResourceNotFoundException("Note not found with id: " + noteId);
        }

        Note note = snapshot.toObject(Note.class);
        if (note == null) {
            throw new ResourceNotFoundException("Note not found with id: " + noteId);
        }

        note.setId(noteId);
        note.setIsPinned(note.getIsPinned() == null ? Boolean.TRUE : !note.getIsPinned());

        waitFor(docRef.set(note));
        return note;
    }

    public void deleteNote(String noteId) {
        DocumentReference docRef = firestore.collection(NOTES_COLLECTION).document(noteId);
        waitFor(docRef.delete());
    }

    private <T> T waitFor(ApiFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Firestore operation.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Firestore operation failed.", e);
        }
    }
}
