package com.dreamteam.breakloop.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/soundscapes")
public class SoundscapeController {

    private static final Map<String, String> FILES = Map.of(
            "hearth", "audio/hearth.mp3",
            "rain", "audio/rain.mp3");

    @GetMapping("/{name}")
    public ResponseEntity<Resource> getSoundscape(
            @PathVariable String name) throws IOException {

        String path = FILES.get(name.toLowerCase());

        if (path == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }
}