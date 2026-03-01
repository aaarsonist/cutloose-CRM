package com.barbershop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;
@Entity
@Getter
@Setter
@Table(name = "masters")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "master")
    @JsonIgnore
    private List<Timetable> appointments;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<WorkSchedule> workSchedules;
}