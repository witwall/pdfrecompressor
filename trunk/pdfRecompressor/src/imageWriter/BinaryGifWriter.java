package imageWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 * Saves images consisting of two colors (0 for black, 1 for white) represented
 * as an arrays of bits packed into bytes to GIF files.
 * @author Martin Jirman <xjirman1@fi.muni.cz>
 */
public class BinaryGifWriter {

    /**
     * Saves given bit array into two-color GIF images. Bits set to one
     * represent white pixels and bits set to zero represent white pixels.
     * @param bits Pixels to save (each byte of array contains 8 bits - either
     *             image pixels or some kind of padding). Array starts with top
     *             left pixel and goes right in upper line.
     * @param sizeX Width of the image.
     * @param sizeY Height of the image.
     * @param eachLinePadded True if each horizontal line of pixels
     *                       (represented by bits) is padded to byte boundary.
     * @param lsbFirst True if least significant bit of each byte represents
     *                 first pixel of each pixel octet.
     * @param output The stream to write the image into.
     *               This method DOES NOT close this stream.
     * @throws IOException if any IO error related to given output stream occurs
     */
    public static void saveGif(byte[] bits, int sizeX, int sizeY,
            boolean eachLinePadded, boolean lsbFirst, OutputStream output)
            throws IOException {
        // validate arguments
        if(bits == null) {
            throw new NullPointerException("bits");
        }
        if(sizeX <= 0) {
            throw new IllegalArgumentException("Argument sizeX must be " +
                    "positive (was " + sizeX + ")");
        }
        if(sizeY <= 0) {
            throw new IllegalArgumentException("Argument sizeY must be " +
                    "positive (was " + sizeY + ")");
        }
        if(output == null) {
            throw new NullPointerException("output");
        }

        // Create BufferedImage representing given bit array.
        final BufferedImage image = new BufferedImage(
                sizeX, sizeY, BufferedImage.TYPE_BYTE_BINARY);
        final int[] pixels = unpackBits(bits, lsbFirst);
        final int scansize = eachLinePadded ? ((sizeX + 7) / 8) * 8 : sizeX;
        image.setRGB(0, 0, sizeX, sizeY, pixels, 0, scansize);

        // save the image
        ImageIO.write(image, "GIF", output);
    }

    /**
     * Unpacks bit array represented by bytes into array of integers.
     * Each bit set to one will be converted to int with all bits set to one
     * and each zero bit will be converted to integer with all bits to zero.
     * @param bits array of bits
     * @param lsbFirst true if least significant bit of each byte should
     *        start each output integer octet representing one input byte.
     * @return array o fintegers representing input bits
     */
    private static int[] unpackBits(byte[] bits, boolean lsbFirst) {
        final int[] result = new int[bits.length * 8];
        if(lsbFirst) {
            for(int i = 0; i < result.length; i++) {
                final int bit = bits[i >> 3] & (1 << (i & 7));
                result[i] = (bit == 0) ? 0 : ~0;
            }
        } else {
            for(int i = 0; i < result.length; i++) {
                final int bit = bits[i >> 3] & (128 >> (i & 7));
                result[i] = (bit == 0) ? 0 : ~0;
            }
        }
        return result;
    }

}
