package automation.web.visual;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Compare PNG images and generate Results file
 */

public class ImageComparator {

    public static void main(String[] args) throws IOException {
        compareImages(args[1], args[2], args[3]);
    }

    /**
     * Save image as file
     * @param image
     * @param fileName
     * @throws IOException possible exception
     */
    public static void savePngImage(BufferedImage image, String fileName) throws IOException {
        ImageIO.write(image, "png", new File(fileName));
    }

    /**
     * Compare Base image with Current image
     * @param baseImage
     * @param currentImage
     * @param resultOfComparison
     * @return
     * @throws IOException possible exception
     */
    public static boolean compareImages(String baseImage, String currentImage, String resultOfComparison)
            throws IOException {
        boolean result = true;
        BufferedImage bImage = ImageIO.read(new File(baseImage));
        BufferedImage cImage = ImageIO.read(new File(currentImage));

        int height = bImage.getHeight();
        int width = bImage.getWidth();
        BufferedImage rImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                try {
                    int pixelC = cImage.getRGB(x, y);
                    int pixelB = bImage.getRGB(x, y);
                    if (pixelB == pixelC ) {    // goood
                        rImage.setRGB(x, y,  bImage.getRGB(x, y));
                    } else {                    //bad
                        result = false;
                        int a= 0xff |  bImage.getRGB(x, y)>>24 ,
                                r= 0xff &  bImage.getRGB(x, y)>>16 ,
                                g= 0x00 &  bImage.getRGB(x, y)>>8,
                                b= 0x00 &  bImage.getRGB(x, y);

                        int modifiedRGB=a<<24|r<<16|g<<8|b;
                        rImage.setRGB(x,y,modifiedRGB);
                    }
                } catch (Exception e) {
                    result = false;
                    // handled height or width mismatch
                    rImage.setRGB(x, y, 0x80ff0000);
                }
            }
        }
        //save result file
        savePngImage(rImage, resultOfComparison);

        return result;
    }
}
