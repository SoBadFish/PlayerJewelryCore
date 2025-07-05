package org.sobadfish.playerjewerlycore;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.sobadfish.playerjewerlycore.core.GeometryData;
import org.sobadfish.playerjewerlycore.core.GeometryJsonData;
import org.sobadfish.playerjewerlycore.core.ImageUtils;
import org.sobadfish.playerjewerlycore.core.MergeResultGeometry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 玩家饰品核心
 * 可以 给玩家身上添加模型饰品
 *
 * */
public class PlayerJewelryCore extends PluginBase implements Listener {

    /**
     * 玩家初始模型
     * */
    public LinkedHashMap<String, Skin> PLAYER_DEFAULT_SKIN = new LinkedHashMap<>();

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static final String PLUGIN_NAME = "&f[&b饰品&f]";

    public static PlayerJewelryCore INSTANCE;

    public LinkedHashMap<String, GeometryData> PLAYERS = new LinkedHashMap<>();


    @Override
    public void onEnable() {
        INSTANCE = this;
        this.getServer().getPluginManager().registerEvents(this,this);
        saveDefaultConfig();
        reloadConfig();
        this.getLogger().info("玩家饰品核心 已加载");
    }


    public static PlayerJewelryCore getInstance() {
        return INSTANCE;
    }



    public void addPlayerGeometryData(String skinName, GeometryData data) {
        if(data.image.getWidth() > 64 || data.image.getHeight() > 64) {
            return;
        }
        this.PLAYERS.put(skinName, data);
    }

    public void addPlayerGeometryData(String skinName, File skinImage,File skinModel) {
       GeometryData geometryData = loadFileToGeometryData(skinImage, skinModel);
       if(geometryData != null) {
           addPlayerGeometryData(skinName,geometryData);
       }


    }

    public GeometryData getPlayerGeometryData(String skinName) {
        return this.PLAYERS.get(skinName);
    }

    /**
     * 读取皮肤与模型文件
     * 加载为数据文件
     * */
    public GeometryData loadFileToGeometryData(File skinImage,File skinModel) {
        try {
            BufferedImage modelImage = ImageIO.read(skinImage);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(GeometryJsonData.Geometry.Bone.Cube.class, new GeometryJsonData.CubeTypeAdapter())
                    .create();
            String jsonContent = Utils.readFile(skinModel);
            GeometryJsonData modelJson = gson.fromJson(jsonContent, GeometryJsonData.class);
            return new GeometryData(modelImage,modelJson);
        } catch (IOException e) {
            e.printStackTrace();
            this.getLogger().error("模型文件读取失败");
            return null;
        }

    }




    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // 玩家加入事件的处理逻辑
        Player player = event.getPlayer();
        PLAYER_DEFAULT_SKIN.put(player.getName(), player.getSkin());
        if(getConfig().getBoolean("enable-join-update",true)) {
            sendMessageToConsole("&e正在进行饰品渲染");
            updatePlayerSkinModel(player);
        }
    }


    public void updatePlayerSkinModel(Player player){
        // 替换皮肤
        if(!PLAYER_DEFAULT_SKIN.containsKey(player.getName())){
            return;
        }
        if (PLAYER_DEFAULT_SKIN.get(player.getName()).getSkinData().width >= 128) {
            return;
        }
        //异步处理
        Skin modelBone = PLAYER_DEFAULT_SKIN.get(player.getName());
        executorService.execute(() -> {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(GeometryJsonData.Geometry.Bone.Cube.class, new GeometryJsonData.CubeTypeAdapter())
                    .create();
            // 创建PLAYERS数据的深拷贝
            List<GeometryData> geometryDataCopy = new ArrayList<>(PLAYERS.size());
            for(GeometryData data : PLAYERS.values()) {
                GeometryData copy = new GeometryData(
                   data.image,
                   data.skinData.clone()
                );
                copy.enable = data.enable;
                geometryDataCopy.add(copy);
            }
            MergeResultGeometry pd = loadGeometry(PLAYER_DEFAULT_SKIN.get(player.getName()),geometryDataCopy);
            if(getConfig().getBoolean("generate-merge-model",true)) {
                try {
                    ImageIO.write(pd.skinPng, "png", new File(getDataFolder()+"/"+player.getName()+".png"));
                    Utils.writeFile(getDataFolder()+"/"+player.getName()+".json",gson.toJson(pd.geometryJsonData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Skin skin1 = new Skin();
            skin1.setCapeData(modelBone.getCapeData());
            skin1.setSkinData(pd.skinPng);
            skin1.setGeometryData(gson.toJson(pd.geometryJsonData));
            skin1.setGeometryDataEngineVersion(modelBone.getGeometryDataEngineVersion()); //设置皮肤版本，主流格式有1.16.0,1.12.0(Blockbench新模型),1.10.0(Blockbench Legacy模型),1.8.0
            skin1.setSkinId(modelBone.getSkinId());
            skin1.setSkinResourcePatch(modelBone.getSkinResourcePatch());
            skin1.isLegacySlim = modelBone.isLegacySlim;
            PlayerSkinPacket pk = new PlayerSkinPacket();
            pk.skin = skin1;
            pk.uuid = player.getUniqueId();
            pk.newSkinName = "";
            pk.oldSkinName = "";
            Server.getInstance().getOnlinePlayers().values().forEach(p->p.dataPacket(pk));

        });
    }
    /**
     * 玩家皮肤加载模型
     * */
    private MergeResultGeometry loadGeometry(Skin defSkin, List<GeometryData> skinData) {
        // 注册自定义序列化/反序列化器
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GeometryJsonData.Geometry.Bone.Cube.class, new GeometryJsonData.CubeTypeAdapter())
                .create();

        GeometryJsonData pd = gson.fromJson(defSkin.getGeometryData(), GeometryJsonData.class);

        int enabledCount = (int) skinData.stream().filter(d -> d.enable).count();
        List<BufferedImage> bufferedImages = new ArrayList<>(enabledCount + 1);
        bufferedImages.add(ImageUtils.serializedImageToBufferedImage(defSkin.getSkinData()));

        skinData.parallelStream()
                .filter(d -> d.enable)
                .forEach(d -> bufferedImages.add(d.image));

        ImageUtils.MergeResult result = ImageUtils.mergeImagesWithDynamicCanvas(bufferedImages.toArray(new BufferedImage[0]));

        List<ImageUtils.Point> uvPoints = new ArrayList<>(enabledCount);
        for (int i = 1; i <= enabledCount; i++) {
            uvPoints.add(result.positions.get(i));
        }

        skinData.parallelStream()
                .filter(d -> d.enable)
                .forEach(data -> {
                    ImageUtils.Point uvPoint = uvPoints.remove(0);

                    data.skinData.getMinecraftGeometry().parallelStream()
                            .filter(geo -> geo.getBones() != null && !geo.getBones().isEmpty())
                            .forEach(geo -> geo.getBones().parallelStream()
                                    .filter(bone -> bone.getCubes() != null && !bone.getCubes().isEmpty())
                                    .forEach(bone -> {
                                        List<GeometryJsonData.Geometry.Bone.Cube> newCubes = new ArrayList<>();
                                        for (GeometryJsonData.Geometry.Bone.Cube cube : bone.getCubes()) {
                                            GeometryJsonData.Geometry.Bone.Cube newCube = cube.clone();

                                            int[] uv = newCube.getUvAsArray();
                                            Map<String, GeometryJsonData.Geometry.Bone.Face> uvFace = newCube.getUvAsMap();

                                            if (uv != null) {
                                                newCube.setUv(new int[]{
                                                        uv[0] + uvPoint.x,
                                                        uv[1] + uvPoint.y
                                                });
                                            } else if (uvFace != null) {
                                                Map<String, GeometryJsonData.Geometry.Bone.Face> newFace = new HashMap<>();
                                                for (String uvFaceName : uvFace.keySet()) {
                                                    GeometryJsonData.Geometry.Bone.Face face = uvFace.get(uvFaceName).clone();
                                                    int[] faceUv = face.clone().getUv();
                                                    if (faceUv != null) {
                                                        GeometryJsonData.Geometry.Bone.Face newFace1 = face.clone();
                                                        newFace1.setUv(new int[]{
                                                                faceUv[0] + uvPoint.x,
                                                                faceUv[1] + uvPoint.y
                                                        });
                                                        newFace.put(uvFaceName, newFace1);
                                                    }
                                                }
                                                newCube.setUv(newFace);
                                            }

                                            newCubes.add(newCube);
                                        }
                                        bone.setCubes(newCubes);
                                    }));
                });

        pd.getMinecraftGeometry().parallelStream()
                .forEach(geometry -> {
                    GeometryJsonData.Geometry.Description desc = geometry.getDescription();
                    if (desc != null && desc.getTexture_width() >= 64 && desc.getTexture_height() >= 64) {
                        desc.setTexture_width(result.mergedImage.getWidth());
                        desc.setTexture_height(result.mergedImage.getHeight());
                        //128*128的坑
                        if(result.mergedImage.getWidth() == 256){
                            desc.setVisible_bounds_width(2);
                            desc.setVisible_bounds_height(3);
                            desc.setVisible_bounds_offset(new float[]{0,1.5f,0});

                        }else{
                            desc.setVisible_bounds_width(0);
                            desc.setVisible_bounds_height(0);
                        }

                        List<GeometryJsonData.Geometry.Bone> bones = geometry.getBones();
                        if (bones != null) {
                            skinData.stream()
                                    .filter(d -> !d.skinData.getMinecraftGeometry().isEmpty())
                                    .forEach(data -> data.skinData.getMinecraftGeometry().stream()
                                            .filter(geo -> geo.getBones() != null && !geo.getBones().isEmpty())
                                            .forEach(geo -> bones.addAll(geo.getBones())));
                        }
                    }
                });

        pd.getMinecraftGeometry().forEach(geometry -> {
            List<GeometryJsonData.Geometry.Bone> bones = geometry.getBones();
            if (bones != null && !bones.isEmpty()) {
                Map<String, GeometryJsonData.Geometry.Bone> uniqueBones = new LinkedHashMap<>();
                for (int i = bones.size() - 1; i >= 0; i--) {
                    GeometryJsonData.Geometry.Bone bone = bones.get(i);
                    uniqueBones.putIfAbsent(bone.getName(), bone);
                }
                geometry.setBones(new ArrayList<>(uniqueBones.values()));
            }
        });

        return new MergeResultGeometry(result.mergedImage, pd);
    }




    public static void sendMessageToObject(String msg, Object o){
        String message = TextFormat.colorize('&',PLUGIN_NAME+" &r"+msg);
        if(o != null){
            if(o instanceof Player){
                if(((Player) o).isOnline()) {
                    ((Player) o).sendMessage(message);
                    return;
                }
            }
            if(o instanceof EntityHuman){
                message = ((EntityHuman) o).getName()+"->"+message;
            }
        }
        INSTANCE.getLogger().info(message);

    }

    public static void sendMessageToConsole(String msg){
        sendMessageToObject(msg,null);
    }



}