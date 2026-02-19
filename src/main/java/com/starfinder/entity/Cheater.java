package com.starfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cheaters")
public class Cheater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "battle_tag", nullable = false)
    private String battleTag;

    @Column(name = "cheat_type")
    private String cheatType;

    @Column(length = 1000)
    private String description;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(nullable = false)
    private String status;

    private Integer mmr;

    private String race;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBattleTag() { return battleTag; }
    public void setBattleTag(String battleTag) { this.battleTag = battleTag; }
    public String getCheatType() { return cheatType; }
    public void setCheatType(String cheatType) { this.cheatType = cheatType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMmr() { return mmr; }
    public void setMmr(Integer mmr) { this.mmr = mmr; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
}
