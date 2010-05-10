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


    /**
     *
     * @param key represents pdf object key to which was associated this image
     * @param width represents width of image
     * @param height represents height of image
     * @param pageNumber represents page number in original pdf dokument
     */
    public PdfImageInformation(String key, int width, int height, int pageNumber) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.pageNumber = pageNumber;
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


}
