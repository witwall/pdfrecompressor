/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdfrecompression;

/**
 *
 * @author radim
 */
public class PdfFile {
    private String fileName;
    private int numOfBiImages;
    private int numOfLZWImages;
    private int numOfOtherColoredImages;
    private int numOfAllImWithoutLZW;

    public PdfFile(String fileName, int numOfBiImages, int numOfLZWImages, int numOfOtherColoredImages) {
        this.fileName = fileName;
        this.numOfBiImages = numOfBiImages;
        this.numOfLZWImages = numOfLZWImages;
        this.numOfOtherColoredImages = numOfOtherColoredImages;
        this.numOfAllImWithoutLZW = numOfBiImages + numOfOtherColoredImages;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNumOfAllImWithoutLZW() {
        return numOfAllImWithoutLZW;
    }

    public void setNumOfAllIm(int numOfAllIm) {
        this.numOfAllImWithoutLZW = numOfAllIm;
    }

    public int getNumOfBiImages() {
        return numOfBiImages;
    }

    public void setNumOfBiImages(int numOfBiImages) {
        this.numOfBiImages = numOfBiImages;
    }

    public int getNumOfLZWImages() {
        return numOfLZWImages;
    }

    public void setNumOfLZWImages(int numOfLZWImages) {
        this.numOfLZWImages = numOfLZWImages;
    }

    public int getNumOfOtherColoredImages() {
        return numOfOtherColoredImages;
    }

    public void setNumOfOtherColoredImages(int numOfOtherColoredImages) {
        this.numOfOtherColoredImages = numOfOtherColoredImages;
    }
}
