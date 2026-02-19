package com.starfinder.dto;

public class CheaterDTO {
    private String battleTag;
    private String cheatType;
    private String description;

    public String getBattleTag() {
        return battleTag;
    }

    public void setBattleTag(String battleTag) {
        this.battleTag = battleTag;
    }

    public String getCheatType() {
        return cheatType;
    }

    public void setCheatType(String cheatType) {
        this.cheatType = cheatType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
