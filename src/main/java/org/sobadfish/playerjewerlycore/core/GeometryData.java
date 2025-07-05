package org.sobadfish.playerjewerlycore.core;

import java.awt.image.BufferedImage;

public class GeometryData implements Cloneable{

    public BufferedImage image;

    public GeometryJsonData skinData;

    public boolean enable = true;

    public GeometryData(BufferedImage image, GeometryJsonData geometryJsonData) {
        this.image = image;
        this.skinData = geometryJsonData;
    }

    public GeometryData(BufferedImage image, GeometryJsonData geometryJsonData,boolean enable) {
        this.image = image;
        this.skinData = geometryJsonData;
        this.enable = enable;
    }


    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public GeometryData clone() {
        try {
            GeometryData geometryData = (GeometryData) super.clone();
            geometryData.skinData = this.skinData.clone();
            return geometryData;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}