package com.braveryalive.service;

import com.braveryalive.model.Build;
import java.util.List;

/**
 * Interface for exporting builds to different formats.
 */
public interface BuildExporter {
    void export(Build build);
}
