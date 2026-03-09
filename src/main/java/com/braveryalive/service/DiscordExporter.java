package com.braveryalive.service;

import com.braveryalive.model.Build;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Exports build to Discord chat format.
 */
public class DiscordExporter implements BuildExporter {
    private final Clipboard clipboard;

    public DiscordExporter() {
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    @Override
    public void export(Build build) {
        if (build == null) return;
        String text = String.format(
                "Champion\t\t    %s\nRune Path\t\t      %s\nSummoners\t\t %s %s\nSpell to max\t\t %s\nItems\t\t\t\t\t  %s\n\t\t\t\t\t\t\t\t%s\n\t\t\t\t\t\t\t\t%s\n\t\t\t\t\t\t\t\t%s\n\t\t\t\t\t\t\t\t%s\n\t\t\t\t\t\t\t\t%s",
                build.getChampion().name(),
                build.getMastery().name(),
                build.getSummonerSpell1(), build.getSummonerSpell2(),
                build.getSkillToMax().name(),
                build.getBoots().name(),
                build.getLegendaryItems().get(0).name(),
                build.getLegendaryItems().get(1).name(),
                build.getLegendaryItems().get(2).name(),
                build.getLegendaryItems().get(3).name(),
                build.getLegendaryItems().get(4).name()
        );
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }
}
