package pdfrecompression;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * class representing JBIG2 image in format suitable for pdf (without header,...)
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class PdfImage {

    private PdfImageInformation pdfImageInformation;
    private File imageDataFile;

    /**
     * constructor sets pointer to file containing image data, pdfImageInformation will be set later
     * @param imageDataFile represents pointer to file containing image data
     */
    public PdfImage(File imageDataFile) {
        if (imageDataFile == null) {
            throw new NullPointerException("imageDataFile");
        }
        this.imageDataFile = imageDataFile;
    }

    /**
     * constructor which sets both atributes (image data and informations about this image)
     * @param imageData represents data of image
     * @param pdfImageInformation represents associated information of image like width, height, position in original pdf,...
     */
    public PdfImage(File imageData, PdfImageInformation pdfImageInformation) {
        this.imageDataFile = imageData;
        this.pdfImageInformation = pdfImageInformation;
    }


    /**
     * return byte array of image data
     * @throws pdfrecompression.PdfRecompressionException if file wasn't found
     *      or there is too muchdata in the file that cannot be contained in one byte array
     */
    public byte[] getImageData() throws PdfRecompressionException {
        Long sizeOfFile = imageDataFile.length();
        int imageSize = 0;
        FileInputStream jbImageInput = null;

        try {
            jbImageInput = new FileInputStream(imageDataFile);
            if (sizeOfFile > Integer.MAX_VALUE) {
                throw new PdfRecompressionException("cannot process image greater than " + Integer.MAX_VALUE);
            }

            DataInput inputData = new DataInputStream(jbImageInput);
            imageSize = sizeOfFile.intValue();
            byte[] imageBytes = new byte[imageSize];
            inputData.readFully(imageBytes);
            return imageBytes;
        } catch (FileNotFoundException ex) {
            throw new PdfRecompressionException(ex);
        } catch (IOException ioEx) {
            throw new PdfRecompressionException("io error", ioEx);
        }
    }

    /**
     * sets information of pdf image by calling constructor
     * @param key represents pdf object key
     * @param pageNum represents number of page in PDF where is this image belong to
     * @param width represents with of image
     * @param height represents height of image
     */
    public void setPdfImageInformation(String key, int pageNum, int width, int height, int objNum, int genNum) {
        pdfImageInformation = new PdfImageInformation(key, width, height, objNum, genNum);
    }

    /**
     * sets information of pdf image dirrectly
     * @param pdfImageInformation represents information about image
     */
    public void setPdfImageInformation(PdfImageInformation pdfImageInformation) {
        this.pdfImageInformation = pdfImageInformation;
    }

    /**
     * @return informations about image
     */
    public PdfImageInformation getPdfImageInformation() {
        return pdfImageInformation;
    }

    /**
     *
     * @return file containing image data
     */
    public File getImageDataFile() {
        return imageDataFile;
    }
    
}
