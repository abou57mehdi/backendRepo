package com.ESI.CareerBooster.cvGenerator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "education")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "institution")
    private String institution;
    
    @Column(name = "degree")
    private String degree;
    
    @Column(name = "field_of_study")
    private String fieldOfStudy;
    
    @Column(name = "start_date")
    private String startDate;
    
    @Column(name = "end_date")
    private String endDate;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "gpa")
    private Double gpa;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
