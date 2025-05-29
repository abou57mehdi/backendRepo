package com.ESI.CareerBooster.cvGenerator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "start_date")
    private String startDate;
    
    @Column(name = "end_date")
    private String endDate;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "experience_achievements", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();
}
