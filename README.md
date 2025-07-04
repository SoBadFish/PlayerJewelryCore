# PlayerJewelryCore - 玩家饰品核心插件

## 功能概述
PlayerJewelryCore是一款基于 Nukkit MOT 核心插件，允许服务器为玩家添加3D饰品模型。主要功能包括：
- 动态合并多个3D模型到玩家皮肤上
- 支持自定义饰品位置和UV映射
- 异步处理模型合并，避免阻塞主线程
- 自动适配不同尺寸的皮肤和模型

## 安装方法
1. 将插件jar文件放入Nukkit服务器的`plugins`目录
2. 重启服务器
3. 插件会自动生成默认配置

## API调用方式

### 添加饰品模型
```java
// 获取插件实例
PlayerJewelryCore core = PlayerJewelryCore.getInstance();

// 方式1：直接添加GeometryData对象
File skinImage = new File("贴图.png");
File skinModel = new File("饰品模型.json");
GeometryData data = core.loadFileToGeometryData(skinImage, skinModel);
core.addPlayerGeometryData("自定义名称", data);

// 方式2：通过图片和模型文件添加
File skinImage = new File("贴图.png");
File skinModel = new File("饰品模型.json"); 
core.addPlayerGeometryData("骨骼模型", skinImage, skinModel);
```

### 获取饰品模型
```java
GeometryData data = core.getPlayerGeometryData("自定义名称");
```

### 更新玩家皮肤
```java
core.updatePlayerSkinModel(player);
```


## 使用示例

### 添加一个饰品
```java
PlayerJewelryCore core = PlayerJewelryCore.getInstance();

// 加载饰品资源
File wingImage = new File(core.getDataFolder(), "贴图.png");
File wingModel = new File(core.getDataFolder(), "骨骼模型.json");

// 添加到玩家
core.addPlayerGeometryData("自定义名称", wingImage, wingModel);

// 更新玩家皮肤
core.updatePlayerSkinModel(player);
```

## 模型文件规范
饰品模型需要符合以下规范：
1. 图片文件必须是PNG格式
2. JSON模型文件需符合Minecraft基岩版实体模型格式
3. 建议模型尺寸为32x32或64x64像素

## 开发者提示
- 使用`ImageUtils.mergeImagesWithDynamicCanvas()`方法可以合并多个饰品图片
- `GeometryJsonData`类提供了完整的模型数据结构
- 建议在异步线程中处理模型合并操作

## 兼容性
- 支持Nukkit API 1.0.11+
- 支持Minecraft基岩版1.16.0+的皮肤格式