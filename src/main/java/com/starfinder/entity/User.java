package com.starfinder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "battle_tag")
    private String battleTag;

    @Column(name = "battle_tag_cn")
    private String battleTagCN;

    @Column(name = "battle_tag_us")
    private String battleTagUS;

    @Column(name = "battle_tag_eu")
    private String battleTagEU;

    @Column(name = "battle_tag_kr")
    private String battleTagKR;

    @Column(name = "character_id")
    private Long characterId;

    @Column(length = 1)
    private String race;

    private Integer mmr;

    @Column(name = "mmr_terran")
    private Integer mmrTerran;

    @Column(name = "mmr_zerg")
    private Integer mmrZerg;

    @Column(name = "mmr_protoss")
    private Integer mmrProtoss;

    @Column(name = "mmr_random")
    private Integer mmrRandom;

    @Column(name = "mmr_2v2")
    private Integer mmr2v2;

    @Column(name = "mmr_3v3")
    private Integer mmr3v3;

    @Column(name = "mmr_4v4")
    private Integer mmr4v4;

    @Column(name = "commander")
    private String commander;

    @Column(name = "coop_level", length = 20)
    private String coopLevel;

    @Column(name = "email")
    private String email;

    @Column(nullable = false)
    private String password;

    private String qq;

    @Column(name = "stream_url")
    private String streamUrl;

    private String signature;

    private String region;

    @Column(length = 20)
    private String role;

    @Column(length = 500)
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBattleTag() {
        return battleTag;
    }

    public void setBattleTag(String battleTag) {
        this.battleTag = battleTag;
    }

    public String getBattleTagCN() {
        return battleTagCN;
    }

    public void setBattleTagCN(String battleTagCN) {
        this.battleTagCN = battleTagCN;
    }

    public String getBattleTagUS() {
        return battleTagUS;
    }

    public void setBattleTagUS(String battleTagUS) {
        this.battleTagUS = battleTagUS;
    }

    public String getBattleTagEU() {
        return battleTagEU;
    }

    public void setBattleTagEU(String battleTagEU) {
        this.battleTagEU = battleTagEU;
    }

    public String getBattleTagKR() {
        return battleTagKR;
    }

    public void setBattleTagKR(String battleTagKR) {
        this.battleTagKR = battleTagKR;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public Integer getMmr() {
        return mmr;
    }

    public void setMmr(Integer mmr) {
        this.mmr = mmr;
    }

    public Integer getMmrTerran() {
        return mmrTerran;
    }

    public void setMmrTerran(Integer mmrTerran) {
        this.mmrTerran = mmrTerran;
    }

    public Integer getMmrZerg() {
        return mmrZerg;
    }

    public void setMmrZerg(Integer mmrZerg) {
        this.mmrZerg = mmrZerg;
    }

    public Integer getMmrProtoss() {
        return mmrProtoss;
    }

    public void setMmrProtoss(Integer mmrProtoss) {
        this.mmrProtoss = mmrProtoss;
    }

    public Integer getMmrRandom() {
        return mmrRandom;
    }

    public void setMmrRandom(Integer mmrRandom) {
        this.mmrRandom = mmrRandom;
    }

    public Integer getMmr2v2() {
        return mmr2v2;
    }

    public void setMmr2v2(Integer mmr2v2) {
        this.mmr2v2 = mmr2v2;
    }

    public Integer getMmr3v3() {
        return mmr3v3;
    }

    public void setMmr3v3(Integer mmr3v3) {
        this.mmr3v3 = mmr3v3;
    }

    public Integer getMmr4v4() {
        return mmr4v4;
    }

    public void setMmr4v4(Integer mmr4v4) {
        this.mmr4v4 = mmr4v4;
    }

    public String getCommander() {
        return commander;
    }

    public void setCommander(String commander) {
        this.commander = commander;
    }

    public String getCoopLevel() {
        return coopLevel;
    }

    public void setCoopLevel(String coopLevel) {
        this.coopLevel = coopLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}