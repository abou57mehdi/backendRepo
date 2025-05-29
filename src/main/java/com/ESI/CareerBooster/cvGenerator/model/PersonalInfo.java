package com.ESI.CareerBooster.cvGenerator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "personal_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String title;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "linkedin")
    private String linkedin;

    @Column(name = "github")
    private String github;

    @Column(name = "website")
    private String website;

    @Column(name = "profile_picture")
    private String profilePicture;
}
