package com.starfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coaching_posts")
public class CoachingPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String race;
    private Integer mmr;

    @Column(name = "price_info")
    private String priceInfo;

    private String contact;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "post_type", length = 20)
    private String postType;

    @Column(name = "author_tag")
    private String authorTag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    public Integer getMmr() { return mmr; }
    public void setMmr(Integer mmr) { this.mmr = mmr; }
    public String getPriceInfo() { return priceInfo; }
    public void setPriceInfo(String priceInfo) { this.priceInfo = priceInfo; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    public String getAuthorTag() { return authorTag; }
    public void setAuthorTag(String authorTag) { this.authorTag = authorTag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
