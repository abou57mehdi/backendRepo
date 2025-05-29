package com.ESI.CareerBooster.cvGenerator.dto;

import com.ESI.CareerBooster.cvGenerator.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVGeneratorDTO {
    private Long id;
    private PersonalInfo personalInfo;
    private String summary;
    private List<String> skills = new ArrayList<>();
    private List<Experience> experiences = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Certification> certifications = new ArrayList<>();
    private List<Language> languages = new ArrayList<>();
    private List<String> hobbies = new ArrayList<>();
    private List<String> volunteering = new ArrayList<>();
    private List<String> awards = new ArrayList<>();
    private String template;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Convert DTO to Entity
    public CVGenerator toEntity() {
        CVGenerator cv = new CVGenerator();
        cv.setId(this.id);
        cv.setPersonalInfo(this.personalInfo);
        cv.setSummary(this.summary);
        cv.setSkills(this.skills != null ? this.skills : new ArrayList<>());
        cv.setExperiences(this.experiences != null ? this.experiences : new ArrayList<>());
        cv.setEducation(this.education != null ? this.education : new ArrayList<>());
        cv.setProjects(this.projects != null ? this.projects : new ArrayList<>());
        cv.setCertifications(this.certifications != null ? this.certifications : new ArrayList<>());
        cv.setLanguages(this.languages != null ? this.languages : new ArrayList<>());
        cv.setHobbies(this.hobbies != null ? this.hobbies : new ArrayList<>());
        cv.setVolunteering(this.volunteering != null ? this.volunteering : new ArrayList<>());
        cv.setAwards(this.awards != null ? this.awards : new ArrayList<>());
        cv.setTemplate(this.template);
        cv.setCreatedDate(this.createdDate);
        cv.setLastModifiedDate(this.lastModifiedDate);
        return cv;
    }

    // Convert Entity to DTO
    public static CVGeneratorDTO fromEntity(CVGenerator cv) {
        CVGeneratorDTO dto = new CVGeneratorDTO();
        dto.setId(cv.getId());
        dto.setPersonalInfo(cv.getPersonalInfo());
        dto.setSummary(cv.getSummary());
        dto.setSkills(cv.getSkills() != null ? cv.getSkills() : new ArrayList<>());
        dto.setExperiences(cv.getExperiences() != null ? cv.getExperiences() : new ArrayList<>());
        dto.setEducation(cv.getEducation() != null ? cv.getEducation() : new ArrayList<>());
        dto.setProjects(cv.getProjects() != null ? cv.getProjects() : new ArrayList<>());
        dto.setCertifications(cv.getCertifications() != null ? cv.getCertifications() : new ArrayList<>());
        dto.setLanguages(cv.getLanguages() != null ? cv.getLanguages() : new ArrayList<>());
        dto.setHobbies(cv.getHobbies() != null ? cv.getHobbies() : new ArrayList<>());
        dto.setVolunteering(cv.getVolunteering() != null ? cv.getVolunteering() : new ArrayList<>());
        dto.setAwards(cv.getAwards() != null ? cv.getAwards() : new ArrayList<>());
        dto.setTemplate(cv.getTemplate());
        dto.setCreatedDate(cv.getCreatedDate());
        dto.setLastModifiedDate(cv.getLastModifiedDate());
        return dto;
    }
}
