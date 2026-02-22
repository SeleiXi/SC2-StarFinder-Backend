package com.starfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_reports")
public class PublicReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "mmr_min")
    private Integer mmrMin;

    @Column(name = "mmr_max")
    private Integer mmrMax;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reported_by_id")
    private Long reportedById;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public Integer getMmrMin() { return mmrMin; }
    public void setMmrMin(Integer mmrMin) { this.mmrMin = mmrMin; }
    public Integer getMmrMax() { return mmrMax; }
    public void setMmrMax(Integer mmrMax) { this.mmrMax = mmrMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getReportedById() { return reportedById; }
    public void setReportedById(Long reportedById) { this.reportedById = reportedById; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
