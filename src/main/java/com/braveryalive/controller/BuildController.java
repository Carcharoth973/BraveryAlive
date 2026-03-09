package com.braveryalive.controller;

import com.braveryalive.model.BuildResponse;
import com.braveryalive.model.Pair;
import com.braveryalive.config.Mastery;
import com.braveryalive.config.Skill;
import com.braveryalive.service.BuildGenerator;
import com.braveryalive.service.DataLoader;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BuildController implements HttpHandler {
    private final BuildGenerator generator;
    private final DataLoader dataLoader;
    private BuildResponse lastBuild;

    public BuildController() {
        this.dataLoader = new DataLoader();
        this.generator = new BuildGenerator(dataLoader);
    }

    public void handleRoll(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Pair champ = generator.getNewChamp();
        Mastery mastery = generator.getNewMastery();
        String spell1 = generator.getNewSummonerSpell("none");
        String spell2 = generator.getNewSummonerSpell(spell1);
        Skill skill = generator.getNewSkillToMax();
        Pair boots = generator.getNewBoots();
        List<Pair> legendaries = generator.getNewLegendary(champ.name(), 5);

        String version = dataLoader.getGameVersion();
        int champId = dataLoader.getChampNameChampId().get(champ.name());
        String champImg = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/champion/" + champId + ".png";
        Integer bootsId = dataLoader.getItemNameItemId().get(boots.name());
        String bootsImg = bootsId != null ? "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + bootsId + ".png" : "";
        // for items we can try to use the ids stored in dataLoader
        List<String> itemNames = legendaries.stream().map(Pair::name).toList();
        List<String> itemUrls = itemNames.stream().map(name -> {
            Integer id = dataLoader.getItemNameItemId().get(name);
            return id != null ? "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + id + ".png" : "";
        }).toList();

        BuildResponse response = new BuildResponse(
                champ.name(),
                champImg,
                mastery.name(),
                spell1,
                spell2,
                skill.name(),
                boots.name(),
                bootsImg,
                itemNames,
                itemUrls
        );
        storeLastBuild(response);

        String jsonResponse = String.format(
            "{\"champion\":\"%s\",\"championImageUrl\":\"%s\",\"mastery\":\"%s\",\"summonerSpell1\":\"%s\",\"summonerSpell2\":\"%s\",\"skillToMax\":\"%s\",\"boots\":\"%s\",\"bootsImageUrl\":\"%s\",\"items\":%s,\"itemImageUrls\":%s}",
            response.champion(),
            response.championImageUrl(),
            response.mastery(),
            response.summonerSpell1(),
            response.summonerSpell2(),
            response.skillToMax(),
            response.boots(),
            response.bootsImageUrl(),
            formatStringList(response.items()),
            formatStringList(response.itemImageUrls())
        );

        sendJsonResponse(exchange, jsonResponse);
    }

    public void handleVersion(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String version = "\"" + dataLoader.getGameVersion() + "\"";
        sendJsonResponse(exchange, version);
    }

    public void handleExportDiscord(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (lastBuild == null) {
            sendJsonResponse(exchange, "{\"error\":\"No build generated yet\"}");
            return;
        }

        String text = String.format(
                "Champion\\t\\t    %s\\nRune Path\\t\\t      %s\\nSummoners\\t\\t %s %s\\nSpell to max\\t\\t %s\\nItems\\t\\t\\t\\t\\t  %s\\n\\t\\t\\t\\t\\t\\t\\t\\t%s\\n\\t\\t\\t\\t\\t\\t\\t\\t%s\\n\\t\\t\\t\\t\\t\\t\\t\\t%s\\n\\t\\t\\t\\t\\t\\t\\t\\t%s\\n\\t\\t\\t\\t\\t\\t\\t\\t%s",
                lastBuild.champion(),
                lastBuild.mastery(),
                lastBuild.summonerSpell1(), lastBuild.summonerSpell2(),
                lastBuild.skillToMax(),
                lastBuild.boots(),
                lastBuild.items().get(0),
                lastBuild.items().get(1),
                lastBuild.items().get(2),
                lastBuild.items().get(3),
                lastBuild.items().get(4)
        );

        String jsonResponse = String.format("{\"format\":\"discord\",\"content\":\"%s\"}", text.replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t"));
        sendJsonResponse(exchange, jsonResponse);
    }

    public void handleExportLeague(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        if (lastBuild == null) {
            sendJsonResponse(exchange, "{\"error\":\"No build generated yet\"}");
            return;
        }

        List<Integer> itemIDs = generator.getItemIdFromName(generator.getNewLegendary(lastBuild.champion(), 5).stream().map(p -> new Pair(p.name(), null)).toList());
        itemIDs.add(0, generator.getItemIdFromName(List.of(new Pair(lastBuild.boots(), null))).get(0));

        String json = String.format(
                "{\"title\":\"%s - %s - %s\",\"associatedMaps\":[11],\"associatedChampions\":[%d],\"blocks\":[{\"items\":[%s],\"type\":\"%s's Wasted Gaming Build\"},{\"items\":[{\"id\":\"2003\",\"count\":1}],\"type\":\"Consumables\"}]}",
                lastBuild.champion(), lastBuild.mastery(), lastBuild.skillToMax(),
                generator.getChampIdFromName(lastBuild.champion()),
                formatItems(itemIDs),
                lastBuild.champion()
        );

        String jsonResponse = String.format("{\"format\":\"league\",\"content\":%s}", json);
        sendJsonResponse(exchange, jsonResponse);
    }

    private void storeLastBuild(BuildResponse build) {
        lastBuild = build;
    }

    private void sendJsonResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String formatStringList(List<String> list) {
        return "[" + list.stream()
                .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }

    private String formatItems(List<Integer> itemIDs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < itemIDs.size(); i++) {
            sb.append(String.format("{\"id\":\"%d\",\"count\":1}", itemIDs.get(i)));
            if (i < itemIDs.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // This method is not used since we have specific handlers
    }
}
