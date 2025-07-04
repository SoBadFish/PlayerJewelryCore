package org.sobadfish.playerjewerlycore.core;

import cn.nukkit.utils.SerializedImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2025/2/14
 */
public class ImageUtils {


    /**
     * 将玩家皮肤转为 BuffedImage
     */
    public static BufferedImage serializedImageToBufferedImage(SerializedImage serializedImage) {
        int width = serializedImage.width;
        int height = serializedImage.height;
        byte[] data = serializedImage.data;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = data[index++] & 0xFF;
                int green = data[index++] & 0xFF;
                int blue = data[index++] & 0xFF;
                int alpha = data[index++] & 0xFF;
                int rgb = new Color(red, green, blue, alpha).getRGB();
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }


    /**
     * 合并图像并返回拼接位置信息
     */
    public static class MergeResult {
        public final BufferedImage mergedImage;
        public final List<Point> positions;

        public MergeResult(BufferedImage mergedImage, List<Point> positions) {
            this.mergedImage = mergedImage;
            this.positions = positions;
        }
    }

    public static class Point {
        public int x;
        public int y;


        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * 动态调整画布大小拼接图像(64→128→256...)
     * <p>
     * 动态调整画布大小拼接图像
     *
     * @param images 图片数组(第一个位置不变，其余按面积从大到小排列)
     * @return 包含拼接图像和各图像左上角坐标的结果对象(坐标顺序与输入顺序一致)
     */
    public static MergeResult mergeImagesWithDynamicCanvas(BufferedImage... images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("图片数组不能为空");
        }

        // 复制数组并排序(第一个保持原样，其余按面积从大到小)
        BufferedImage[] sortedImages = images.clone();
        if (sortedImages.length > 1) {
            java.util.Arrays.sort(sortedImages, 1, sortedImages.length,
                    (img1, img2) -> {
                        int area1 = img1.getWidth() * img1.getHeight();
                        int area2 = img2.getWidth() * img2.getHeight();
                        return Integer.compare(area2, area1); // 降序排序
                    });
        }

        List<Point> positions = new java.util.ArrayList<>();

        // 计算最小图片尺寸作为网格单元基准
        int minWidth = Integer.MAX_VALUE;
        int minHeight = Integer.MAX_VALUE;
        for (BufferedImage img : sortedImages) {
            minWidth = Math.min(minWidth, img.getWidth());
            minHeight = Math.min(minHeight, img.getHeight());
        }
        int cellSize = Math.max(minWidth, minHeight);

        // 计算初始网格(128x128)
        int gridSize = 128 / cellSize;
        int currentSize = 128;

        // 检查是否需要更大的画布(256x256)
        int requiredCells = (int) Math.ceil(Math.sqrt(sortedImages.length));
        if (requiredCells > gridSize || cellSize * gridSize > 128) {
            currentSize = 256;
            gridSize = 256 / cellSize;
        }

        // 验证画布是否足够大
        if (cellSize * gridSize > currentSize) {
            throw new IllegalArgumentException("无法在"+currentSize+"x"+currentSize+
                "画布上放置所有图片(需要至少"+cellSize*gridSize+"x"+cellSize*gridSize+")");
        }

        BufferedImage mergedImage = new BufferedImage(currentSize, currentSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mergedImage.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);

        // 计算每个图像的位置并绘制(保持原始尺寸)
        for (int i = 0; i < sortedImages.length; i++) {
            int row = i / gridSize;
            int col = i % gridSize;
            int x = col * cellSize;
            int y = row * cellSize;

            // 保持原始顺序的坐标映射
            int originalIndex = i == 0 ? 0 :
                    java.util.Arrays.asList(images).indexOf(sortedImages[i]);
            while (positions.size() <= originalIndex) {
                positions.add(null);
            }
            positions.set(originalIndex, new Point(x, y));

            // 直接绘制原图，不缩放
            g.drawImage(sortedImages[i], x, y, null);
        }

        g.dispose();
        return new MergeResult(mergedImage, positions);
    }
}
