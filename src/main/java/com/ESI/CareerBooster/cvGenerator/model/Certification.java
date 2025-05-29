package com.ESI.CareerBooster.cvGenerator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "certification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "issuer")
    private String issuer;
    
    @Column(name = "date")
    private String date;
    
    @Column(name = "expiry_date")
    private String expiryDate;
    
    @Column(name = "credential_id")
    private String credentialId;
    
    @Column(name = "credential_url")
    private String credentialUrl;
}
