package com.quicknote.api.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.quicknote.api.dto.AuthRequestDTO;
import com.quicknote.api.dto.AuthResponseDTO;
import com.quicknote.api.exception.ResourceNotFoundException;
import com.quicknote.api.model.User;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String USERS_COLLECTION = "users";

    private final Firestore firestore;

    @Autowired
    public AuthService(Firestore firestore) {
        this.firestore = firestore;
    }

    public AuthResponseDTO register(AuthRequestDTO request) {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, request.getUsername(), request.getPassword());

        DocumentReference docRef = firestore.collection(USERS_COLLECTION).document(userId);
        waitFor(docRef.set(user));

        return new AuthResponseDTO(generateMockToken(), userId, request.getUsername());
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        ApiFuture<QuerySnapshot> future = firestore.collection(USERS_COLLECTION)
            .whereEqualTo("username", request.getUsername())
            .limit(1)
            .get();

        QuerySnapshot snapshot = waitFor(future);
        List<QueryDocumentSnapshot> documents = snapshot.getDocuments();

        if (documents.isEmpty()) {
            throw new ResourceNotFoundException("User is not registered. Please register first.");
        }

        QueryDocumentSnapshot document = documents.getFirst();
        User user = document.toObject(User.class);

        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return new AuthResponseDTO(generateMockToken(), document.getId(), user.getUsername());
    }

    private String generateMockToken() {
        return "mock-token-" + UUID.randomUUID();
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
