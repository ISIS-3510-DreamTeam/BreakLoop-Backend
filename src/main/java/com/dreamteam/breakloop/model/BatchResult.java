package com.dreamteam.breakloop.model;

import java.util.List;

/** Outcome of a batch upsert: items stored and ids ignored as stale. */
public record BatchResult(int written, List<String> skipped) {
}
