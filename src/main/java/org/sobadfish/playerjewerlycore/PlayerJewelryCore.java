package org.sobadfish.playerjewerlycore;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.SerializedImage;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    public Skin defaultSkin;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static final String PLUGIN_NAME = "&f[&b饰品&f]";

    public static PlayerJewelryCore INSTANCE;

    public LinkedHashMap<String, GeometryData> PLAYERS = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.getServer().getPluginManager().registerEvents(this,this);

        saveDefaultConfig();
        this.getLogger().info("玩家饰品核心 已加载");
    }


    public static PlayerJewelryCore getInstance() {
        return INSTANCE;
    }



    public void addPlayerGeometryData(String skinName, GeometryData data) {
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
            Gson gson = new Gson();
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
        defaultSkin = player.getSkin();
        sendMessageToConsole("&e正在进行饰品渲染");
        updatePlayerSkinModel(player);
    }


    public void updatePlayerSkinModel(Player player){
        // 替换皮肤
        if (defaultSkin.getSkinData().width >= 128) {
            return;
        }
        //异步处理
        Skin modelBone = player.getSkin();
        executorService.execute(() -> {
            Gson gson = new GsonBuilder().create();
            List<GeometryData> geometryData = new ArrayList<>(PLAYERS.values());
            MergeResultGeometry pd = loadGeometry(player.getSkin(),geometryData);
            //生成模型和图片用作测试
//            File last = new File(getDataFolder() + "/merged_" + player.getName() + ".last.png");
//            try {
//                ImageIO.write(pd.skinPng, "PNG", last);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            File outNet = new File(getDataFolder() + "/skin_" + player.getName() + ".new.json");
//            try {
//                Utils.writeFile(outNet, gson.toJson(pd.geometryJsonData));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Skin skin1 = new Skin();
            skin1.setCapeData(modelBone.getCapeData());
            skin1.setSkinData(pd.skinPng);
            skin1.setGeometryData(gson.toJson(pd.geometryJsonData));
            skin1.setGeometryDataEngineVersion(modelBone.getGeometryDataEngineVersion()); //设置皮肤版本，主流格式有1.16.0,1.12.0(Blockbench新模型),1.10.0(Blockbench Legacy模型),1.8.0
            skin1.setSkinId(modelBone.getSkinId());
            skin1.setSkinResourcePatch(modelBone.getSkinResourcePatch());
            skin1.isLegacySlim = modelBone.isLegacySlim;
            //2秒后实现
            Server.getInstance().getScheduler().scheduleDelayedTask(new PluginTask<>(getInstance()) {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        player.setSkin(skin1);
                    }
                }
            },20);
        });
    }

    /**
     * 玩家皮肤 加载 部分模型
     * */
    private MergeResultGeometry loadGeometry(Skin defSkin, List<GeometryData> skinData){
        Gson gson = new Gson();
        SerializedImage image = defSkin.getSkinData();
        BufferedImage defaultSkinPng = ImageUtils.serializedImageToBufferedImage(image);
        List<BufferedImage> bufferedImages = new ArrayList<>();
        bufferedImages.add(defaultSkinPng);
        for(GeometryData geometryData : skinData){
            if(!geometryData.enable){
                continue;
            }
            bufferedImages.add(geometryData.image);
        }

        ImageUtils.MergeResult result = ImageUtils.mergeImagesWithDynamicCanvas(bufferedImages.toArray(new BufferedImage[0]));
        GeometryJsonData pd = gson.fromJson(defSkin.getGeometryData(), GeometryJsonData.class);

        //更新模型的UV
        int uvIndex = 1;
        for(GeometryData data: skinData){
            if(!data.enable){
                continue;
            }
            ImageUtils.Point uvPoint = result.positions.get(uvIndex);
            for(GeometryJsonData.Geometry geo:data.skinData.getMinecraftGeometry()){
                if(!geo.getBones().isEmpty()){
                    List<GeometryJsonData.Geometry.Bone> defbone = geo.getBones();
                    for(GeometryJsonData.Geometry.Bone bone: defbone){
                        //修改坐标
                        for(GeometryJsonData.Geometry.Bone.Cube cube: bone.getCubes()){
                            int[] uv = new int[]{cube.getUv()[0] + uvPoint.x,cube.getUv()[1] + uvPoint.y};
                            cube.setUv(uv);
                        }
                    }
                }
            }
            uvIndex++;
        }
        //更改骨骼
        for (GeometryJsonData.Geometry geometry : pd.getMinecraftGeometry()) {
            GeometryJsonData.Geometry.Description desc = geometry.getDescription();
            if (desc != null) {
                if(desc.getTexture_width() >= 64 && desc.getTexture_height() >= 64) {
                    desc.setTexture_width(result.mergedImage.getWidth());
                    desc.setTexture_height(result.mergedImage.getHeight());
                    desc.setVisible_bounds_width(0);
                    desc.setVisible_bounds_height(0);
                }else{
                    continue;
                }
            }else{
                continue;
            }
            // 添加骨骼
            List<GeometryJsonData.Geometry.Bone> bones = geometry.getBones();
            if(bones != null){
                for(GeometryData data: skinData){
                    if (!data.skinData.getMinecraftGeometry().isEmpty()) {
                        //需要添加的骨骼模型
                        for(GeometryJsonData.Geometry geo:data.skinData.getMinecraftGeometry()){
                            if(!geo.getBones().isEmpty()){
                                bones.addAll( geo.getBones());
                            }
                        }
                    }
                    uvIndex++;
                }
            }
            geometry.setBones(bones);
        }
        return new MergeResultGeometry(result.mergedImage,pd);
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