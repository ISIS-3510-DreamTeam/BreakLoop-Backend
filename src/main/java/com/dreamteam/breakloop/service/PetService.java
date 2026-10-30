package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.Pet;
import com.dreamteam.breakloop.model.PetState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetService {

    private final Firestore firestore;
    private final List<Pet> catalog;

    public PetService(Firestore firestore, ObjectMapper objectMapper) throws IOException {
        this.firestore = firestore;

        // The catalog is read once when the server starts.
        try (InputStream input = new ClassPathResource("catalog/pets.json").getInputStream()) {
            this.catalog = objectMapper.readValue(input, new TypeReference<List<Pet>>() {});
        }
    }

    public List<Pet> getCatalog() {
        return catalog;
    }

    public PetState getState(String uid) {
        try {
            DocumentSnapshot document = firestore.collection("users").document(uid).get().get();

            PetState state = new PetState();
            state.setActivePetId(document.getString("activePetId"));
            state.setPetMood(document.getString("petMood"));
            Long updatedAt = document.getLong("petUpdatedAt");
            state.setPetUpdatedAt(updatedAt != null ? updatedAt : 0L);
            return state;

        } catch (Exception e) {
            throw new RuntimeException("Error reading pet state", e);
        }
    }

    public void saveState(String uid, PetState state) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("activePetId", state.getActivePetId());
        fields.put("petMood", state.getPetMood());
        fields.put("petUpdatedAt", state.getPetUpdatedAt());

        try {
            // merge() only touches these three fields; email, createdAt, etc. stay as they are.
            firestore.collection("users").document(uid)
                    .set(fields, SetOptions.merge())
                    .get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving pet state", e);
        }
    }
}