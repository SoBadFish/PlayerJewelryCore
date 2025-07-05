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


    /**
     * 将模型位置绑定到左手
     * */
    public void bindLeftHand(String modelName){
        bindParent(modelName,"leftArm");
    }

    /**
     * 将模型位置绑定到右手
     * */
    public void bindRightHand(String modelName){
        bindParent(modelName,"rightArm");
    }

    /**
     * 将模型位置绑定到头
     * */
    public void bindHead(String modelName){
        bindParent(modelName,"head");
    }

    /**
     * 将模型位置绑定到身体
     * */
    public void bindBody(String modelName){
        bindParent(modelName,"body");
    }


    public void bindParent(String modelName,String geometryName){
        for(GeometryJsonData.Geometry geometry:skinData.getMinecraftGeometry()){
            if(geometry.getBones() != null && !geometry.getBones().isEmpty()){
                for(GeometryJsonData.Geometry.Bone bone:geometry.getBones()){
                    if(bone.getName().equals(modelName)){
                        bone.setParent(geometryName);
                    }
                }
            }
        }
    }


}