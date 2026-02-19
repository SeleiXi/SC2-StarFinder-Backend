package com.starfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String rules;

    private String rewards;

    @Column(name = "contact_link")
    private String contactLink;

    @Column(name = "group_link")
    private String groupLink;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(nullable = false)
    private String status;

    private String region;

    @Column(name = "start_time")
    private String startTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public String getRewards() { return rewards; }
    public void setRewards(String rewards) { this.rewards = rewards; }
    public String getContactLink() { return contactLink; }
    public void setContactLink(String contactLink) { this.contactLink = contactLink; }
    public String getGroupLink() { return groupLink; }
    public void setGroupLink(String groupLink) { this.groupLink = groupLink; }
    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long submittedBy) { this.submittedBy = submittedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
}
