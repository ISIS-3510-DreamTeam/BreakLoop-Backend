package com.dreamteam.breakloop.service;
import com.dreamteam.breakloop.model.User;
import com.dreamteam.breakloop.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, Firestore firestore) {
        this.userRepository = userRepository;
    }

    public User getUser(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    
}