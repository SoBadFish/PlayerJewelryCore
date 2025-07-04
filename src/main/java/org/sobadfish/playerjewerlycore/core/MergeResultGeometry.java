package org.sobadfish.playerjewerlycore.core;

import java.awt.image.BufferedImage;

public class MergeResultGeometry {

    public BufferedImage skinPng;

    public GeometryJsonData geometryJsonData;

    public MergeResultGeometry(BufferedImage skinPng, GeometryJsonData geometryJsonData) {
        this.skinPng = skinPng;
        this.geometryJsonData = geometryJsonData;
    }
}
