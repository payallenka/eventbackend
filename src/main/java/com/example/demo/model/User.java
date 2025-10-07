package com.example.demo.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String picture;
    
    @Column(name = "supabase_user_id")
    private String supabaseUserId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public enum Role {
        ADMIN, USER
    }

    // Constructors
    public User() {}

    public User(String email, String name, String picture, String supabaseUserId) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.supabaseUserId = supabaseUserId;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    
    public String getSupabaseUserId() { return supabaseUserId; }
    public void setSupabaseUserId(String supabaseUserId) { this.supabaseUserId = supabaseUserId; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
