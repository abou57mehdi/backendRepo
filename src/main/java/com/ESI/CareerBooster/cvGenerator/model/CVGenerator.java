package com.ESI.CareerBooster.cvGenerator.model;

import com.ESI.CareerBooster.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cv_generator")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVGenerator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_info_id")
    private PersonalInfo personalInfo;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @ElementCollection
    @CollectionTable(name = "cv_skills", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id")
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id")
    private List<Education> education = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id")
    private List<Project> projects = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id")
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id")
    private List<Language> languages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cv_hobbies", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "hobby")
    private List<String> hobbies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cv_volunteering", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "volunteering")
    private List<String> volunteering = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cv_awards", joinColumns = @JoinColumn(name = "cv_id"))
    @Column(name = "award")
    private List<String> awards = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "template")
    private String template;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
