package com.smartcampus.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentProfile {
    @SerializedName("id")
    private int id;
    @SerializedName("userId")
    private int userId;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("semester")
    private Integer semester;
    @SerializedName("branch")
    private String branch;
    @SerializedName("cgpa")
    private Float cgpa;
    @SerializedName("preferredRegion")
    private String preferredRegion;
    @SerializedName("about")
    private String about;
    @SerializedName("resumeUrl")
    private String resumeUrl;
    @SerializedName("skills")
    private List<SkillItem> skills;
    @SerializedName("certifications")
    private List<Certification> certifications;
    @SerializedName("projects")
    private List<Project> projects;
    @SerializedName("experiences")
    private List<Experience> experiences;
    @SerializedName("placementStatus")
    private String placementStatus;

    // Getters
    public String getPlacementStatus() {
        return placementStatus != null ? placementStatus : "UNPLACED";
    }
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getSemester() {
        return semester;
    }

    public String getBranch() {
        return branch;
    }

    public Float getCgpa() {
        return cgpa;
    }

    public String getPreferredRegion() {
        return preferredRegion;
    }

    public String getAbout() {
        return about;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public List<SkillItem> getSkills() {
        return skills;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    // Setters
    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setCgpa(Float cgpa) {
        this.cgpa = cgpa;
    }

    public void setPreferredRegion(String preferredRegion) {
        this.preferredRegion = preferredRegion;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setPlacementStatus(String placementStatus) {
        this.placementStatus = placementStatus;
    }
}
