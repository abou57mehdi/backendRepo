package com.ESI.CareerBooster.cvGenerator.service.impl;

import com.ESI.CareerBooster.auth.model.User;
import com.ESI.CareerBooster.auth.repository.UserRepository;
import com.ESI.CareerBooster.cvGenerator.model.CVGenerator;
import com.ESI.CareerBooster.cvGenerator.repository.CVGeneratorRepository;
import com.ESI.CareerBooster.cvGenerator.service.CVGeneratorService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CVGeneratorServiceImpl implements CVGeneratorService {

    private final CVGeneratorRepository cvGeneratorRepository;
    private final UserRepository userRepository;

    @Override
    public CVGenerator createCV(CVGenerator cv, String userEmail) {
        log.info("Creating new CV for user: {}", userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        cv.setUser(user);
        cv.setCreatedDate(LocalDateTime.now());
        cv.setLastModifiedDate(LocalDateTime.now());

        // Set default template if not provided
        if (cv.getTemplate() == null || cv.getTemplate().isEmpty()) {
            cv.setTemplate("modern");
        }

        CVGenerator savedCV = cvGeneratorRepository.save(cv);
        log.info("CV created successfully with ID: {}", savedCV.getId());

        return savedCV;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CVGenerator> getAllCVsByUserId(String userEmail) {
        log.info("Fetching all CVs for user: {}", userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return cvGeneratorRepository.findByUserOrderByCreatedDateDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CVGenerator> getCVById(Long id, String userEmail) {
        log.info("Fetching CV with ID: {} for user: {}", id, userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return cvGeneratorRepository.findByIdAndUser(id, user);
    }

    @Override
    public CVGenerator updateCV(Long id, CVGenerator updatedCV, String userEmail) {
        log.info("Updating CV with ID: {} for user: {}", id, userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Optional<CVGenerator> existingCVOpt = cvGeneratorRepository.findByIdAndUser(id, user);

        if (existingCVOpt.isEmpty()) {
            log.warn("CV not found or user not authorized. ID: {}, User: {}", id, userEmail);
            throw new RuntimeException("CV not found or access denied");
        }

        CVGenerator existingCV = existingCVOpt.get();

        // Update fields
        existingCV.setPersonalInfo(updatedCV.getPersonalInfo());
        existingCV.setSummary(updatedCV.getSummary());
        existingCV.setSkills(updatedCV.getSkills());
        existingCV.setExperiences(updatedCV.getExperiences());
        existingCV.setEducation(updatedCV.getEducation());
        existingCV.setProjects(updatedCV.getProjects());
        existingCV.setCertifications(updatedCV.getCertifications());
        existingCV.setLanguages(updatedCV.getLanguages());
        existingCV.setTemplate(updatedCV.getTemplate());
        existingCV.setLastModifiedDate(LocalDateTime.now());

        CVGenerator savedCV = cvGeneratorRepository.save(existingCV);
        log.info("CV updated successfully with ID: {}", savedCV.getId());

        return savedCV;
    }

    @Override
    public boolean deleteCV(Long id, String userEmail) {
        log.info("Deleting CV with ID: {} for user: {}", id, userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (!cvGeneratorRepository.existsByIdAndUser(id, user)) {
            log.warn("CV not found or user not authorized. ID: {}, User: {}", id, userEmail);
            return false;
        }

        cvGeneratorRepository.deleteByIdAndUser(id, user);
        log.info("CV deleted successfully with ID: {}", id);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long getCVCountByUserId(String userEmail) {
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return cvGeneratorRepository.countByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsCVForUser(Long id, String userEmail) {
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return cvGeneratorRepository.existsByIdAndUser(id, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CVGenerator> getCVsByTemplate(String userEmail, String template) {
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return cvGeneratorRepository.findByUserAndTemplate(user, template);
    }

    @Override
    public byte[] generateCVAsPdf(Long id, String userEmail, String template) {
        log.info("Generating PDF for CV ID: {} with template: {}", id, template);

        Optional<CVGenerator> cvOpt = getCVById(id, userEmail);
        if (cvOpt.isEmpty()) {
            throw new RuntimeException("CV not found or access denied");
        }

        CVGenerator cv = cvOpt.get();

        try {
            return generateModernCV(cv);
        } catch (Exception e) {
            log.error("Error generating PDF for CV ID: {}", id, e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private byte[] generateModernCV(CVGenerator cv) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Define colors
        BaseColor primaryBlue = new BaseColor(25, 118, 210); // #1976D2
        BaseColor lightBlue = new BaseColor(227, 242, 253); // #E3F2FD
        BaseColor darkGray = new BaseColor(66, 66, 66);
        BaseColor redAccent = new BaseColor(244, 67, 54); // For highlights

        // Create complete layout with header and content in one table
        createCompleteLayout(document, cv, primaryBlue, lightBlue, darkGray, redAccent);

        document.close();
        return baos.toByteArray();
    }

    private void createCompleteLayout(Document document, CVGenerator cv, BaseColor primaryBlue,
                                    BaseColor lightBlue, BaseColor darkGray, BaseColor redAccent) throws DocumentException {
        // Create one main table for the entire CV
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{60, 40});

        // Left column - Main content
        PdfPCell leftColumn = new PdfPCell();
        leftColumn.setBorder(Rectangle.NO_BORDER);
        leftColumn.setPadding(0);
        leftColumn.setVerticalAlignment(Element.ALIGN_TOP);

        // Right column - Sidebar with light blue background
        PdfPCell rightColumn = new PdfPCell();
        rightColumn.setBorder(Rectangle.NO_BORDER);
        rightColumn.setPadding(0);
        rightColumn.setBackgroundColor(lightBlue);
        rightColumn.setVerticalAlignment(Element.ALIGN_TOP);

        // Add header to left column
        addHeaderToLeftColumn(leftColumn, cv, primaryBlue, lightBlue, darkGray);

        // Add main content to left column
        addMainContentToLeftColumn(leftColumn, cv, primaryBlue, darkGray);

        // Add sidebar content to right column
        addSidebarContent(rightColumn, cv, primaryBlue, darkGray, redAccent);

        mainTable.addCell(leftColumn);
        mainTable.addCell(rightColumn);
        document.add(mainTable);
    }

    private void addHeaderToLeftColumn(PdfPCell leftColumn, CVGenerator cv, BaseColor primaryBlue,
                                     BaseColor lightBlue, BaseColor darkGray) {
        // Create header section
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(25);
        headerCell.setBackgroundColor(lightBlue);

        // Name
        String fullName = getFullName(cv);
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, primaryBlue);
        Paragraph namePara = new Paragraph(fullName.toUpperCase(), nameFont);
        namePara.setSpacingAfter(8);
        headerCell.addElement(namePara);

        // Title
        if (cv.getPersonalInfo() != null && cv.getPersonalInfo().getTitle() != null) {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 16, darkGray);
            Paragraph titlePara = new Paragraph(cv.getPersonalInfo().getTitle(), titleFont);
            titlePara.setSpacingAfter(15);
            headerCell.addElement(titlePara);
        }

        // Contact Info
        addContactInfoToHeader(headerCell, cv, darkGray);

        headerTable.addCell(headerCell);
        leftColumn.addElement(headerTable);
    }

    private void addContactInfoToHeader(PdfPCell headerCell, CVGenerator cv, BaseColor darkGray) {
        if (cv.getPersonalInfo() == null) return;

        Font contactFont = FontFactory.getFont(FontFactory.HELVETICA, 12, darkGray);

        // Email
        if (cv.getPersonalInfo().getEmail() != null) {
            Paragraph email = new Paragraph("✉ " + cv.getPersonalInfo().getEmail(), contactFont);
            email.setSpacingBefore(3);
            headerCell.addElement(email);
        }

        // Phone
        if (cv.getPersonalInfo().getPhone() != null) {
            Paragraph phone = new Paragraph("📞 " + cv.getPersonalInfo().getPhone(), contactFont);
            phone.setSpacingBefore(3);
            headerCell.addElement(phone);
        }

        // LinkedIn
        if (cv.getPersonalInfo().getLinkedin() != null && !cv.getPersonalInfo().getLinkedin().trim().isEmpty()) {
            Paragraph linkedin = new Paragraph("🔗 " + cv.getPersonalInfo().getLinkedin(), contactFont);
            linkedin.setSpacingBefore(3);
            headerCell.addElement(linkedin);
        }

        // GitHub
        if (cv.getPersonalInfo().getGithub() != null && !cv.getPersonalInfo().getGithub().trim().isEmpty()) {
            Paragraph github = new Paragraph("💻 " + cv.getPersonalInfo().getGithub(), contactFont);
            github.setSpacingBefore(3);
            headerCell.addElement(github);
        }
    }

    private void addMainContentToLeftColumn(PdfPCell leftColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        // Create content section with padding
        PdfPTable contentTable = new PdfPTable(1);
        contentTable.setWidthPercentage(100);

        PdfPCell contentCell = new PdfPCell();
        contentCell.setBorder(Rectangle.NO_BORDER);
        contentCell.setPadding(25);
        contentCell.setVerticalAlignment(Element.ALIGN_TOP);

        // Add main sections with minimal spacing
        addExperienceSection(contentCell, cv, primaryBlue, darkGray);
        addProjectsSection(contentCell, cv, primaryBlue, darkGray);
        addEducationSection(contentCell, cv, primaryBlue, darkGray);

        contentTable.addCell(contentCell);
        leftColumn.addElement(contentTable);
    }

    private void addSidebarContent(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue,
                                 BaseColor darkGray, BaseColor redAccent) {
        // Create sidebar section with padding
        PdfPTable sidebarTable = new PdfPTable(1);
        sidebarTable.setWidthPercentage(100);

        PdfPCell sidebarCell = new PdfPCell();
        sidebarCell.setBorder(Rectangle.NO_BORDER);
        sidebarCell.setPadding(25);
        sidebarCell.setVerticalAlignment(Element.ALIGN_TOP);

        // Add "PROFESSIONAL CV" badge at top
        Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, primaryBlue);
        Paragraph badge = new Paragraph("PROFESSIONAL\nCV", badgeFont);
        badge.setAlignment(Element.ALIGN_CENTER);
        badge.setSpacingAfter(20);
        sidebarCell.addElement(badge);

        // Add sidebar sections
        addSummarySection(sidebarCell, cv, primaryBlue, darkGray);
        addSkillsSection(sidebarCell, cv, primaryBlue, darkGray);
        addLanguagesSection(sidebarCell, cv, primaryBlue, darkGray, redAccent);
        addCertificationsSection(sidebarCell, cv, primaryBlue, darkGray);
        addAdditionalSections(sidebarCell, cv, primaryBlue, darkGray);

        sidebarTable.addCell(sidebarCell);
        rightColumn.addElement(sidebarTable);
    }

    private String getFullName(CVGenerator cv) {
        if (cv.getPersonalInfo() == null) {
            return "Professional CV";
        }

        String fullName = cv.getPersonalInfo().getName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = (cv.getPersonalInfo().getFirstName() != null ? cv.getPersonalInfo().getFirstName() : "") +
                      " " + (cv.getPersonalInfo().getLastName() != null ? cv.getPersonalInfo().getLastName() : "");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Professional CV";
        }
        return fullName.trim();
    }

    private void addSectionTitle(PdfPCell cell, String title, BaseColor primaryBlue) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, primaryBlue);
        Paragraph titlePara = new Paragraph(title.toUpperCase(), titleFont);
        titlePara.setSpacingBefore(8);  // Reduced from 15
        titlePara.setSpacingAfter(5);   // Reduced from 8

        // Add underline effect
        titlePara.add(new Chunk("\n"));
        Chunk underline = new Chunk("_____________________");
        underline.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8, primaryBlue));
        titlePara.add(underline);

        cell.addElement(titlePara);
    }

    private void addExperienceSection(PdfPCell leftColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getExperiences() == null || cv.getExperiences().isEmpty()) return;

        addSectionTitle(leftColumn, "Experience", primaryBlue);

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkGray);
        Font positionFont = FontFactory.getFont(FontFactory.HELVETICA, 11, darkGray);
        Font descriptionFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);

        for (var experience : cv.getExperiences()) {
            // Job Title
            if (experience.getPosition() != null) {
                Paragraph jobTitle = new Paragraph(experience.getPosition(), companyFont);
                jobTitle.setSpacingBefore(6);  // Reduced from 10
                leftColumn.addElement(jobTitle);
            }

            // Company and dates
            StringBuilder companyInfo = new StringBuilder();
            if (experience.getCompany() != null) {
                companyInfo.append(experience.getCompany());
            }
            if (experience.getStartDate() != null || experience.getEndDate() != null) {
                companyInfo.append(" | ");
                if (experience.getStartDate() != null) {
                    companyInfo.append(experience.getStartDate());
                }
                companyInfo.append(" - ");
                if (experience.getEndDate() != null) {
                    companyInfo.append(experience.getEndDate());
                } else {
                    companyInfo.append("Present");
                }
            }

            if (companyInfo.length() > 0) {
                Paragraph company = new Paragraph(companyInfo.toString(), positionFont);
                company.setSpacingBefore(1);  // Reduced from 2
                leftColumn.addElement(company);
            }

            // Description with bullet points
            if (experience.getDescription() != null && !experience.getDescription().trim().isEmpty()) {
                String[] descriptions = experience.getDescription().split("\n");
                for (String desc : descriptions) {
                    if (!desc.trim().isEmpty()) {
                        Paragraph bullet = new Paragraph("• " + desc.trim(), descriptionFont);
                        bullet.setIndentationLeft(15);
                        bullet.setSpacingBefore(2);  // Reduced from 3
                        leftColumn.addElement(bullet);
                    }
                }
            }
        }
    }

    private void addProjectsSection(PdfPCell leftColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getProjects() == null || cv.getProjects().isEmpty()) return;

        addSectionTitle(leftColumn, "Projects", primaryBlue);

        Font projectFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkGray);
        Font descriptionFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);
        Font techFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);

        for (var project : cv.getProjects()) {
            if (project.getName() != null) {
                Paragraph projectName = new Paragraph(project.getName(), projectFont);
                projectName.setSpacingBefore(6);  // Reduced from 10
                leftColumn.addElement(projectName);
            }

            if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
                Paragraph description = new Paragraph(project.getDescription(), descriptionFont);
                description.setIndentationLeft(15);
                description.setSpacingBefore(2);  // Reduced from 3
                leftColumn.addElement(description);
            }

            if (project.getTechnologies() != null && !project.getTechnologies().isEmpty()) {
                StringBuilder techText = new StringBuilder("Technologies: ");
                for (int i = 0; i < project.getTechnologies().size(); i++) {
                    techText.append(project.getTechnologies().get(i));
                    if (i < project.getTechnologies().size() - 1) {
                        techText.append(", ");
                    }
                }
                Paragraph tech = new Paragraph(techText.toString(), techFont);
                tech.setIndentationLeft(15);
                tech.setSpacingBefore(1);  // Reduced from 2
                leftColumn.addElement(tech);
            }
        }
    }

    private void addEducationSection(PdfPCell leftColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getEducation() == null || cv.getEducation().isEmpty()) return;

        addSectionTitle(leftColumn, "Education", primaryBlue);

        Font degreeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkGray);
        Font institutionFont = FontFactory.getFont(FontFactory.HELVETICA, 11, darkGray);
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

        for (var education : cv.getEducation()) {
            // Degree
            StringBuilder degreeInfo = new StringBuilder();
            if (education.getDegree() != null) {
                degreeInfo.append(education.getDegree());
            }
            if (education.getFieldOfStudy() != null) {
                if (degreeInfo.length() > 0) degreeInfo.append(" in ");
                degreeInfo.append(education.getFieldOfStudy());
            }

            if (degreeInfo.length() > 0) {
                Paragraph degree = new Paragraph(degreeInfo.toString(), degreeFont);
                degree.setSpacingBefore(6);  // Reduced from 10
                leftColumn.addElement(degree);
            }

            // Institution
            if (education.getInstitution() != null) {
                Paragraph institution = new Paragraph(education.getInstitution(), institutionFont);
                institution.setSpacingBefore(1);  // Reduced from 2
                leftColumn.addElement(institution);
            }

            // Dates
            if (education.getStartDate() != null || education.getEndDate() != null) {
                StringBuilder dateInfo = new StringBuilder();
                if (education.getStartDate() != null) {
                    dateInfo.append(education.getStartDate());
                }
                dateInfo.append(" - ");
                if (education.getEndDate() != null) {
                    dateInfo.append(education.getEndDate());
                } else {
                    dateInfo.append("Present");
                }

                Paragraph dates = new Paragraph(dateInfo.toString(), dateFont);
                dates.setSpacingBefore(1);  // Reduced from 2
                leftColumn.addElement(dates);
            }
        }
    }

    private void addSummarySection(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getSummary() == null || cv.getSummary().trim().isEmpty()) return;

        addSectionTitle(rightColumn, "My Life Philosophy", primaryBlue);

        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, darkGray);
        Paragraph summary = new Paragraph("\"" + cv.getSummary() + "\"", contentFont);
        summary.setAlignment(Element.ALIGN_JUSTIFIED);
        summary.setSpacingBefore(3);  // Reduced from 5
        rightColumn.addElement(summary);
    }

    private void addSkillsSection(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getSkills() == null || cv.getSkills().isEmpty()) return;

        addSectionTitle(rightColumn, "Strengths", primaryBlue);

        Font skillFont = FontFactory.getFont(FontFactory.HELVETICA, 11, darkGray);

        for (String skill : cv.getSkills()) {
            // Create skill with visual rating
            Paragraph skillPara = new Paragraph();
            skillPara.add(new Chunk(skill, skillFont));
            skillPara.add(new Chunk("     ", skillFont));

            // Add visual rating dots (simplified)
            Chunk dots = new Chunk("●●●●●", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, primaryBlue));
            skillPara.add(dots);

            skillPara.setSpacingBefore(5);  // Reduced from 8
            rightColumn.addElement(skillPara);
        }
    }

    private void addLanguagesSection(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue,
                                   BaseColor darkGray, BaseColor redAccent) {
        if (cv.getLanguages() == null || cv.getLanguages().isEmpty()) return;

        addSectionTitle(rightColumn, "Languages", primaryBlue);

        Font langFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, darkGray);
        Font levelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);

        for (var language : cv.getLanguages()) {
            if (language.getName() != null) {
                Paragraph langPara = new Paragraph();
                langPara.add(new Chunk(language.getName(), langFont));

                if (language.getProficiency() != null) {
                    langPara.add(new Chunk("\n" + language.getProficiency(), levelFont));

                    // Add visual proficiency dots
                    String dots = getProficiencyDots(language.getProficiency());
                    Chunk proficiencyDots = new Chunk("  " + dots,
                        new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, redAccent));
                    langPara.add(proficiencyDots);
                }

                langPara.setSpacingBefore(6);  // Reduced from 10
                rightColumn.addElement(langPara);
            }
        }
    }

    private String getProficiencyDots(String proficiency) {
        if (proficiency == null) return "●●●○○";

        String lower = proficiency.toLowerCase();
        if (lower.contains("native") || lower.contains("fluent") || lower.contains("advanced")) {
            return "●●●●●";
        } else if (lower.contains("intermediate") || lower.contains("conversational")) {
            return "●●●○○";
        } else if (lower.contains("basic") || lower.contains("beginner")) {
            return "●●○○○";
        }
        return "●●●○○"; // default
    }

    private void addCertificationsSection(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        if (cv.getCertifications() == null || cv.getCertifications().isEmpty()) return;

        addSectionTitle(rightColumn, "Certifications", primaryBlue);

        Font certFont = FontFactory.getFont(FontFactory.HELVETICA, 11, darkGray);
        Font issuerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);

        for (var certification : cv.getCertifications()) {
            Paragraph certPara = new Paragraph();

            if (certification.getName() != null) {
                certPara.add(new Chunk(certification.getName(), certFont));
            }

            if (certification.getIssuer() != null) {
                certPara.add(new Chunk("\n" + certification.getIssuer(), issuerFont));
            }

            if (certification.getDate() != null) {
                certPara.add(new Chunk(" (" + certification.getDate() + ")", issuerFont));
            }

            certPara.setSpacingBefore(5);  // Reduced from 8
            rightColumn.addElement(certPara);
        }
    }

    private void addAdditionalSections(PdfPCell rightColumn, CVGenerator cv, BaseColor primaryBlue, BaseColor darkGray) {
        // Add hobbies if available
        if (cv.getHobbies() != null && !cv.getHobbies().isEmpty()) {
            addSectionTitle(rightColumn, "Interests", primaryBlue);

            Font hobbyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);
            StringBuilder hobbiesText = new StringBuilder();
            for (int i = 0; i < cv.getHobbies().size(); i++) {
                hobbiesText.append(cv.getHobbies().get(i));
                if (i < cv.getHobbies().size() - 1) {
                    hobbiesText.append(" • ");
                }
            }

            Paragraph hobbies = new Paragraph(hobbiesText.toString(), hobbyFont);
            hobbies.setSpacingBefore(3);  // Reduced from 5
            rightColumn.addElement(hobbies);
        }

        // Add awards if available
        if (cv.getAwards() != null && !cv.getAwards().isEmpty()) {
            addSectionTitle(rightColumn, "Awards", primaryBlue);

            Font awardFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);
            for (String award : cv.getAwards()) {
                Paragraph awardPara = new Paragraph("🏆 " + award, awardFont);
                awardPara.setSpacingBefore(3);  // Reduced from 5
                rightColumn.addElement(awardPara);
            }
        }

        // Add volunteering if available
        if (cv.getVolunteering() != null && !cv.getVolunteering().isEmpty()) {
            addSectionTitle(rightColumn, "Volunteering", primaryBlue);

            Font volFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);
            for (String vol : cv.getVolunteering()) {
                Paragraph volPara = new Paragraph("🤝 " + vol, volFont);
                volPara.setSpacingBefore(3);  // Reduced from 5
                rightColumn.addElement(volPara);
            }
        }
    }
}