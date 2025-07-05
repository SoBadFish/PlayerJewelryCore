package org.sobadfish.playerjewerlycore.core;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeometryJsonData implements Cloneable {

    /**
     * 自定义Cube解析适配器
     */
    public static class CubeTypeAdapter extends TypeAdapter<GeometryJsonData.Geometry.Bone.Cube> {
        private final Gson gson = new Gson();

        @Override
        public void write(JsonWriter out, GeometryJsonData.Geometry.Bone.Cube cube) throws IOException {
            out.beginObject();
            out.name("origin").jsonValue(gson.toJson(cube.getOrigin()));
            out.name("size").jsonValue(gson.toJson(cube.getSize()));

            // 处理UV字段
            Object uv = cube.getUv();
            if (uv != null) {
                out.name("uv");
                if (uv instanceof int[]) {
                    out.jsonValue(gson.toJson(uv));
                } else if (uv instanceof Map) {
                    out.beginObject();
                    for (Map.Entry<String, ?> entry : ((Map<String, ?>) uv).entrySet()) {
                        out.name(entry.getKey()).jsonValue(gson.toJson(entry.getValue()));
                    }
                    out.endObject();
                }
            }

            if (cube.getInflate() != null) {
                out.name("inflate").value(cube.getInflate());
            }

            out.endObject();
        }

        @Override
        public GeometryJsonData.Geometry.Bone.Cube read(JsonReader in) throws IOException {
            GeometryJsonData.Geometry.Bone.Cube cube = new GeometryJsonData.Geometry.Bone.Cube();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "origin":
                        in.beginArray();
                        float[] origin = new float[3];
                        for (int i = 0; i < 3; i++) {
                            origin[i] = (float) in.nextDouble();
                        }
                        cube.setOrigin(origin);
                        in.endArray();
                        break;
                    case "size":
                        in.beginArray();
                        float[] size = new float[3];
                        for (int i = 0; i < 3; i++) {
                            size[i] = (float) in.nextDouble();
                        }
                        cube.setSize(size);
                        in.endArray();
                        break;
                    case "uv":
                        if (in.peek() == JsonToken.BEGIN_ARRAY) {
                            in.beginArray();
                            int[] uv = new int[2];
                            uv[0] = in.nextInt();
                            uv[1] = in.nextInt();
                            cube.setUv(uv);
                            in.endArray();
                        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
                            Map<String, Geometry.Bone.Face> uvMap = new HashMap<>();
                            in.beginObject();
                            while (in.hasNext()) {
                                String faceName = in.nextName();
                                in.beginObject();
                                Geometry.Bone.Face face = new Geometry.Bone.Face();
                                while (in.hasNext()) {
                                    String prop = in.nextName();
                                    if ("uv".equals(prop)) {
                                        in.beginArray();
                                        face.setUv(new int[]{in.nextInt(), in.nextInt()});
                                        in.endArray();
                                    } else if ("uv_size".equals(prop)) {
                                        in.beginArray();
                                        face.setUvSize(new int[]{in.nextInt(), in.nextInt()});
                                        in.endArray();
                                    } else {
                                        in.skipValue();
                                    }
                                }
                                uvMap.put(faceName, face);
                                in.endObject();
                            }
                            cube.setUv(uvMap);
                            in.endObject();
                        }
                        break;
                    case "inflate":
                        cube.setInflate((float) in.nextDouble());
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            return cube;
        }
    }

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

    @Override
    public GeometryJsonData clone() {
        try {
            GeometryJsonData data = (GeometryJsonData) super.clone();
            if (minecraftGeometry != null) {
                data.minecraftGeometry = new ArrayList<>();
                for (Geometry geometry : minecraftGeometry) {
                    data.minecraftGeometry.add(geometry.clone());
                }
            }
            return data;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class Geometry implements Cloneable{
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

        @Override
        public Geometry clone() {
            try {
                Geometry clone = (Geometry) super.clone();
                if (description != null) {
                    clone.description = new Description();
                    clone.description.identifier = description.identifier;
                    clone.description.texture_width = description.texture_width;
                    clone.description.texture_height = description.texture_height;
                    clone.description.visible_bounds_width = description.visible_bounds_width;
                    clone.description.visible_bounds_height = description.visible_bounds_height;
                    if (description.visible_bounds_offset != null) {
                        clone.description.visible_bounds_offset = description.visible_bounds_offset.clone();
                    }
                }
                if (bones != null) {
                    clone.bones = new ArrayList<>();
                    for (Bone bone : bones) {
                        clone.bones.add(bone.clone());
                    }
                }
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
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

        public static class Bone implements Cloneable{
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

            @Override
            public Bone clone() {
                try {
                    Bone clone = (Bone) super.clone();
                    if (pivot != null) {
                        clone.pivot = pivot.clone();
                    }
                    if (rotation != null) {
                        clone.rotation = rotation.clone();
                    }
                    if (cubes != null) {
                        clone.cubes = new ArrayList<>();
                        for (Cube cube : cubes) {
                            clone.cubes.add(cube.clone());
                        }
                    }
                    return clone;
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError();
                }
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

            public static class Cube implements Cloneable{

                private float[] origin;
                private float[] size;

                @SerializedName("uv")
                private Object uv; // 可以是int[]或Map<String, Face>

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

                public Object getUv() {
                    return uv;
                }

                public void setUv(Object uv) {
                    this.uv = uv;
                }

                public Float getInflate() {
                    return inflate;
                }

                public void setInflate(Float inflate) {
                    this.inflate = inflate;
                }

                /**
                 * 获取UV作为数组
                 */
                public int[] getUvAsArray() {
                    if (uv instanceof List) {
                        List<?> list = (List<?>) uv;
                        return list.stream()
                                .mapToInt(o -> ((Number) o).intValue())
                                .toArray();
                    } else if (uv instanceof int[]) {
                        return (int[]) uv;
                    }
                    return null;
                }

                /**
                 * 获取UV作为面映射
                 */
                public Map<String, Face> getUvAsMap() {
                    if (uv instanceof Map) {
                        return (Map<String, Face>) uv;
                    }
                    return null;
                }

                /**
                 * 统一设置UV值
                 */
                public void setUvValue(int[] uvArray) {
                    this.uv = uvArray;
                }

                @Override
                public Cube clone() {
                    try {
                        Cube clone = (Cube) super.clone();
                        if (origin != null) clone.origin = origin.clone();
                        if (size != null) clone.size = size.clone();
                        if (uv instanceof int[]) {
                            clone.uv = ((int[]) uv).clone();
                        } else if (uv instanceof Map) {
                            Map<String, Face> uvAsMap = getUvAsMap();
                            uvAsMap.replaceAll((k, v) -> v.clone());
                            clone.uv = new HashMap<>((Map<?, ?>) uvAsMap);
                        }
                        return clone;
                    } catch (CloneNotSupportedException e) {
                        throw new AssertionError();
                    }
                }



            }
            public static class Face implements Cloneable{
                private int[] uv;
                @SerializedName("uv_size")
                private int[] uvSize;

                public int[] getUv() {
                    return uv;
                }

                public void setUv(int[] uv) {
                    this.uv = uv;
                }

                public int[] getUvSize() {
                    return uvSize;
                }

                public void setUvSize(int[] uvSize) {
                    this.uvSize = uvSize;
                }

                @Override
                public Face clone() {
                    try {
                        Face clone = (Face) super.clone();

                        if (uvSize != null) clone.uvSize = uvSize.clone();

                        clone.uv = uv.clone();

                        return clone;
                    } catch (CloneNotSupportedException e) {
                        throw new AssertionError();
                    }
                }
            }
        }
    }
}
