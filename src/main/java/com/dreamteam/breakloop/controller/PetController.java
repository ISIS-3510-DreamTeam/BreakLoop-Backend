package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Pet;
import com.dreamteam.breakloop.model.PetState;
import com.dreamteam.breakloop.service.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    /** All pet types with the image path for each mood. */
    @GetMapping("/pets")
    public List<Pet> getPets() {
        return service.getCatalog();
    }

    @GetMapping("/users/{uid}/pet")
    public PetState getPetState(@PathVariable String uid) {
        return service.getState(uid);
    }

    @PutMapping("/users/{uid}/pet")
    public void updatePetState(@PathVariable String uid, @RequestBody PetState state) {
        service.saveState(uid, state);
    }
}