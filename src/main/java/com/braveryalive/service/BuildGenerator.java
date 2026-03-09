package com.braveryalive.service;

import com.braveryalive.model.Pair;
import com.braveryalive.config.Skill;
import com.braveryalive.config.Mastery;
import processing.core.PImage;
import java.util.*;

/**
 * Handles the generation of random builds.
 */
import org.springframework.stereotype.Service;

@Service
public class BuildGenerator {
    private final Random random;
    private final DataLoader dataLoader;

    public BuildGenerator(DataLoader dataLoader) {
        this.random = new Random();
        this.dataLoader = dataLoader;
    }

    public Skill getNewSkillToMax() {
        return switch (random.nextInt(3)) {
            case 0 -> Skill.Q;
            case 1 -> Skill.W;
            case 2 -> Skill.E;
            default -> throw new IllegalStateException("Unexpected value");
        };
    }

    public Mastery getNewMastery() {
        return switch (random.nextInt(4)) {
            case 0 -> Mastery.PRECISION;
            case 1 -> Mastery.DOMINATION;
            case 2 -> Mastery.SORCERY;
            case 3 -> Mastery.RESOLVE;
            default -> throw new IllegalStateException("Unexpected value");
        };
    }

    public String getNewSummonerSpell(String exclude) {
        String spell;
        do {
            spell = dataLoader.getSummonerSpells().get(random.nextInt(dataLoader.getSummonerSpells().size()));
        } while (spell.equals(exclude));
        return spell;
    }

    public Pair getNewChamp() {
        String[] keys = dataLoader.getChampNameChampImage().keySet().toArray(new String[0]);
        String value = keys[random.nextInt(keys.length)];
        return new Pair(value, dataLoader.getChampNameChampImage().get(value));
    }

    public Pair getNewBoots() {
        String[] keys = dataLoader.getBootsNameBootsImage().keySet().toArray(new String[0]);
        String value = keys[random.nextInt(keys.length)];
        return new Pair(value, dataLoader.getBootsNameBootsImage().get(value));
    }

    public List<Pair> getNewLegendary(String champ, int count) {
        List<String> outputList = new ArrayList<>();
        List<Pair> returnList = new ArrayList<>();
        String[] rollingArray = dataLoader.getLegendaryNameLegendaryImage().keySet().toArray(new String[0]);
        List<String> annulGroup = Arrays.asList("Banshee's Veil", "Edge of Night");
        List<String> blightGroup = Arrays.asList("Bloodletter's Curse", "Cryptbloom", "Terminus", "Void Staff");
        List<String> fatalityGroup = Arrays.asList("Black Cleaver", "Serylda's Grudge", "Lord Dominik's Regards", "Mortal Reminder", "Terminus");
        List<String> hydraGroup = Arrays.asList("Profane Hydra", "Ravenous Hydra", "Titanic Hydra", "Stridebreaker");
        List<String> immolateGroup = Arrays.asList("Sunfire Aegis", "Hollow Radiance");
        List<String> lifelineGroup = Arrays.asList("Maw of Malmortius", "Archangel's Staff", "Immortal Shieldbow", "Sterak's Gage");
        List<String> manaflowGroup = Arrays.asList("Winter's Approach", "Archangel's Staff", "Manamune");
        List<String> momentumGroup = Arrays.asList("Dead Man's Plate", "Trailblazer");
        List<String> unboundedGroupOne = Arrays.asList("Iceborn Gauntlet", "Lich Bane", "Trinity Force");
        List<String> rangedOnly = Arrays.asList("Runaan's Hurricane");

        for (int i = 0; i < count; i++) {
            String value = rollingArray[random.nextInt(rollingArray.length)];
            boolean valid = !outputList.contains(value)
                    && !(annulGroup.contains(value) && hasOverlap(outputList, annulGroup))
                    && !(blightGroup.contains(value) && hasOverlap(outputList, blightGroup))
                    && !(fatalityGroup.contains(value) && hasOverlap(outputList, fatalityGroup))
                    && !(hydraGroup.contains(value) && hasOverlap(outputList, hydraGroup))
                    && !(immolateGroup.contains(value) && hasOverlap(outputList, immolateGroup))
                    && !(lifelineGroup.contains(value) && hasOverlap(outputList, lifelineGroup))
                    && !(manaflowGroup.contains(value) && hasOverlap(outputList, manaflowGroup))
                    && !(momentumGroup.contains(value) && hasOverlap(outputList, momentumGroup))
                    && !(unboundedGroupOne.contains(value) && hasOverlap(outputList, unboundedGroupOne))
                    && !(dataLoader.getChampNameChampRange().get(champ) < 275 && rangedOnly.contains(value));
            if (valid) {
                outputList.add(value);
            } else {
                i--;
            }
        }
        for (String s : outputList) {
            returnList.add(new Pair(s, dataLoader.getLegendaryNameLegendaryImage().get(s)));
        }
        return returnList;
    }

    private boolean hasOverlap(List<String> list, List<String> group) {
        for (String item : group) {
            if (list.contains(item)) return true;
        }
        return false;
    }

    public Integer getChampIdFromName(String champName) {
        return dataLoader.getChampNameChampId().get(champName);
    }

    public List<Integer> getItemIdFromName(List<Pair> itemBuild) {
        List<Integer> returnList = new ArrayList<>();
        for (Pair p : itemBuild) {
            returnList.add(dataLoader.getItemNameItemId().get(p.name()));
        }
        return returnList;
    }
}
