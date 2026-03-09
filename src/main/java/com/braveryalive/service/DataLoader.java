package com.braveryalive.service;

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

/**
 * Responsible for loading data from Riot's API.
 */
import org.springframework.stereotype.Service;

@Service
public class DataLoader {
    private final ExecutorService executorService;
    private final String gameVersion;
    private final Map<String, Integer> champNameChampId;
    private final Map<String, Integer> champNameChampRange;
    private final Map<String, PImage> champNameChampImage;
    private final List<String> summonerSpells;
    private final Map<String, Integer> itemNameItemId;
    private final Map<String, PImage> bootsNameBootsImage;
    private final Map<String, PImage> legendaryNameLegendaryImage;

    public DataLoader() {
        this.executorService = Executors.newFixedThreadPool(16);
        this.gameVersion = loadGameVersion();
        JSONObject allItems = loadAllItems();
        JSONObject allChamps = loadAllChamps();
        this.champNameChampId = loadChampNameChampId(allChamps);
        this.champNameChampRange = loadChampNameChampRange(allChamps);
        this.champNameChampImage = Collections.synchronizedMap(loadChampNameChampImage(allChamps));
        this.summonerSpells = loadSummonerSpells();
        this.itemNameItemId = loadItemNameItemId(allItems);
        this.bootsNameBootsImage = Collections.synchronizedMap(loadBootsNameBootsImage(allItems));
        this.legendaryNameLegendaryImage = Collections.synchronizedMap(loadLegendaryNameLegendaryImage(allItems));
        executorService.shutdown();
        try {
            System.out.println("Starting image loading...");
            System.out.println("Image loading success: " + executorService.awaitTermination(3, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Boots count: " + bootsNameBootsImage.size() + "\nBoots:\n" + bootsNameBootsImage.keySet()
                + "\nitems count: " + legendaryNameLegendaryImage.size() + "\nItems:\n" + legendaryNameLegendaryImage.keySet());
    }

    private String loadGameVersion() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL("https://ddragon.leagueoflegends.com/api/versions.json");
            URLConnection con = url.openConnection();
            con.connect();
            Reader reader = new BufferedReader(new InputStreamReader(
                    (InputStream) con.getContent(), StandardCharsets.UTF_8));
            boolean isInFirstQuotation = false;
            int c;
            while ((c = reader.read()) != -1) {
                if ((char) c == '"') {
                    if (isInFirstQuotation) break;
                    else isInFirstQuotation = true;
                } else if (isInFirstQuotation) stringBuilder.append((char) c);
            }
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    private JSONObject loadAllItems() {
        JSONObject returnJsonObject = new JSONObject();
        Map<String, List<String>> discardedItems = new HashMap<>();
        discardedItems.put("map is not SR", new ArrayList<>());
        discardedItems.put("has consumable", new ArrayList<>());
        discardedItems.put("tag = Trinket", new ArrayList<>());
        discardedItems.put("tag = Jungle", new ArrayList<>());
        discardedItems.put("tag = GoldPer", new ArrayList<>());
        discardedItems.put("tag = Lane", new ArrayList<>());
        discardedItems.put("dedicated removal", new ArrayList<>());
        discardedItems.put("inStore null/false", new ArrayList<>());
        JSONObject allItems = Objects.requireNonNull(
                loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/item.json"))
                .getJSONObject("data");
        for (String s : allItems.keySet()) {
            JSONObject item = allItems.getJSONObject(s);
            if (!(boolean) item.query("/maps/11")) {
                discardedItems.get("map is not SR").add(item.getString("name"));
            } else if (item.has("consumed")) {
                discardedItems.get("has consumable").add(item.getString("name"));
            } else if (item.getJSONArray("tags").toString().contains("Trinket")) {
                discardedItems.get("tag = Trinket").add(item.getString("name"));
            } else if (item.getJSONArray("tags").toString().contains("Jungle")) {
                discardedItems.get("tag = Jungle").add(item.getString("name"));
            } else if (item.getJSONArray("tags").toString().contains("GoldPer")) {
                discardedItems.get("tag = GoldPer").add(item.getString("name"));
            } else if (item.getJSONArray("tags").toString().contains("Lane")) {
                discardedItems.get("tag = Lane").add(item.getString("name"));
            } else if (item.getString("name").equals("Empyrean Promise")) {
                discardedItems.get("dedicated removal").add(item.getString("name"));
            } else if (item.has("inStore")) {
                discardedItems.get("inStore null/false").add(item.getString("name"));
            } else {
                returnJsonObject.put(s, item);
            }
        }
        for (String reason : discardedItems.keySet()) {
            System.out.println("Discarded Items for Reason: " + reason + " Size: " + discardedItems.get(reason).size() + "\n" + discardedItems.get(reason));
        }
        return returnJsonObject;
    }

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

    private Map<String, Integer> loadChampNameChampRange(JSONObject champJson) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (String s : champJson.keySet()) {
            returnMap.put((String) champJson.query("/" + s + "/name"), (Integer) champJson.query("/" + s + "/stats/attackrange"));
        }
        return returnMap;
    }

    private Map<String, Integer> loadChampNameChampId(JSONObject champJson) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (String s : champJson.keySet()) {
            returnMap.put((String) champJson.query("/" + s + "/name"), champJson.getJSONObject(s).getInt("key"));
        }
        return returnMap;
    }

    private List<String> loadSummonerSpells() {
        List<String> returnList = new ArrayList<>();
        JSONObject summonersJson = Objects.requireNonNull(
                loadJsonObject("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/data/en_US/summoner.json"))
                .getJSONObject("data");
        for (String s : summonersJson.keySet()) {
            if (summonersJson.getJSONObject(s).getJSONArray("modes").toString().contains("CLASSIC")) {
                returnList.add((String) summonersJson.query("/" + s + "/name"));
            }
        }
        return returnList;
    }

    private Map<String, PImage> loadChampNameChampImage(JSONObject allChamps) {
        Map<String, String> outputHashMap = new HashMap<>();
        for (String s : allChamps.keySet()) {
            outputHashMap.put((String) allChamps.query("/" + s + "/name"), s);
        }
        return loadImages(outputHashMap, "champion");
    }

    private Map<String, PImage> loadBootsNameBootsImage(JSONObject allItems) {
        Map<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            try {
                if (allItems.getJSONObject(s).getJSONArray("tags").toString().contains("Boots")
                        && allItems.getJSONObject(s).getInt("depth") == 2) {
                    outputHashMap.put((String) allItems.query("/" + s + "/name"), s);
                }
            } catch (JSONException ignored) {
            }
        }
        return loadImages(outputHashMap, "item");
    }

    private Map<String, PImage> loadLegendaryNameLegendaryImage(JSONObject allItems) {
        Map<String, String> outputHashMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            String helpName = (String) allItems.query("/" + s + "/name");
            JSONObject item = allItems.getJSONObject(s);
            if (!item.getJSONArray("tags").toString().contains("Boots")
                    && !item.has("requiredAlly")
                    && !item.has("requiredChampion")
                    && !item.has("consumeOnFull")
                    && !bootsNameBootsImage.containsKey(helpName)) {
                if (item.has("into")) {
                    boolean allRequireAlly = true;
                    for (Object st : item.getJSONArray("into").toList()) {
                        if (allItems.has((String) st)) {
                            if (!allItems.getJSONObject((String) st).has("requiredAlly")) {
                                allRequireAlly = false;
                                break;
                            }
                        }
                    }
                    if (allRequireAlly) outputHashMap.put(helpName, s);
                } else {
                    outputHashMap.put(helpName, s);
                }
            }
        }
        return loadImages(outputHashMap, "item");
    }

    private Map<String, Integer> loadItemNameItemId(JSONObject allItems) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (String s : allItems.keySet()) {
            returnMap.put((String) allItems.query("/" + s + "/name"), Integer.parseInt(s));
        }
        return returnMap;
    }

    private Map<String, PImage> loadImages(Map<String, String> inputMap, String type) {
        PApplet pApplet = new PApplet();
        Map<String, PImage> returnMap = new HashMap<>();
        for (String s : inputMap.keySet()) {
            executorService.execute(() ->
                    returnMap.put(s, pApplet.loadImage("http://ddragon.leagueoflegends.com/cdn/" + gameVersion + "/img/" + type + "/" + inputMap.get(s) + ".png")));
        }
        return returnMap;
    }

    private JSONObject loadJsonObject(String address) {
        try {
            URL url = new URL(address);
            URLConnection con = url.openConnection();
            con.connect();
            JSONTokener jsonTokener = new JSONTokener((InputStream) con.getContent());
            return new JSONObject(jsonTokener);
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        return null;
    }

    // Getters
    public String getGameVersion() { return gameVersion; }
    public Map<String, Integer> getChampNameChampId() { return champNameChampId; }
    public Map<String, Integer> getChampNameChampRange() { return champNameChampRange; }
    public Map<String, PImage> getChampNameChampImage() { return champNameChampImage; }
    public List<String> getSummonerSpells() { return summonerSpells; }
    public Map<String, Integer> getItemNameItemId() { return itemNameItemId; }
    public Map<String, PImage> getBootsNameBootsImage() { return bootsNameBootsImage; }
    public Map<String, PImage> getLegendaryNameLegendaryImage() { return legendaryNameLegendaryImage; }
}
