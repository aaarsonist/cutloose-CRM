package com.barbershop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(name = "provider")
    private String provider; // "LOCAL" или "GOOGLE"
    @Column(name = "provider_id")
    private String providerId; // ID от Google

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "bookedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Timetable> bookings;
}