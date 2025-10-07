package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createOrUpdateUser(String email, String name, String picture, String supabaseUserId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setPicture(picture);
            user.setSupabaseUserId(supabaseUserId);
            return userRepository.save(user);
        } else {
            User newUser = new User(email, name, picture, supabaseUserId);
            // First user becomes admin
            if (userRepository.count() == 0) {
                newUser.setRole(User.Role.ADMIN);
            }
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId);
    }

    public User createUser(String email, String name, String picture, User.Role role) {
        User newUser = new User(email, name, picture, null);
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

    public User promoteToAdmin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(User.Role.ADMIN);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
