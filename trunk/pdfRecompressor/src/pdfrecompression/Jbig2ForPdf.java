package pdfrecompression;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * class representing list of images compressed according to JBIG2 standard
 * in format suitable for pdf
 * and byte array of global Data
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class Jbig2ForPdf {

    private byte[] globalData;
    private List<PdfImage> jbig2Images = new ArrayList<PdfImage>();
    private List<String> jbFileNames = new ArrayList<String>();


    /**
     * constructor that reads jbig2 images and global data and saves them in array of bytes
     * @param pathToDir represents path to directory containing images data and global data
     * @throws pdfrecompression.PdfRecompressionException
     */
    public Jbig2ForPdf(String pathToDir) throws PdfRecompressionException {

        File directory = new File(pathToDir);
        if (!directory.isDirectory()) {
            throw new PdfRecompressionException("argument pathToDir doesn`t contain path to directory");
        }

        String[] fileNames = directory.list();
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File checkFile = new File(fileName);
            if (checkFile.isDirectory()) {
                continue;
            }
            int pointIndex;

            if ((fileName.lastIndexOf(".")+1) == (fileName.length() - 4)) {                
                if (fileName.startsWith("output")) {
                    String suffix = fileName.substring(fileName.length()-4);
                    try {
                        Integer.parseInt(suffix);
                        jbFileNames.add(fileName);
                        jbig2Images.add(new PdfImage(checkFile));
                    } catch (NumberFormatException ex) {
                    }
                }
            }
            if (fileName.equals("output.sym")) {
                Long sizeOfFile = checkFile.length();
                int imageSize = 0;
                FileInputStream jbImageInput = null;

                try {
                    jbImageInput = new FileInputStream(checkFile);
                    jbFileNames.add(fileName);
                    if (sizeOfFile > Integer.MAX_VALUE) {
                        throw new PdfRecompressionException("cannot process image greater than " + Integer.MAX_VALUE);
                    }

                    DataInput inputData = new DataInputStream(jbImageInput);
                    imageSize = sizeOfFile.intValue();
                    byte[] imageBytes = new byte[imageSize];
                    inputData.readFully(imageBytes);
                    globalData = imageBytes;
                } catch (FileNotFoundException ex) {
                    throw new PdfRecompressionException(ex);
                } catch (IOException ioEx) {
                    throw new PdfRecompressionException("io error", ioEx);
                }
            }
        }
    }

    /**
     * add pdf image
     * @param jbImage represents image encoding according to JBIG2 standard
     */
    public void addJbig2Image(PdfImage jbImage) {
        jbig2Images.add(jbImage);
    }

    /**
     * sets information to concrete image in the list
     * @param i represents position of the image in the list
     * @param pdfImageInformation represents information about that image
     */
    public void setJbig2ImageInfo(int i, PdfImageInformation pdfImageInformation) {
        jbig2Images.get(i).setPdfImageInformation(pdfImageInformation);
    }

    /**
     * Sets informations about pdf image given in List.
     * This list of information has to have the same order as images in the list of pdf images and the same count
     * @param pdfImageInformations represents list of informations about pdf images
     * @throws pdfrecompression.PdfRecompressionException if there is different number of informations and images
     */
    public void setJbig2ImagesInfo(List<PdfImageInformation> pdfImageInformations) throws PdfRecompressionException {
        if (pdfImageInformations == null) {
            throw new NullPointerException("pdfImageInformations");
        }

        if (pdfImageInformations.size() != jbig2Images.size()) {
            throw new PdfRecompressionException("there can't be difference in count of images and their informations");
        }

        for (int i = 0; i < jbig2Images.size(); i++) {
            setJbig2ImageInfo(i, pdfImageInformations.get(i));
        }
    }

    /**
     * sets global data of image
     * @param globalData represents global data
     */
    public void setGlobalData(byte[] globalData) {
        this.globalData = globalData;
    }

    /**
     * sets atribut jbig2Images
     * @param jbig2Images represents list of pdf images
     */
    public void setJbig2Images(List<PdfImage> jbig2Images) {
        this.jbig2Images = jbig2Images;
    }

    /**
     * @return return global data
     */
    public byte[] getGlobalData() {
        return globalData;
    }

    /**
     * @param index represents position of image in the list
     * @return image from the list from position given by parameter index
     */
    public PdfImage getJbig2Image(int index) {
        return jbig2Images.get(index);
    }

    /**
     * @return list of pdf images encoded according to JBIG2 standard
     */
    public List<PdfImage> getListOfJbig2Images() {
        return jbig2Images;
    }

    /**
     *
     * @return files that contains data of images and global data
     *         (output of jbig2enc with parameters -s and -p)
     */
    public List<String> getJbFileNames() {
        return jbFileNames;
    }
}
