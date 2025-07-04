package org.sobadfish.playerjewerlycore.core;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * 玩家JSON模型数据实体类
 * */
public class GeometryJsonData {
    private String format_version;

    @SerializedName("minecraft:geometry")
    private List<Geometry> minecraftGeometry;

    // Getters and Setters
    public String getFormat_version() {
        return format_version;
    }

    public void setFormat_version(String format_version) {
        this.format_version = format_version;
    }

    public List<Geometry> getMinecraftGeometry() {
        return minecraftGeometry;
    }

    public void setMinecraftGeometry(List<Geometry> minecraftGeometry) {
        this.minecraftGeometry = minecraftGeometry;
    }

    public static class Geometry {
        private Description description;
        private List<Bone> bones;

        // Getters and Setters
        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public List<Bone> getBones() {
            return bones;
        }

        public void setBones(List<Bone> bones) {
            this.bones = bones;
        }

        public static class Description {
            private String identifier;
            private int texture_width;
            private int texture_height;
            private float visible_bounds_width;
            private float visible_bounds_height;
            private float[] visible_bounds_offset;

            // Getters and Setters
            public String getIdentifier() {
                return identifier;
            }

            public void setIdentifier(String identifier) {
                this.identifier = identifier;
            }

            public int getTexture_width() {
                return texture_width;
            }

            public void setTexture_width(int texture_width) {
                this.texture_width = texture_width;
            }

            public int getTexture_height() {
                return texture_height;
            }

            public void setTexture_height(int texture_height) {
                this.texture_height = texture_height;
            }

            public float getVisible_bounds_width() {
                return visible_bounds_width;
            }

            public void setVisible_bounds_width(float visible_bounds_width) {
                this.visible_bounds_width = visible_bounds_width;
            }

            public float getVisible_bounds_height() {
                return visible_bounds_height;
            }

            public void setVisible_bounds_height(float visible_bounds_height) {
                this.visible_bounds_height = visible_bounds_height;
            }

            public float[] getVisible_bounds_offset() {
                return visible_bounds_offset;
            }

            public void setVisible_bounds_offset(float[] visible_bounds_offset) {
                this.visible_bounds_offset = visible_bounds_offset;
            }
        }

        public static class Bone {
            private String name;
            private String parent;
            private double[] pivot;
            private double[] rotation;
            private List<Cube> cubes;
            private Map<String, Object> locators;
            private Float inflate;

            //1.14 兼容
            @SerializedName("poly_mesh")
            private PolyMesh polyMesh;


            // Getters and Setters
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getParent() {
                return parent;
            }

            public PolyMesh getPolyMesh() {
                return polyMesh;
            }

            public void setPolyMesh(PolyMesh polyMesh) {
                this.polyMesh = polyMesh;
            }

            public void setParent(String parent) {
                this.parent = parent;
            }

            public double[] getPivot() {
                return pivot;
            }

            public void setPivot(double[] pivot) {
                this.pivot = pivot;
            }

            public double[] getRotation() {
                return rotation;
            }

            public void setRotation(double[] rotation) {
                this.rotation = rotation;
            }

            public List<Cube> getCubes() {
                return cubes;
            }

            public void setCubes(List<Cube> cubes) {
                this.cubes = cubes;
            }

            public Map<String, Object> getLocators() {
                return locators;
            }

            public void setLocators(Map<String, Object> locators) {
                this.locators = locators;
            }

            public Float getInflate() {
                return inflate;
            }

            public void setInflate(Float inflate) {
                this.inflate = inflate;
            }

            public static class PolyMesh{

                @SerializedName("normalized_uvs")
                private boolean normalizedUvs;

                private float[][] normals;

                private int[][][] polys;

                private float[][] positions;

                private float[][] uvs;


                public void setNormalizedUvs(boolean normalized_uvs) {
                    this.normalizedUvs = normalized_uvs;
                }

                public void setPositions(float[][] positions) {
                    this.positions = positions;
                }


                public void setUvs(float[][] uvs) {
                    this.uvs = uvs;
                }

                public void setPolys(int[][][] polys) {
                    this.polys = polys;
                }

                public void setNormals(float[][] normals) {
                    this.normals = normals;
                }

                public float[][] getNormals() {
                    return normals;
                }

                public float[][] getPositions() {
                    return positions;
                }

                public float[][] getUvs() {
                    return uvs;
                }

                public int[][][] getPolys() {
                    return polys;
                }

            }

            public static class Cube {
                private float[] origin;
                private float[] size;
                private int[] uv;
                private Float inflate;

                // Getters and Setters
                public float[] getOrigin() {
                    return origin;
                }

                public void setOrigin(float[] origin) {
                    this.origin = origin;
                }

                public float[] getSize() {
                    return size;
                }

                public void setSize(float[] size) {
                    this.size = size;
                }

                public int[] getUv() {
                    return uv;
                }

                public void setUv(int[] uv) {
                    this.uv = uv;
                }

                public Float getInflate() {
                    return inflate;
                }

                public void setInflate(Float inflate) {
                    this.inflate = inflate;
                }
            }
        }
    }
}
