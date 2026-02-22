package com.starfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clan_recruitments")
public class ClanRecruitment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "clan_name", nullable = false)
    private String clanName;

    @Column(name = "clan_tag", length = 20)
    private String clanTag;

    private String region;

    @Column(name = "min_mmr")
    private Integer minMmr;

    @Column(name = "max_mmr")
    private Integer maxMmr;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String contact;

    @Column(name = "author_tag")
    private String authorTag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getClanName() { return clanName; }
    public void setClanName(String clanName) { this.clanName = clanName; }
    public String getClanTag() { return clanTag; }
    public void setClanTag(String clanTag) { this.clanTag = clanTag; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getMinMmr() { return minMmr; }
    public void setMinMmr(Integer minMmr) { this.minMmr = minMmr; }
    public Integer getMaxMmr() { return maxMmr; }
    public void setMaxMmr(Integer maxMmr) { this.maxMmr = maxMmr; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAuthorTag() { return authorTag; }
    public void setAuthorTag(String authorTag) { this.authorTag = authorTag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
