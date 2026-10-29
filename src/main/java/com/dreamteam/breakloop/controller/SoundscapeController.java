package com.dreamteam.breakloop.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/soundscapes")
public class SoundscapeController {

    private static final Path AUDIO_DIRECTORY =
            Paths.get("audio").toAbsolutePath().normalize();

    private static final Map<String, String> FILES = Map.of(
            "hearth", "hearth.mp3",
            "rain", "rain.mp3"
    );

    @GetMapping("/{name}")
    public ResponseEntity<Resource> getSoundscape(
            @PathVariable String name
    ) {
        String filename = FILES.get(name.toLowerCase());

        if (filename == null) {
            return ResponseEntity.notFound().build();
        }

        Path file = AUDIO_DIRECTORY.resolve(filename).normalize();

        if (!file.startsWith(AUDIO_DIRECTORY)
                || !file.toFile().isFile()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\""
                )
                .body(resource);
    }
}