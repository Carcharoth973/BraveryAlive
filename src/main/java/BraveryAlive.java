import processing.core.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import org.json.*;
import java.net.*;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BraveryAlive extends PApplet {
    static String appVersion = "2.3.0";
    static final String provenPatch = "12.10";
    org.json.JSONObject items_json;
    org.json.JSONObject champs_json;
    org.json.JSONObject summenors_json;
    HashMap<Integer, String> itemID_itemName;
    HashMap<Integer, String> itemID_itemName_Boots;
    HashMap<Integer, String> itemID_itemName_Mythic;
    HashMap<Integer, String> champID_champName;
    HashMap<Integer, Integer> champID_champRange;
    HashMap<Integer, String> summonerID_summonerName;
    Integer champID, sum1ID, sum2ID;
    Integer[] itemID;

    HashMap<Integer, PImage> itemID_ItemImage;

    PFont calibri;
    PGraphics[] sides;
    PShape[] dices;
    PShape g,k,o;
    Clipboard clipB;
    Button reroll;
    Button copy;
    Button export;
    ArrayList<Button> buttons;
    String[] split;
    Integer[] splitInts;
    String version;
    boolean isgRotation;

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
        itemID_itemName = new HashMap<>();
        itemID_itemName_Boots = new HashMap<>();
        itemID_itemName_Mythic = new HashMap<>();
        champID_champName = new HashMap<>();
        champID_champRange = new HashMap<>();
        summonerID_summonerName = new HashMap<>();
        itemID_ItemImage = new HashMap<>();
        champID = -1;
        sum1ID = -1;
        sum2ID = -1;
        itemID = new Integer[6];
        sides = new PGraphics[6];
        dices = new PShape[6];
        g = new PShape();
        k = new PShape();
        o = new PShape();
        clipB = Toolkit.getDefaultToolkit().getSystemClipboard();
        setSides();
        reroll = new Button(width / 4f, height - 50, 50, 50, ELLIPSE, "reroll");
        copy = new Button((3 * width) / 4f, height - 50, 50, 50, ELLIPSE, "copy");
        export = new Button(width / 2f, height - 50, 50, 50, ELLIPSE, "export");
        buttons = new ArrayList<>();
        buttons.add(reroll);
        buttons.add(copy);
        buttons.add(export);
        split = new String[]{"Test ", "your ", "Braverieness"};
        splitInts = new Integer[]{6};
        calibri = loadFont("Calibri-36.vlw");
        textFont(calibri);
        createDice();
        createCopy();
        createExport();
        loadMeta();
    }

    @Override
    public void draw() {
        background(220);
        drawBack();
        drawText();
        copy.drawMe(copy,k, this);
        export.drawMe(export,o, this);
        isgRotation = reroll.drawMe(reroll,g, this);
        if (isgRotation) {
            g.rotateY((float) 0.03);
            g.rotateX((float) 0.03);
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
            if (b == reroll) {
                if (b.inMargin(this) && b.getPressed())
                    roll();
            }
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

    public void loadMeta() {
        JSONObject json_data = new JSONObject();

        try {
            URL url = new URL("https://ddragon.leagueoflegends.com/api/versions.json");
            URLConnection con = url.openConnection();
            con.connect();

            StringBuilder textBuilder = new StringBuilder();
            try {
                Reader reader = new BufferedReader(new InputStreamReader
                        ((InputStream) con.getContent(), StandardCharsets.UTF_8));
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            } catch (Exception e) {
                print("konnte keine Verbindung aufbauen: " + e);
            }
            version = textBuilder.substring(textBuilder.toString().indexOf("\"") + 1, textBuilder.toString().indexOf("\"", textBuilder.toString().indexOf("\"") + 1));

            url = new URL("http://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/item.json");
            con = url.openConnection();
            con.connect();
            JSONTokener tokener = new JSONTokener((InputStream) con.getContent());
            items_json = new JSONObject(tokener);
            json_data = items_json.getJSONObject("data");

            url = new URL("http://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/champion.json");
            con = url.openConnection();
            con.connect();
            tokener = new JSONTokener((InputStream) con.getContent());
            champs_json = new JSONObject(tokener);

            url = new URL("http://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/summoner.json");
            con = url.openConnection();
            con.connect();
            tokener = new JSONTokener((InputStream) con.getContent());
            summenors_json = new JSONObject(tokener);

        } catch (Exception e) {
            print("konnte keine Verbindung aufbauen: " + e);
        }

        for (String s : json_data.keySet()) {
            // Map SR
            if (!json_data.getJSONObject(s).getJSONObject("maps").getBoolean("11")) {
                continue;
            }
            for (int i = 0; i < json_data.getJSONObject(s).getJSONArray("tags").length(); i++) {
                if (json_data.getJSONObject(s).getJSONArray("tags").getString(i).contains("Trinket")
                        || json_data.getJSONObject(s).getJSONArray("tags").getString(i).contains("Jungle")
                        || json_data.getJSONObject(s).has("consumed")
                        || json_data.getJSONObject(s).getJSONArray("tags").getString(i).contains("GoldPer")
                        || json_data.getJSONObject(s).getJSONArray("tags").getString(i).contains("Lane")) {
                    break;
                } else {
                    //boots
                    if (json_data.getJSONObject(s).getJSONArray("tags").getString(i).contains("Boots")
                            && !json_data.getJSONObject(s).has("into")) {
                        itemID_itemName_Boots.put(Integer.parseInt(s), json_data.getJSONObject(s).getString("name"));
                        break;
                    } else
                        //Mythics
                        if (i + 1 == json_data.getJSONObject(s).getJSONArray("tags").length()
                                && json_data.getJSONObject(s).getString("description").contains("Mythic Passive:")
                                && !json_data.getJSONObject(s).has("requiredAlly")
                                && !json_data.getJSONObject(s).has("inStore")) {
                            itemID_itemName_Mythic.put(Integer.parseInt(s), json_data.getJSONObject(s).getString("name"));
                            break;
                        } else if (i + 1 == json_data.getJSONObject(s).getJSONArray("tags").length()
                                && !json_data.getJSONObject(s).has("requiredAlly")
                                && !json_data.getJSONObject(s).has("requiredChampion")
                                && !json_data.getJSONObject(s).has("inStore")
                                && !json_data.getJSONObject(s).has("into")) {
                            itemID_itemName.put(Integer.parseInt(s), json_data.getJSONObject(s).getString("name"));
                            break;
                        }
                }
            }
        }

        println("\nBoots count: " + itemID_itemName_Boots.size() + "\nBoots:\n" + itemID_itemName_Boots);
        println("Mythic count: " + itemID_itemName_Mythic.size() + "\nMythic:\n" + itemID_itemName_Mythic);
        println("items count: " + itemID_itemName.size() + "\nItems:\n" + itemID_itemName);
        //Items done Champs next

        for (String s : champs_json.getJSONObject("data").keySet()) {
            champID_champName.put(Integer.parseInt(champs_json.getJSONObject("data").getJSONObject(s).getString("key")), champs_json.getJSONObject("data").getJSONObject(s).getString("name"));
            champID_champRange.put(Integer.parseInt(champs_json.getJSONObject("data").getJSONObject(s).getString("key")), champs_json.getJSONObject("data").getJSONObject(s).getJSONObject("stats").getInt("attackrange"));
        }
        //champs done summoners next

        for (String s : summenors_json.getJSONObject("data").keySet()) {
            for (int i = 0; i < summenors_json.getJSONObject("data").getJSONObject(s).getJSONArray("modes").length(); i++) {
                if (summenors_json.getJSONObject("data").getJSONObject(s).getJSONArray("modes").getString(i).contains("CLASSIC"))
                    summonerID_summonerName.put(Integer.parseInt(summenors_json.getJSONObject("data").getJSONObject(s).getString("key")), summenors_json.getJSONObject("data").getJSONObject(s).getString("name"));
            }
        }
        //Summenors done Images next

        ExecutorService exService = Executors.newFixedThreadPool(8);

        itemID_itemName.keySet().forEach(entry -> exService.execute(() -> {
            String url = "http://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + entry + ".png";
            itemID_ItemImage.put(entry, loadImage(url));
        }));
        itemID_itemName_Boots.keySet().forEach(entry -> exService.execute(() -> {
            String url = "http://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + entry + ".png";
            itemID_ItemImage.put(entry, loadImage(url));
        }));
        itemID_itemName_Mythic.keySet().forEach(entry -> exService.execute(() -> {
            String url = "http://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + entry + ".png";
            itemID_ItemImage.put(entry, loadImage(url));
        }));

        exService.shutdown();
        try {
            exService.awaitTermination(3, TimeUnit.MINUTES);}
        catch( InterruptedException e) {e.printStackTrace();}
    }


    public void roll() {

        String champ, sum1, sum2;
        String path;
        String max;
        String[] item = new String[6];

        Object[] keys = champID_champName.keySet().toArray();
        champID = (Integer) keys[parseInt(random(0, keys.length))];
        champ = champID_champName.get(champID);

        keys = summonerID_summonerName.keySet().toArray();
        sum1ID = (Integer) keys[parseInt(random(0, keys.length))];
        do
            sum2ID = (Integer) keys[parseInt(random(0, keys.length))];
        while (Objects.equals(sum2ID, sum1ID));
        sum1 = summonerID_summonerName.get(sum1ID);
        sum2 = summonerID_summonerName.get(sum2ID);

        max = switch (parseInt(random(0, 3))) {
            case 0 -> "Q";
            case 1 -> "W";
            case 2 -> "E";
            default -> "R";
        };

        path = switch (parseInt(random(0, 4))) {
            case 0 -> "Precision";
            case 1 -> "Domination";
            case 2 -> "Sorcery";
            case 3 -> "Resolve";
            default -> "Inspiration";
        };

        keys = itemID_itemName_Boots.keySet().toArray();
        itemID[0] = (Integer) keys[parseInt(random(0, keys.length))];
        item[0] = itemID_itemName_Boots.get(itemID[0]);
        keys = itemID_itemName.keySet().toArray();
        for (int i = 1; i < item.length - 1; i++) {
            itemID[i] = (Integer) keys[parseInt(random(0, keys.length))];

            for (int j = 1; j < i; j++) {
                if (Objects.equals(itemID[i], itemID[j])) {
                    i--;
                    continue;
                }
                if (itemID_itemName.get(itemID[i]).contains("Hydra")
                        && itemID_itemName.get(itemID[j]).contains("Hydra")) {
                    i--;
                    continue;
                }
                if ((itemID_itemName.get(itemID[i]).contains("Manamune") || itemID_itemName.get(itemID[i]).contains("Winter's Approach") || itemID_itemName.get(itemID[i]).contains("Archangel's Staff"))
                        && (itemID_itemName.get(itemID[j]).contains("Manamune") || itemID_itemName.get(itemID[j]).contains("Winter's Approach") || itemID_itemName.get(itemID[j]).contains("Archangel's Staff"))) {
                    i--;
                    continue;
                }
                if ((itemID_itemName.get(itemID[i]).contains("Lord Dominik's Regards") || itemID_itemName.get(itemID[i]).contains("Serylda's Grudge"))
                        && (itemID_itemName.get(itemID[j]).contains("Lord Dominik's Regards") || itemID_itemName.get(itemID[j]).contains("Serylda's Grudge"))) {
                    i--;
                    continue;
                }
                if ((itemID_itemName.get(itemID[i]).contains("Guinsoo's Rageblade") || itemID_itemName.get(itemID[i]).contains("Infinity Edge"))
                        && (itemID_itemName.get(itemID[j]).contains("Guinsoo's Rageblade") || itemID_itemName.get(itemID[j]).contains("Infinity Edge"))) {
                    i--;
                    continue;
                }
                if ((itemID_itemName.get(itemID[i]).contains("Mercurial Scimitar") || itemID_itemName.get(itemID[i]).contains("Silvermere Dawn"))
                        && (itemID_itemName.get(itemID[j]).contains("Mercurial Scimitar") || itemID_itemName.get(itemID[j]).contains("Silvermere Dawn"))) {
                    i--;
                    continue;
                }
                if ((itemID_itemName.get(itemID[i]).contains("Sterak's Gage") || itemID_itemName.get(itemID[i]).contains("Maw of Malmortius") || itemID_itemName.get(itemID[i]).contains("Immortal Shieldbow"))
                        && (itemID_itemName.get(itemID[j]).contains("Sterak's Gage") || itemID_itemName.get(itemID[j]).contains("Maw of Malmortius") || itemID_itemName.get(itemID[j]).contains("Immortal Shieldbow"))) {
                    i--;
                }
            }

            if (champ.contains("Elise")
                    || champ.contains("Gnar")
                    || champ.contains("Jayce")
                    || champ.contains("Kayle")
                    || champ.contains("Nidalee"))
                continue;

            if (champID_champRange.get(champID) < 275) {
                if (itemID_itemName.get(itemID[i]).contains("Runaan's Hurricane"))
                    i--;
            } else {
                System.out.println("aktuell keine Begrenzung für ranged Champs");
                /*if(itemID_itemName.get(itemID[i]).contains("Hydra")
                || itemID_itemName.get(itemID[i]).contains("Sterak's Gage")){
                i--;
                }*/
            }
        }
        for (int i = 1; i < itemID.length - 1; i++) {
            item[i] = itemID_itemName.get(itemID[i]);
        }

        keys = itemID_itemName_Mythic.keySet().toArray();
        boolean banShieldbow = false;
        for (int i = 1; i < item.length - 1; i++) {
            if (item[i].contains("Sterak's Gage") || item[i].contains("Maw of Malmortius")) {
                banShieldbow = true;
                break;
            }
        }
        do {
            itemID[5] = (Integer) keys[parseInt(random(0, keys.length))];
        } while (banShieldbow && itemID[5] == 6673);
        item[item.length - 1] = itemID_itemName_Mythic.get(itemID[5]);
        setText(champ, path, sum1, sum2, max, item);
        splitInts = itemID;
    }

    void setText(String champ, String path, String sum1, String sum2, String max, String[] item) {

        split = new String[11];
        split[0] = champ;
        split[1] = path;
        split[2] = sum1;
        split[3] = sum2;
        split[4] = max;
        System.arraycopy(item, 0, split, 5, split.length - 5);
    }

    public void exportToLeague() {
        String h2;
        try {
            h2 = "{\"title\":\"" + split[0] + " - " + split[1] + " - " + split[4]
                    + "\",\"associatedMaps\":[11],\"associatedChampions\":[" + champID
                    + "],\"blocks\":[{\"items\":["
                    + "{\"id\":\"" + itemID[0] + "\",\"count\":1}"
                    + ",{\"id\":\"" + itemID[1] + "\",\"count\":1}"
                    + ",{\"id\":\"" + itemID[2] + "\",\"count\":1}"
                    + ",{\"id\":\"" + itemID[3] + "\",\"count\":1}"
                    + ",{\"id\":\"" + itemID[4] + "\",\"count\":1}"
                    + ",{\"id\":\"" + itemID[5] + "\",\"count\":1}"
                    + "],\"type\":\"" + split[0] + "'s Wasted Gaming Build\"}"
                    + ",{\"items\":[{\"id\":\"2003\",\"count\":1}],\"type\":\"Consumables\"}]}"
            ;
        } catch (ArrayIndexOutOfBoundsException a) {
            h2 = "";
            println("exproterror");
        }
        StringSelection copyText = new StringSelection(h2);
        clipB.setContents(copyText, null);
    }


    public void copyToClip() {
        String h2;
        try {
            h2 = "Champion\t\t    " + split[0]
                    + "\nRunepath\t\t      " + split[1]
                    + "\nSummenors\t\t " + split[2] + " " + split[3]
                    + "\nSpell to max\t\t " + split[4]
                    + "\nItems\t\t\t\t\t  " + split[5]
                    + "\n\t\t\t\t\t\t\t\t" + split[6]
                    + "\n\t\t\t\t\t\t\t\t" + split[7]
                    + "\n\t\t\t\t\t\t\t\t" + split[8]
                    + "\n\t\t\t\t\t\t\t\t" + split[9]
                    + "\n\t\t\t\t\t\t\t\t" + split[10];
        } catch (ArrayIndexOutOfBoundsException a) {
            h2 = "";
        }
        StringSelection copyText = new StringSelection(h2);
        clipB.setContents(copyText, null);
    }


    void drawText() {
        if (Objects.equals(split[0], "Test ")) {
            textSize(32);
            fill(0);
            textAlign(CENTER);
            text("Test Your Braverieness", width / 2f, height / 2f - 45);
            textSize(14);
            textAlign(CORNER);
            text("Version " + appVersion, 3, height - 3);
            textAlign(RIGHT);
            text("Last proven patch: " + provenPatch, width - 3, height - 3);
        } else {
            textSize(32);
            textAlign(CORNER);
            fill(222, 235, 247);
            text("Champion", 30, 53);
            text(split[0], width / 2f, 53);
            text("Runepath", 30, 103);
            text(split[1], width / 2f, 103);
            text("Summenors", 30, 153);
            text(split[2] + ", " + split[3], width / 2f, 153);
            text("Spell to Max", 30, 203);
            text(split[4], width / 2f, 203);
            textSize(36);
            fill(197, 90, 17, 1);
            textSize(42);
            textAlign(CENTER);
            text("Items", width / 2f, 260);
            textSize(32);
            fill(0);
            textAlign(CORNER);
            imageMode(CORNER);
            text(split[5], (float) (width / 3.5), 300);
            image(itemID_ItemImage.get(splitInts[0]), (float) (width / 3.5 - 40), 275, 32, 32);
            text(split[6], (float) (width / 3.5), 340);
            image(itemID_ItemImage.get(splitInts[1]), (float) (width / 3.5 - 40), 315, 32, 32);
            text(split[7], (float) (width / 3.5), 380);
            image(itemID_ItemImage.get(splitInts[2]), (float) (width / 3.5 - 40), 355, 32, 32);
            text(split[8], (float) (width / 3.5), 420);
            image(itemID_ItemImage.get(splitInts[3]), (float) (width / 3.5 - 40), 395, 32, 32);
            text(split[9], (float) (width / 3.5), 460);
            image(itemID_ItemImage.get(splitInts[4]), (float) (width / 3.5 - 40), 435, 32, 32);
            text(split[10], (float) (width / 3.5), 500);
            image(itemID_ItemImage.get(splitInts[5]), (float) (width / 3.5 - 40), 475, 32, 32);
            textSize(14);
            textAlign(CORNER);
            text("Version " + appVersion, 3, height - 3);
            textAlign(RIGHT);
            text("Last proven patch: " + provenPatch, width - 3, height - 3);
        }
    }

    void drawBack() {
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

    void createDice() {
        g = createShape(GROUP);
        strokeWeight(1);
        stroke(180,180,180);
        fill(255,255,255);
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
    }

    void setSides() {
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
    }

    void createCopy() {
        k = createShape(GROUP);
        strokeWeight(3);
        fill(255, 255, 255);
        PShape one = createShape(RECT, 0, 0, 40, 50, 6, 6, 6, 6);
        PShape first = createShape(LINE, 8, 15, 32, 15);
        PShape second = createShape(LINE, 8, 25, 27, 25);
        PShape third = createShape(LINE, 8, 35, 32, 35);
        PShape two = createShape(RECT, 10, -7, 40, 50, 6, 6, 6, 6);
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
        k.scale((float) 0.4);
    }

    void createExport() {
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


}

