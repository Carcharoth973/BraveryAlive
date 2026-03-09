package com.braveryalive.model;

import java.util.List;

/**
 * DTO returned by the /api/roll endpoint, including image URLs.
 */
public record BuildResponse(
        String champion,
        String championImageUrl,
        String mastery,
        String summonerSpell1,
        String summonerSpell2,
        String skillToMax,
        String boots,
        String bootsImageUrl,
        List<String> items,
        List<String> itemImageUrls
) {
}
