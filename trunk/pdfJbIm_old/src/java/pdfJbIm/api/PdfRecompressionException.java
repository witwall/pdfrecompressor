package pdfJbIm.api;

/**
 * Thrown when found problem with recompressing PDF file
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class PdfRecompressionException extends Exception {

    public PdfRecompressionException(String message) {
        super(message);
    }

    public PdfRecompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfRecompressionException(Throwable cause) {
        super(cause);
    }


}
