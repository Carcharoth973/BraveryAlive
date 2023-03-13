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

    //class wide used Random instance
    private final Random random;
    //class wide used ExecutorService instance
    ExecutorService executorService;
    //current game version used in all urls
    private final String gameVersion;
    private final HashMap<String, Integer> champName_champRange, champName_champId, itemName_itemId;
    private final ArrayList<String> summonerSpells;
    private final JSONObject allItems;
    private final HashMap<String, PImage> bootsName_bootsImage, mythicName_mythicImage, legendaryName_legendaryImage;

    LogicOperator() {
        random = new Random();
        executorService = Executors.newFixedThreadPool(16);
        gameVersion = loadGameVersion();

        allItems = loadAllItems();
        champName_champId = loadChampName_champId();
        itemName_itemId = loadItemName_itemId();
        champName_champRange = loadChampName_champRange();
        summonerSpells = loadSummonerSpells();
        bootsName_bootsImage = loadBootsName_bootsImage();
        mythicName_mythicImage = loadMythicName_mythicImage();
        legendaryName_legendaryImage = loadLegendaryName_legendaryImage();
        executorService.shutdown();
        try {
            System.out.println(
                    executorService.awaitTermination(3, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Boots count: " + bootsName_bootsImage.size() + "\nBoots:\n" + bootsName_bootsImage
                        + "\nMythic count: " + mythicName_mythicImage.size() + "\nMythic:\n" + mythicName_mythicImage
                        + "\nitems count: " + legendaryName_legendaryImage.size() + "\nItems:\n" + legendaryName_legendaryImage);
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

    //return champs with their range from riot server
    private HashMap<String, Integer> loadChampName_champRange() {
        HashMap<String, Integer> returnMap = new HashMap<>();
        JSONObject champs_json = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/champion.json"))
                .getJSONObject("data");
        for (String s : champs_json.keySet()) {
            returnMap.put((String) champs_json.query("/" + s + "/name"), (Integer) champs_json.query("/" + s + "/stats/attackrange"));
        }
        return returnMap;
    }

    //return champs with their range from riot server
    private HashMap<String, Integer> loadChampName_champId() {
        HashMap<String, Integer> returnMap = new HashMap<>();
        JSONObject champs_json = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/champion.json"))
                .getJSONObject("data");
        for (String s : champs_json.keySet()) {
            returnMap.put((String) champs_json.query("/" + s + "/name"),  champs_json.getJSONObject(s).getInt("key"));
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

    //return items filtered to be boots from riot server
    private HashMap<String, PImage> loadBootsName_bootsImage() {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Boots")
                    && !allItems.getJSONObject(s).has("into"))
                outputHashMap.put((String) allItems.query("/" + s + "/name"), s);
        }
        return loadItemImages(outputHashMap);
    }

    //return items filtered to be mythic items from riot server
    private HashMap<String, PImage> loadMythicName_mythicImage() {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            if (allItems.getJSONObject(s).getString("description").contains("Mythic Passive:")
                    && !allItems.getJSONObject(s).has("inStore") //inStore only exists if false
            )
                outputHashMap.put((String) allItems.query("/" + s + "/name"), s);
        }
        return loadItemImages(outputHashMap);
    }

    // return items filtered to be legendary items from riot server
    private HashMap<String, PImage> loadLegendaryName_legendaryImage() {
        HashMap<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            String helpName = (String)allItems.query("/" + s + "/name");
            if (    !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Boots")
                    && !allItems.getJSONObject(s).getString("description").contains("Mythic Passive:")
                    && !allItems.getJSONObject(s).has("requiredAlly")
                    && !allItems.getJSONObject(s).has("requiredChampion")
                    && !allItems.getJSONObject(s).has("inStore") //inStore only exists if false
                    && !allItems.getJSONObject(s).has("into")
                    && !bootsName_bootsImage.containsKey(helpName)
                    && !mythicName_mythicImage.containsKey(helpName))
                outputHashMap.put(helpName, s);
        }
        return loadItemImages(outputHashMap);
    }

    // return items filtered to be legendary items from riot server
    private HashMap<String, Integer> loadItemName_itemId() {
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (String s : allItems.keySet()) {

                returnMap.put((String)allItems.query("/" + s + "/name"), Integer.parseInt(s));
        }
        return returnMap;
    }

    //return all items with their range from riot server
    private JSONObject loadAllItems() {
        JSONObject returnJsonObject = new JSONObject();
        JSONObject allItems = Objects.requireNonNull(
                        loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/item.json"))
                .getJSONObject("data");
        for (String s : allItems.keySet()) {
            if ((boolean) allItems.query("/" + s + "/maps/11")
                    && !allItems.getJSONObject(s).has("consumed")
                    && !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Trinket")
                    && !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Jungle")
                    && !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("GoldPer")
                    && !allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Lane"))
                returnJsonObject.put(s, allItems.getJSONObject(s));
        }
        return returnJsonObject;
    }

    //returns a Map with names and images for items. Expects a Map with item names and ids
    private HashMap<String, PImage> loadItemImages(HashMap<String, String> inputMap) {
        PApplet pApplet = new PApplet();
        HashMap<String, PImage> returnMap = new HashMap<>();
        for (String s : inputMap.keySet()) {
            executorService.execute(() ->
                    returnMap.put(s, pApplet.loadImage("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/img/item/" + inputMap.get(s) + ".png")));
        };
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
    public String getNewChamp() {
        String[] champNames = champName_champRange.keySet().toArray(new String[0]);
        return champNames[random.nextInt(champNames.length)];
    }

    //return new random boots to pick
    public Pair getNewBoots() {
        String value = bootsName_bootsImage.keySet().toArray(new String[0])
                [random.nextInt(bootsName_bootsImage.size())];
        return new Pair(value, bootsName_bootsImage.get(value));
    }

    //return a new random mythic item to pick
    public Pair getNewMythic() {
        String value = mythicName_mythicImage.keySet().toArray(new String[0])
                [random.nextInt(mythicName_mythicImage.size())];
        return new Pair(value, mythicName_mythicImage.get(value));
    }

    //return a new random legendary item to pick
    public ArrayList<Pair> getNewLegendary(String champ, int count) {
        ArrayList<String> outputList = new ArrayList<>();
        ArrayList<Pair> returnList = new ArrayList<>();
        String[] rollingArray = legendaryName_legendaryImage.keySet().toArray(new String[0]);
        String[] nameHolder = {"Maw of Malmortius","Archangel's Staff","Sterak's Gage"};
        ArrayList<String> lifelineGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Winter's Approach","Archangel's Staff","Manamune"};
        ArrayList<String> aweGroup = new ArrayList<>(Arrays.asList(nameHolder));;
        nameHolder = new String[]{"Ravenous Hydra","Titanic Hydra"};
        ArrayList<String> hydraGroup = new ArrayList<>(Arrays.asList(nameHolder));;
        nameHolder = new String[]{"Silvermere Dawn","Mercurial Scimitar"};
        ArrayList<String> qssGroup = new ArrayList<>(Arrays.asList(nameHolder));;
        nameHolder = new String[]{"Navori Quickblades","Spear of Shojin"};
        ArrayList<String> cdrGroupOne = new ArrayList<>(Arrays.asList(nameHolder));;
        nameHolder = new String[]{"Guinsoo's Rageblade","Infinity Edge","Navori Quickblades"};
        ArrayList<String> criticalGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Serylda's Grudge","Lord Dominik's Regards","Mortal Reminder"};
        ArrayList<String> penetrationGroup = new ArrayList<>(Arrays.asList(nameHolder));
        nameHolder = new String[]{"Runaan's Hurricane"};
        ArrayList<String> rangedOnly = new ArrayList<>(Arrays.asList(nameHolder));
        for (int i = 0; i<count; i++){
            String value = rollingArray[random.nextInt(rollingArray.length)];
            if (outputList.contains(value)
                || (lifelineGroup.contains(value) && !Collections.disjoint(outputList,lifelineGroup))
                || (aweGroup.contains(value) && !Collections.disjoint(outputList,aweGroup))
                || (hydraGroup.contains(value) && !Collections.disjoint(outputList,hydraGroup))
                || (qssGroup.contains(value) && !Collections.disjoint(outputList,qssGroup))
                || (cdrGroupOne.contains(value) && !Collections.disjoint(outputList,cdrGroupOne))
                || (criticalGroup.contains(value) && !Collections.disjoint(outputList,criticalGroup))
                || (penetrationGroup.contains(value) && !Collections.disjoint(outputList,penetrationGroup))
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