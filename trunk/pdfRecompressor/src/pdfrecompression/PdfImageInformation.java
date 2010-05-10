/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdfrecompression;

/**
 * Contains information about size of image and it's position in original PDF
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class PdfImageInformation {


    private String key;
    private int width;
    private int height;
    private int pageNumber;
    private int objectNum;
    private int objectGenNum;


    /**
     *
     * @param key represents pdf object key to which was associated this image
     * @param width represents width of image
     * @param height represents height of image
     * @param pageNumber represents page number in original pdf dokument
     */
    public PdfImageInformation(String key, int width, int height, int objectNum, int genNum) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.pageNumber = pageNumber;
        this.objectGenNum = genNum;
        this.objectNum = objectNum;
    }

    /**
     * @return height of image
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return pdf object key of image
     */
    public String getKey() {
        return key;
    }

    /**
     * @return page number where the image in pdf was at
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return width of image
     */
    public int getWidth() {
        return width;
    }

    public int getObjectGenNum() {
        return objectGenNum;
    }

    public void setObjectGenNum(int objectGenNum) {
        this.objectGenNum = objectGenNum;
    }

    public int getObjectNum() {
        return objectNum;
    }

    public void setObjectNum(int objectNum) {
        this.objectNum = objectNum;
    }

}
