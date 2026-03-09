package com.braveryalive.model;

import com.braveryalive.config.Mastery;
import com.braveryalive.config.Skill;
import java.util.List;

/**
 * Represents a complete League of Legends build.
 */
public class Build {
    private final Pair champion;
    private final Mastery mastery;
    private final String summonerSpell1;
    private final String summonerSpell2;
    private final Skill skillToMax;
    private final Pair boots;
    private final List<Pair> legendaryItems;

    public Build(Pair champion, Mastery mastery, String summonerSpell1, String summonerSpell2, Skill skillToMax, Pair boots, List<Pair> legendaryItems) {
        this.champion = champion;
        this.mastery = mastery;
        this.summonerSpell1 = summonerSpell1;
        this.summonerSpell2 = summonerSpell2;
        this.skillToMax = skillToMax;
        this.boots = boots;
        this.legendaryItems = legendaryItems;
    }

    // Getters
    public Pair getChampion() { return champion; }
    public Mastery getMastery() { return mastery; }
    public String getSummonerSpell1() { return summonerSpell1; }
    public String getSummonerSpell2() { return summonerSpell2; }
    public Skill getSkillToMax() { return skillToMax; }
    public Pair getBoots() { return boots; }
    public List<Pair> getLegendaryItems() { return legendaryItems; }
}
