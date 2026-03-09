package com.braveryalive.service;

import com.braveryalive.model.Build;
import com.braveryalive.model.Pair;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Exports build to League of Legends client format.
 */
public class LeagueExporter implements BuildExporter {
    private final Clipboard clipboard;
    private final BuildGenerator buildGenerator;

    public LeagueExporter(BuildGenerator buildGenerator) {
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.buildGenerator = buildGenerator;
    }

    @Override
    public void export(Build build) {
        if (build == null) return;
        List<Integer> itemIDs = buildGenerator.getItemIdFromName(build.getLegendaryItems());
        itemIDs.add(0, buildGenerator.getItemIdFromName(List.of(build.getBoots())).get(0));
        String json = String.format(
                "{\"title\":\"%s - %s - %s\",\"associatedMaps\":[11],\"associatedChampions\":[%d],\"blocks\":[{\"items\":[%s],\"type\":\"%s's Wasted Gaming Build\"},{\"items\":[{\"id\":\"2003\",\"count\":1}],\"type\":\"Consumables\"}]}",
                build.getChampion().name(), build.getMastery().name(), build.getSkillToMax().name(),
                buildGenerator.getChampIdFromName(build.getChampion().name()),
                formatItems(itemIDs),
                build.getChampion().name()
        );
        StringSelection selection = new StringSelection(json);
        clipboard.setContents(selection, null);
    }

    private String formatItems(List<Integer> itemIDs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < itemIDs.size(); i++) {
            sb.append(String.format("{\"id\":\"%d\",\"count\":1}", itemIDs.get(i)));
            if (i < itemIDs.size() - 1) sb.append(",");
        }
        return sb.toString();
    }
}
