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
     * 动态调整画布大小拼接图像
     *
     * @param images 图片数组(按面积从大到小排列)
     * @return 包含拼接图像和各图像左上角坐标的结果对象
     * @throws IllegalArgumentException 如果图片尺寸超过限制
     */
    public static MergeResult mergeImagesWithDynamicCanvas(BufferedImage... images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("图片数组不能为空");
        }

        // 验证单张图片尺寸不超过64x64
        for (BufferedImage img : images) {
            if (img.getWidth() > 64 || img.getHeight() > 64) {
                throw new IllegalArgumentException("单张图片尺寸不能超过64x64");
            }
        }

        // 固定画布大小为128x128并添加严格验证
        int canvasSize = 128;

        BufferedImage mergedImage = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mergedImage.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);

        List<Point> positions = new java.util.ArrayList<>();

        // 使用二维装箱算法优化空间利用率
        int x = 0;
        int y = 0;
        int currentRowHeight = 0;
        int maxY = 0;

        for (BufferedImage img : images) {
            // 如果当前行放不下，尝试换行
            if (x + img.getWidth() > canvasSize) {
                x = 0;
                y = maxY;
                currentRowHeight = 0;
            }

            // 如果换行后还是放不下，尝试在剩余空间寻找合适位置
            if (y + img.getHeight() > canvasSize) {
                boolean placed = false;
                // 尝试在已放置图片的上方寻找空间
                for (Point pos : positions) {
                    if (pos.x + img.getWidth() <= canvasSize &&
                            pos.y >= img.getHeight() &&
                            checkSpace(positions, pos.x, pos.y - img.getHeight(), img.getWidth(), img.getHeight())) {
                        x = pos.x;
                        y = pos.y - img.getHeight();
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    throw new IllegalArgumentException("图片总尺寸超过128x128画布限制");
                }
            }

            positions.add(new Point(x, y));
            g.drawImage(img, x, y, null);

            // 更新位置和最大高度
            x += img.getWidth();
            currentRowHeight = Math.max(currentRowHeight, img.getHeight());
            maxY = Math.max(maxY, y + img.getHeight());
        }

        g.dispose();
        return new MergeResult(mergedImage, positions);
    }

    /**
     * 检查指定区域是否有足够空间放置图片
     */

    private static boolean checkSpace(List<Point> positions, int x, int y, int width, int height) {
        // 检查画布边界
        if (x < 0 || y < 0 || x + width > 128 || y + height > 128) {
            return false;
        }

        // 检查与已放置图片的重叠
        for (Point pos : positions) {
            // 检查四个方向是否完全不重叠
            boolean notOverlap = x + width <= pos.x ||  // 当前图片在左侧
                               x >= pos.x + width ||    // 当前图片在右侧
                               y + height <= pos.y ||   // 当前图片在上方
                               y >= pos.y + height;     // 当前图片在下方

            if (!notOverlap) {
                return false;
            }
        }
        return true;
    }
}