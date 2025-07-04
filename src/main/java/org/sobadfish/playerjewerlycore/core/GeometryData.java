package org.sobadfish.playerjewerlycore.core;

import java.awt.image.BufferedImage;

public class GeometryData {

    public BufferedImage image;

    public GeometryJsonData skinData;

    public boolean enable = true;

    public GeometryData(BufferedImage image, GeometryJsonData geometryJsonData) {
        this.image = image;
        this.skinData = geometryJsonData;
    }


    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }
}