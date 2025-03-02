import processing.core.*;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;

public class BraveryAlive extends PApplet {
    static String appVersion = "3.1.1";
    static final String provenPatch = "15.4.1";
    String gameVersion;
    LogicOperator logicOperator;
    PFont calibri;
    PShape g,k,o;
    Clipboard clipB;
    Button roll, copy, export;
    ArrayList<Button> buttons;
    ArrayList<Pair> build;

    public static void main(String[] args) {
        PApplet.main(BraveryAlive.class.getName());
    }

    @Override
    public void settings() {
        size(500, 600, P3D);
        smooth(16);
    }

    @Override
    public void setup() {
        logicOperator = new LogicOperator();
        gameVersion = logicOperator.getGameVersion();
        clipB = Toolkit.getDefaultToolkit().getSystemClipboard();
        calibri = loadFont("Calibri-36.vlw");
        textFont(calibri);

        g = new PShape();
        g.scale(50);
        k = new PShape();
        o = new PShape();
        createCopy();
        createExport();
        createDice();

        roll = new Button(width / 4f, height - 50, 50, 50, ELLIPSE, CENTER,CORNER,g);
        copy = new Button((3 * width) / 4f, height - 50, 50, 50, ELLIPSE, CENTER,CENTER,k);
        export = new Button(width / 2f, height - 50, 50, 50, ELLIPSE, CENTER,CENTER,o);
        buttons = new ArrayList<>();
        buttons.add(roll);
        buttons.add(copy);
        buttons.add(export);

        build = new ArrayList<>();
    }

    @Override
    public void draw() {
        background(220);
        drawBack();
        drawText();
        for(Button b: buttons){
            b.drawMe(this);
        }
        if (roll.inMargin(this)) {
            roll.getShape().rotateY(0.03f);
            roll.getShape().rotateX(0.03f);
        }
    }

    @Override
    public void mousePressed() {
        for (Button b : buttons) {
            b.setPressed(b.inMargin(this));
        }
    }

    @Override
    public void mouseReleased() {
        for (Button b : buttons) {
            if (b == roll) {
                if (b.inMargin(this) && b.getPressed())
                    rollNewBuild();}
            if (b == copy) {
                if (b.inMargin(this) && b.getPressed())
                    copyToClip();
            }
            if (b == export) {
                if (b.inMargin(this) && b.getPressed())
                    exportToLeague();
            }
            b.setPressed(false);
        }
    }

    //get a new randomly determined build
    private void rollNewBuild() {
        build.clear();
        build.add(logicOperator.getNewChamp());
        build.add(new Pair(logicOperator.getNewMastery(), null));
        build.add(new Pair(logicOperator.getNewSummonerSpell("none"), null));
        build.add(new Pair(logicOperator.getNewSummonerSpell(build.get(2).name()), null));
        build.add(new Pair(logicOperator.getNewSkillMaxing(),null));
        build.add(logicOperator.getNewBoots());
        build.addAll(logicOperator.getNewLegendary(build.get(0).name(),5));
    }

    //copy build data into clipboard to be used in League client -> item build
    public void exportToLeague() {
        if(!build.isEmpty()) {
            String h2;
            ArrayList<Integer> itemIDs = logicOperator.getItemIdFromName(new ArrayList<>(build.subList(5, 11)));
            try {
                h2 = "{\"title\":\"" + build.get(0).name() + " - " + build.get(1).name() + " - " + build.get(4).name()
                        + "\",\"associatedMaps\":[11],\"associatedChampions\":[" + logicOperator.getChampIdFromName(build.get(0).name())
                        + "],\"blocks\":[{\"items\":["
                        + "{\"id\":\"" + itemIDs.get(0) + "\",\"count\":1}"
                        + ",{\"id\":\"" + itemIDs.get(1) + "\",\"count\":1}"
                        + ",{\"id\":\"" + itemIDs.get(2) + "\",\"count\":1}"
                        + ",{\"id\":\"" + itemIDs.get(3) + "\",\"count\":1}"
                        + ",{\"id\":\"" + itemIDs.get(4) + "\",\"count\":1}"
                        + ",{\"id\":\"" + itemIDs.get(5) + "\",\"count\":1}"
                        + "],\"type\":\"" + build.get(0).name() + "'s Wasted Gaming Build\"}"
                        + ",{\"items\":[{\"id\":\"2003\",\"count\":1}],\"type\":\"Consumables\"}]}"
                ;
            } catch (ArrayIndexOutOfBoundsException a) {
                h2 = "";
                println("exportError");
            }
            StringSelection copyText = new StringSelection(h2);
            clipB.setContents(copyText, null);
        }
    }

    //copy build data into clipboard to be used in Discord chat
    public void copyToClip() {
        if(!build.isEmpty()) {
            String h2;
            try {
                h2 = "Champion\t\t    " + build.get(0).name()
                        + "\nRune Path\t\t      " + build.get(1).name()
                        + "\nSummoners\t\t " + build.get(2).name() + " " + build.get(3).name()
                        + "\nSpell to max\t\t " + build.get(4).name()
                        + "\nItems\t\t\t\t\t  " + build.get(5).name()
                        + "\n\t\t\t\t\t\t\t\t" + build.get(6).name()
                        + "\n\t\t\t\t\t\t\t\t" + build.get(7).name()
                        + "\n\t\t\t\t\t\t\t\t" + build.get(8).name()
                        + "\n\t\t\t\t\t\t\t\t" + build.get(9).name()
                        + "\n\t\t\t\t\t\t\t\t" + build.get(10).name();
            } catch (ArrayIndexOutOfBoundsException a) {
                h2 = "";
            }
            StringSelection copyText = new StringSelection(h2);
            clipB.setContents(copyText, null);
        }
    }

    //draw all the text
    private void drawText() {
        float posXCategoryTitle = 30;
        float posXCategory = width / 2f;
        float posXItems = width / 3.5f;
        if (build.isEmpty()) {
            textSize(32);
            fill(0);
            textAlign(CENTER);
            text("Test Your Braverieness", posXCategory, height / 2f - 45);
            textSize(14);
            textAlign(CORNER);
            text("Version " + appVersion, 3, height - 3);
            textAlign(RIGHT);
            text("Last proven patch: " + provenPatch, width - 3, height - 3);
        } else {
            textSize(32);
            textAlign(CORNER);
            imageMode(CORNER);
            fill(222, 235, 247);
            text("Champion", posXCategoryTitle, 53);
            text(build.get(0).name(), posXCategory, 53);
            image(build.get(0).image(), posXCategory - 40, 28, 32, 32);
            text("Rune Path", posXCategoryTitle, 103);
            text(build.get(1).name(), posXCategory, 103);
            text("Summoner", posXCategoryTitle, 153);
            text(build.get(2).name() + ", " + build.get(3).name(), posXCategory, 153);
            text("Spell to Max", posXCategoryTitle, 203);
            text(build.get(4).name(), posXCategory, 203);
            textSize(36);
            fill(197, 90, 17);
            textSize(42);
            textAlign(CENTER);
            text("Items", posXCategory, 260);
            textSize(32);
            fill(0);
            textAlign(CORNER);
            text(build.get(5).name(), posXItems, 300);
            image(build.get(5).image(), posXItems - 40, 275, 32, 32);
            text(build.get(6).name(), posXItems, 340);
            image(build.get(6).image(), posXItems - 40, 315, 32, 32);
            text(build.get(7).name(), posXItems, 380);
            image(build.get(7).image(), posXItems - 40, 355, 32, 32);
            text(build.get(8).name(), posXItems, 420);
            image(build.get(8).image(), posXItems - 40, 395, 32, 32);
            text(build.get(9).name(), posXItems, 460);
            image(build.get(9).image(), posXItems - 40, 435, 32, 32);
            text(build.get(10).name(), posXItems, 500);
            image(build.get(10).image(), posXItems - 40, 475, 32, 32);
            textSize(14);
            textAlign(CORNER);
            text("Version " + appVersion, 3, height - 3);
            textAlign(RIGHT);
            text("Last proven patch: " + provenPatch, width - 3, height - 3);
        }
    }

    private void drawBack() {
        stroke(220);
        strokeWeight(3);
        rectMode(CORNER);
        fill(31, 78, 121);
        rect(20, 20, width - 40, 100, 15, 15, 15, 15);
        fill(56, 87, 35);
        rect(20, 120, width - 40, 100, 15, 15, 15, 15);
        fill(244, 177, 131);
        rect(20, 220, width - 40, 300, 15, 15, 15, 15);
    }

    private void createCopy() {
        k = createShape(GROUP);
        strokeWeight(3);
        fill(255, 255, 255);
        PShape one = createShape(RECT, 0, 0, 40, 50, 6);
        PShape first = createShape(LINE, 8, 15, 32, 15);
        PShape second = createShape(LINE, 8, 25, 27, 25);
        PShape third = createShape(LINE, 8, 35, 32, 35);
        PShape two = createShape(RECT, 10, -7, 40, 50, 6);
        one.translate(0, 0, 10);
        first.translate(0, 0, 10);
        second.translate(0, 0, 10);
        third.translate(0, 0, 10);
        two.translate(0, 0, 5);
        k.addChild(one);
        k.addChild(first);
        k.addChild(second);
        k.addChild(third);
        k.addChild(two);
        k.scale(0.4f);
        k.translate(0,2);
    }

    private void createExport() {
        o = createShape();
        o.beginShape();
        o.strokeWeight(1);
        o.noFill();
        o.vertex(0, 7);
        o.vertex(14, 7);
        o.vertex(14, 0);
        o.vertex(28, 14);
        o.vertex(14, 28);
        o.vertex(14, 21);
        o.vertex(0, 21);
        o.endShape(CLOSE);
    }

    private void createDice() {
        g = createShape(GROUP);
        strokeWeight(1);
        stroke(180,180,180);
        fill(255,255,255);
        PShape[] dices = new PShape[6];
        PGraphics[] sides = setSides();
        for (int i = 0; i < dices.length; i++) {
            dices[i] = createShape();
            dices[i].beginShape();
            dices[i].texture(sides[i]);
            dices[i].vertex(-50, -50, 50, 0, 0);
            dices[i].vertex(50, -50, 50, 100, 0);
            dices[i].vertex(50, 50, 50, 100, 100);
            dices[i].vertex(-50, 50, 50, 0, 100);
            dices[i].endShape();
            switch (i) {
                case 1 -> dices[i].rotateY(PI / 2);
                case 2 -> dices[i].rotateX((float) (PI * 1.5));
                case 3 -> dices[i].rotateX(PI / 2);
                case 4 -> dices[i].rotateY((float) (PI * 1.5));
                case 5 -> dices[i].rotateX(PI);
            }

            g.addChild(dices[i]);
        }
        g.scale((float) 0.2);
        g.translate(12,12);
    }

    private PGraphics[] setSides() {
        PGraphics[] sides = new PGraphics[6];
        ellipseMode(CENTER);
        for (int i = 0; i < sides.length; i++) {
            sides[i] = createGraphics(100, 100);
            sides[i].beginDraw();
            sides[i].fill(255);
            sides[i].rect(0, 0, 100, 100);
            sides[i].endDraw();
        }
        sides[0].beginDraw();
        sides[0].fill(0);
        sides[0].ellipse(50, 50, 20, 20);
        sides[0].endDraw();

        sides[1].beginDraw();
        sides[1].fill(0);
        sides[1].ellipse(25, 25, 20, 20);
        sides[1].ellipse(75, 75, 20, 20);
        sides[1].endDraw();

        sides[2].beginDraw();
        sides[2].fill(0);
        sides[2].ellipse(25, 25, 20, 20);
        sides[2].ellipse(50, 50, 20, 20);
        sides[2].ellipse(75, 75, 20, 20);
        sides[2].endDraw();

        sides[3].beginDraw();
        sides[3].fill(0);
        sides[3].ellipse(25, 25, 20, 20);
        sides[3].ellipse(25, 75, 20, 20);
        sides[3].ellipse(75, 25, 20, 20);
        sides[3].ellipse(75, 75, 20, 20);
        sides[3].endDraw();

        sides[4].beginDraw();
        sides[4].fill(0);
        sides[4].ellipse(25, 25, 20, 20);
        sides[4].ellipse(25, 75, 20, 20);
        sides[4].ellipse(75, 25, 20, 20);
        sides[4].ellipse(50, 50, 20, 20);
        sides[4].ellipse(75, 75, 20, 20);
        sides[4].endDraw();

        sides[5].beginDraw();
        sides[5].fill(0);
        sides[5].ellipse(75, 75, 20, 20);
        sides[5].ellipse(25, 75, 20, 20);
        sides[5].ellipse(75, 25, 20, 20);
        sides[5].ellipse(75, 50, 20, 20);
        sides[5].ellipse(25, 25, 20, 20);
        sides[5].ellipse(25, 50, 20, 20);
        sides[5].endDraw();

        return sides;
    }
}

