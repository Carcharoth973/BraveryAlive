package com.braveryalive.model;

import processing.core.PImage;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record Pair(String name, @JsonIgnore PImage image) {
}
