package com.app.municipio.modules.events.models;
 
import jakarta.persistence.*;
 
import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;

import lombok.ToString;              

import lombok.EqualsAndHashCode;   
 
import java.time.LocalDate;

import java.util.HashSet;

import java.util.Set;
 
@Data

@AllArgsConstructor

@NoArgsConstructor

@Entity

@Builder

@Table(name = "events")

public class Event {
 
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id_event")

    private Long id;
 
    @Column(nullable = false, length = 120)

    private String name;
 
    @Column(nullable = false, length = 80)

    private String type;
 
    @Column(name = "date_start", nullable = false)

    private LocalDate dateStart;
 
    @Column(name = "date_end", nullable = false)

    private LocalDate dateEnd;
 
    @Column(length = 500)

    private String description;
 
    @Column(length = 180)

    private String direction;
 
    @Column(length = 120)

    private String location;
 
    @Column(length = 255)

    private String link;
 
    @Column(nullable = false)

    private Boolean state;
 
    @Builder.Default

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)

    @ToString.Exclude              

    @EqualsAndHashCode.Exclude     

    private Set<EventContact> contact = new HashSet<>();
 
    @Builder.Default

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)

    @ToString.Exclude              

    @EqualsAndHashCode.Exclude     

    private Set<EventServiceEntity> services = new HashSet<>();

    @Builder.Default
@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Set<EventImage> images = new HashSet<>();

}
 