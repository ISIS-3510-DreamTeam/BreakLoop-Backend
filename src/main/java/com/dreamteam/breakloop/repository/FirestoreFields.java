package com.dreamteam.breakloop.repository;

import com.google.cloud.firestore.DocumentSnapshot;

import java.util.Map;

/** Null-safe reads and writes for the hand-mapped Firestore documents. */
final class FirestoreFields {

    static final int BATCH_SIZE = 500;

    private FirestoreFields() {
    }

    static <E extends Enum<E>> E getEnum(
            DocumentSnapshot document, String field, Class<E> type) {

        String value = document.getString(field);

        return value != null ? Enum.valueOf(type, value) : null;
    }

    static void put(Map<String, Object> map, String key, Object value) {

        if (value instanceof Enum<?> e) {
            value = e.name();
        }

        map.put(key, value);
    }
}
