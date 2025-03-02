import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//class for all logic Operations including data load/transform, rolling, export string generation
public class LogicOperator {

    //class-wide used Random instance
    private final Random random;
    //class-wide used ExecutorService instance
    ExecutorService executorService;
    //current game version used in all urls
    private final String gameVersion;
    private final HashMap<String, Integer> champName_champRange, champName_champId, itemName_itemId;
    private final ArrayList<String> summonerSpells;
    private final Map<String, PImage> bootsName_bootsImage, legendaryName_legendaryImage, champName_champImage;

    LogicOperator() {
        random = new Random();
        executorService = Executors.newFixedThreadPool(16);
        gameVersion = loadGameVersion();
        JSONObject allItems = loadAllItems();
        JSONObject allChamps = loadAllChamps();
        champName_champId = loadChampName_champId(allChamps);
        champName_champRange = loadChampName_champRange(allChamps);
        champName_champImage = Collections.synchronizedMap(loadChampName_champImage(allChamps));
        summonerSpells = loadSummonerSpells();
        itemName_itemId = loadItemName_itemId(allItems);
        bootsName_bootsImage = Collections.synchronizedMap(loadBootsName_bootsImage(allItems));
        legendaryName_legendaryImage = Collections.synchronizedMap(loadLegendaryName_legendaryImage(allItems));
        executorService.shutdown();
        try {
            System.out.println("Starting image loading...");
            System.out.println("Image loading success: " + executorService.awaitTermination(3, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Boots count: " + bootsName_bootsImage.size() + "\nBoots:\n" + bootsName_bootsImage.keySet()
                        + "\nitems count: " + legendaryName_legendaryImage.size() + "\nItems:\n" + legendaryName_legendaryImage.keySet());
    }

    //return current game version from riot server
    private String loadGameVersion() {
        //StringBuilder is needed since the json is not properly formatted for the JSONTokener
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //establish connection to League versions.json
            URL url = new URL("https://ddragon.leagueoflegends.com/api/versions.json");
            URLConnection con = url.openConnection();
            con.connect();

            Reader reader = new BufferedReader(new InputStreamReader(
                    (InputStream) con.getContent(), StandardCharsets.UTF_8));
            boolean isInFirstQuotation = false;
            int c;
            //read the first String within Quotations which corresponds to the current League version
            while ((c = reader.read()) != -1) {
                if ((char) c == '"')
                    if (isInFirstQuotation)
                        break;
                    else
                        isInFirstQuotation = true;
                else if ((isInFirstQuotation))
                    stringBuilder.append((char) c);
            }
        } catch (java.net.MalformedURLException urlException) {
            System.out.println("java.net.MalformedURLException occurred:");
            urlException.printStackTrace();
        } catch (IOException ioException) {
            System.out.println("IOException occurred:");
            ioException.printStackTrace();
        }
        return stringBuilder.toString();
    }

    //return all items with their range from riot server
    private JSONObject loadAllItems() {
        JSONObject returnJsonObject = new JSONObject();
        HashMap<String,ArrayList<String>> discardedItems = new HashMap<>() {{
            put("map is not SR", new ArrayList<>());
            put("has consumable", new ArrayList<>());
            put("tag = Trinket", new ArrayList<>());
            put("tag = Jungle", new ArrayList<>());
            put("tag = GoldPer", new ArrayList<>());
            put("tag = Lane", new ArrayList<>());
            put("dedicated removal", new ArrayList<>());
            put("inStore null/false", new ArrayList<>());
        }};
        JSONObject allItems = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/item.json"))
                .getJSONObject("data");
        for (String s : allItems.keySet()) {
            if (!(boolean) allItems.query("/" + s + "/maps/11"))
                discardedItems.get("map is not SR").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).has("consumed"))
                discardedItems.get("has consumable").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Trinket"))
                discardedItems.get("tag = Trinket").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Jungle"))
                discardedItems.get("tag = Jungle").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("GoldPer"))
                discardedItems.get("tag = GoldPer").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Lane"))
                discardedItems.get("tag = Lane").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).getString("name").equals("Empyrean Promise"))
                discardedItems.get("dedicated removal").add(allItems.getJSONObject(s).getString("name"));
            else if (allItems.getJSONObject(s).has("inStore"))
                discardedItems.get("inStore null/false").add(allItems.getJSONObject(s).getString("name"));
            else
                returnJsonObject.put(s, allItems.getJSONObject(s));
        }
        for(String s: discardedItems.keySet()){
            System.out.println("Discarded Items for Reason: " + s + " Size: "+ discardedItems.get(s).size()+ "\n" + discardedItems.get(s));
        }
        return returnJsonObject;
    }

    //return all items with their range from riot server
    private JSONObject loadAllChamps() {
        JSONObject returnJsonObject = new JSONObject();
        JSONObject allChamps = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/champion.json"))
                .getJSONObject("data");
        for (String s : allChamps.keySet()) {
            returnJsonObject.put(s, allChamps.getJSONObject(s));
        }
        return returnJsonObject;
    }

    //return champs with their range from riot server
    private HashMap<String, Integer> loadChampName_champRange(JSONObject champJson) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (String s : champJson.keySet()) {
            returnMap.put((String) champJson.query("/" + s + "/name"), (Integer) champJson.query("/" + s + "/stats/attackrange"));
        }
        return returnMap;
    }

    //return champs with their range from riot server
    private HashMap<String, Integer> loadChampName_champId(JSONObject champJson) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (String s : champJson.keySet()) {
            returnMap.put((String) champJson.query("/" + s + "/name"),  champJson.getJSONObject(s).getInt("key"));
        }
        return returnMap;
    }

    //return summoner spells with "CLASSIC" in modes values
    private ArrayList<String> loadSummonerSpells() {
        ArrayList<String> returnList = new ArrayList<>();
        JSONObject summoners_json = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/summoner.json"))
                .getJSONObject("data");
        for (String s : summoners_json.keySet()) {
            if (summoners_json.getJSONObject(s).getJSONArray("modes").toString().contains("CLASSIC"))
                returnList.add((String) summoners_json.query("/" + s + "/name"));
        }
        return returnList;
    }

    //return items filtered to be boots from riot server with image data
    private HashMap<String, PImage> loadChampName_champImage(JSONObject allChamps) {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allChamps.keySet()) {
            outputHashMap.put((String) allChamps.query("/" + s + "/name"), s);
        }
        return loadImages(outputHashMap, "champion");
    }

    //return items filtered to be boots from riot server with image data
    private HashMap<String, PImage> loadBootsName_bootsImage(JSONObject allItems) {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            System.out.println("item=" +s);
            try{
                if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Boots")
                        && allItems.getJSONObject(s).getInt("depth") == 2)
                    outputHashMap.put((String) allItems.query("/" + s + "/name"), s);
            } catch (JSONException ignored) {} // some items don't have "depth"
        }
        return loadImages(outputHashMap, "item");
    }

    // return items filtered to be legendary items from riot server with image data
    private HashMap<String, PImage> loadLegendaryName_legendaryImage(JSONObject allItems) {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            String helpName = (String)allItems.query("/" + s + "/name");
            if (    !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Boots")
                    && !allItems.getJSONObject(s).has("requiredAlly")
                    && !allItems.getJSONObject(s).has("requiredChampion")
                    && !allItems.getJSONObject(s).has("consumeOnFull")
                    && !bootsName_bootsImage.containsKey(helpName))
                if((allItems.getJSONObject(s).has("into"))) {
                    boolean allRequireAlly = true;
                    for (Object st : allItems.getJSONObject(s).getJSONArray("into").toList()) {
                        if(allItems.has((String)st))
                            if (!allItems.getJSONObject((String)st).has("requiredAlly")) {
                                allRequireAlly = false;
                                break;
                            }
                    }
                    if (allRequireAlly)
                        outputHashMap.put(helpName, s);
                }
                else
                    outputHashMap.put(helpName, s);
        }
        return loadImages(outputHashMap, "item");
    }

    // return all item names with corresponding ids from riot server
    private HashMap<String, Integer> loadItemName_itemId(JSONObject allItems) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (String s : allItems.keySet()) {
                returnMap.put((String)allItems.query("/" + s + "/name"), Integer.parseInt(s));
        }
        return returnMap;
    }

    //returns a Map with names and images for items. Expects a Map with item names and ids
    private HashMap<String, PImage> loadImages(HashMap<String, String> inputMap, String type) {
        PApplet pApplet = new PApplet();
        HashMap<String, PImage> returnMap = new HashMap<>();
        for (String s : inputMap.keySet()) {
            executorService.execute(() ->
                    returnMap.put(s, pApplet.loadImage("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/img/" + type + "/" + inputMap.get(s) + ".png")));
        }
        return returnMap;
    }

    //establish a new URLConnection to given address and return the content as parsed JSON
    private JSONObject loadJsonObject(String address) {
        try {
            URL url = new URL(address);
            URLConnection con = url.openConnection();
            con.connect();
            JSONTokener JsonTokener = new JSONTokener((InputStream) con.getContent());
            return new JSONObject(JsonTokener);
        } catch (MalformedURLException urlException) {
            System.out.println("java.net.MalformedURLException occurred:");
            urlException.printStackTrace();
        } catch (IOException ioException) {
            System.out.println("IOException occurred:");
            ioException.printStackTrace();
        }
        return null;
    }

    //return current gameVersion
    public String getGameVersion() {
        return gameVersion;
    }

    //return a new random skill to max
    public String getNewSkillMaxing() {
        return switch (random.nextInt(3)) {
            case 0 -> "Q";
            case 1 -> "W";
            case 2 -> "E";
            default -> "Error";
        };
    }

    //return a new random mastery to pick
    public String getNewMastery() {
        return switch (random.nextInt(4)) {
            case 0 -> "Precision";
            case 1 -> "Domination";
            case 2 -> "Sorcery";
            case 3 -> "Resolve";
            default -> "Error";
        };
    }

    //return a new summoner spell to pick
    public String getNewSummonerSpell(String rolledSummonerSpell) {
        String returnSummonerSpell;
        do {
            returnSummonerSpell = summonerSpells.get(random.nextInt(summonerSpells.size()));
        } while (returnSummonerSpell.equals(rolledSummonerSpell));
        return returnSummonerSpell;
    }

    //return a new random champ to pick
    public Pair getNewChamp() {
        String value = champName_champImage.keySet().toArray(new String[champName_champImage.size()])
                [random.nextInt(champName_champImage.size())];
        return new Pair(value, champName_champImage.get(value));
    }

    //return new random boots to pick
    public Pair getNewBoots() {
        String value = bootsName_bootsImage.keySet().toArray(new String[0])
                [random.nextInt(bootsName_bootsImage.size())];
        return new Pair(value, bootsName_bootsImage.get(value));
    }

    //return a new random legendary item to pick
    public ArrayList<Pair> getNewLegendary(String champ, int count) {
        ArrayList<String> outputList = new ArrayList<>();
        ArrayList<Pair> returnList = new ArrayList<>();
        String[] rollingArray = legendaryName_legendaryImage.keySet().toArray(new String[0]);
        String[] nameHolder = {"Banshee's Veil","Edge of Night"};
        ArrayList<String> annulGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Bloodletter's Curse","Cryptbloom","Terminus", "Void Staff"};
        ArrayList<String> blightGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Black Cleaver","Serylda's Grudge","Lord Dominik's Regards","Mortal Reminder","Terminus"};
        ArrayList<String> fatalityGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Profane Hydra","Ravenous Hydra","Titanic Hydra","Stridebreaker"};
        ArrayList<String> hydraGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Sunfire Aegis","Hollow Radiance"};
        ArrayList<String> immolateGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Maw of Malmortius","Archangel's Staff","Immortal Shieldbow","Sterak's Gage"};
        ArrayList<String> lifelineGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Winter's Approach","Archangel's Staff","Manamune"};
        ArrayList<String> manaflowGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Dead Man's Plate","Trailblazer"};
        ArrayList<String> momentumGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Iceborn Gauntlet","Lich Bane","Trinity Force"};
        ArrayList<String> unboundedGroupOne = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Runaan's Hurricane"};
        ArrayList<String> rangedOnly = new ArrayList<>(Arrays.asList(nameHolder));
        for (int i = 0; i<count; i++){
            String value = rollingArray[random.nextInt(rollingArray.length)];
            if (outputList.contains(value)
                || (annulGroup.contains(value) && !Collections.disjoint(outputList,annulGroup))
                || (blightGroup.contains(value) && !Collections.disjoint(outputList,blightGroup))
                || (fatalityGroup.contains(value) && !Collections.disjoint(outputList,fatalityGroup))
                || (hydraGroup.contains(value) && !Collections.disjoint(outputList,hydraGroup))
                || (immolateGroup.contains(value) && !Collections.disjoint(outputList,immolateGroup))
                || (lifelineGroup.contains(value) && !Collections.disjoint(outputList,lifelineGroup))
                || (manaflowGroup.contains(value) && !Collections.disjoint(outputList,manaflowGroup))
                || (momentumGroup.contains(value) && !Collections.disjoint(outputList,momentumGroup))
                || (unboundedGroupOne.contains(value) && !Collections.disjoint(outputList,unboundedGroupOne))
                || (champName_champRange.get(champ) < 275 && rangedOnly.contains(value))
            ){
                i--;
            }
            else
                outputList.add(value);
        }
        for (String s: outputList)
            returnList.add(new Pair(s,legendaryName_legendaryImage.get(s)));
       return returnList;
    }

    //return corresponding ID for a champ Name
    public Integer getChampIdFromName(String champName) {
        return champName_champId.get(champName);
    }

    public ArrayList<Integer> getItemIdFromName (ArrayList<Pair> itemBuild) {
        ArrayList<Integer> returnList = new ArrayList<>();
        for(Pair p: itemBuild){
            returnList.add(itemName_itemId.get(p.name()));
        }
        return returnList;
    }
}