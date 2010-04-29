/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfrecompression;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfWriter;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.pdf.PRIndirectReference;
//import com.itextpdf.text.pdf.PRStream;
//import com.itextpdf.text.pdf.PdfDictionary;
//import com.itextpdf.text.pdf.PdfName;
//import com.itextpdf.text.pdf.PdfObject;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfStamper;
//import com.itextpdf.text.pdf.PdfStream;
//import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * This will read a pdf and extract images (names of images are stored in list and put back their compressed version)
 * 
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class PdfImageProcessor {

    private int imageCounter = 1;
    private List<String> namesOfImages = new ArrayList<String>();
    private List<PdfImageInformation> originalImageInformations = new ArrayList<PdfImageInformation>();

    /**
     * @return names of images in a list
     */
    public List<String> getNamesOfImages() {
        return namesOfImages;
    }

    /**
     *
     * @return list of informations about images
     */
    public List<PdfImageInformation> getOriginalImageInformations() {
        return originalImageInformations;
    }

    /**
     * extract images to files (each image in one file)
     * encrypted files handled by iText
     * @param pdfFile represents name of pdfFile, I want extract images from
     * @param password represents password to decrypt encrypted PDF file
     * @param pagesToProcess represents pages that should be recompressed
     *      (recompressed pages are just intersect of existing pages and pages given in the list pagesTo Process)
     * @throws PdfRecompressionException
     */
    public void extractImages(String pdfFile, String password, Set<Integer> pagesToProcess) throws PdfRecompressionException {

        // checking arguments and setting appropriate variables
        if (pdfFile == null) {
            throw new IllegalArgumentException(pdfFile);
        }

        String prefix = null;

        InputStream inputStream = null;
        if (password != null) {
            try {
                ByteArrayOutputStream decryptedOutputStream = null;
                PdfReader reader = new PdfReader(pdfFile, password.getBytes());
                PdfStamper stamper = new PdfStamper(reader, decryptedOutputStream);
                stamper.close();
                inputStream = new ByteArrayInputStream(decryptedOutputStream.toByteArray());
            } catch (DocumentException ex) {
                throw new PdfRecompressionException(ex);
            } catch (IOException ex) {
                throw new PdfRecompressionException("Reading file caused exception", ex);
            }
        } else {
            try {
                inputStream = new FileInputStream(pdfFile);
            } catch (FileNotFoundException ex) {
                throw new PdfRecompressionException("File wasn't found", ex);
            }
        }



        // if prefix is not set then prefix set to name of pdf without .pdf
        // if pdfFile has unconsistent name (without suffix .pdf) and name longer than 4 chars then last for chars are removed
        // and this string set as prefix
        if ((prefix == null) && (pdfFile.length() > 4)) {
            prefix = pdfFile.substring(0, pdfFile.length() - 4);
        }

        // loading pdfFile as PDDocument
        PDDocument document = null;
        try {
            document = PDDocument.load(inputStream);

            AccessPermission accessPermissions = document.getCurrentAccessPermission();

            if (!accessPermissions.canExtractContent()) {
                throw new PdfRecompressionException("Error: You do not have permission to extract images.");
            }

            // going page by page
            List pages = document.getDocumentCatalog().getAllPages();
            for (int pageNumber = 0; pageNumber < pages.size(); pageNumber++) {
                if ((pagesToProcess != null) && (!pagesToProcess.contains(pageNumber + 1))) {
                    continue;
                }
                PDPage page = (PDPage) pages.get(pageNumber);
                PDResources resources = page.getResources();



                // reading images from each page and saving them to file
                // (name of file is saved in list namSystem.err.println(images);esOfImages
                Map images = resources.getImages();
                if (images != null) {
                    Iterator imageIter = images.keySet().iterator();
                    while (imageIter.hasNext()) {
                        String key = (String) imageIter.next();
                        PDXObjectImage image = (PDXObjectImage) images.get(key);

                        PDStream pdStr = new PDStream(image.getCOSStream());
                        List filters = pdStr.getFilters();

                        if (image.getBitsPerComponent() > 1) {
                            System.err.println("It is not a bitonal image => skipping");
                            continue;
                        }

                        // at this moment for preventing bad output (bad coloring) from LZWDecode filter
                        if (filters.contains(COSName.LZW_DECODE.getName())) {
                            System.err.println("This is LZWDecoded => skipping");
                            continue;

                        }

                        // detection of unsupported filters by pdfBox library
                        if (filters.contains("JBIG2Decode")) {
                            System.err.println("Allready compressed according to JBIG2 standard => skipping");
                            continue;
                        }

                        if (filters.contains("JPXDecode")) {
                            System.err.println("Unsupported filter JPXDecode => skipping");
                            continue;
                        }

                        String name = getUniqueFileName(prefix + key, image.getSuffix());
                        System.out.println("Writing image:" + name);
                        image.write2file(name);
                        PdfImageInformation pdfImageInfo = new PdfImageInformation(key, image.getWidth(), image.getHeight(), pageNumber + 1);
                        originalImageInformations.add(pdfImageInfo);

                        namesOfImages.add(name + "." + image.getSuffix());
                    }
                }
            }
        } catch (IOException ioEx) {
            throw new PdfRecompressionException(ioEx);
        } finally {
            try {
                // closing pdf document
                if (document != null) {
                    document.close();
                }
            } catch (IOException ioEx) {
                throw new PdfRecompressionException(ioEx);
            }
        }
    }

    /**
     * get file name that is not used right now
     * @param prefix represents prefix of the name of file
     * @param suffix represents suffix of the name of file
     * @return file name that is not used right now
     */
    public String getUniqueFileName(String prefix, String suffix) {
        String uniqueName = null;
        File f = null;
        while ((f == null) || (f.exists())) {
            uniqueName = prefix + "-" + imageCounter;
            f = new File(uniqueName + "." + suffix);
            imageCounter++;
        }
        return uniqueName;
    }

    /**
     * extract stream of image using IText library
     * @param is represent input stream from pdf document
     * @throws pdfrecompression.PdfRecompressionException
     */
    public void extractImagesStreamsUsingIText(InputStream is) throws PdfRecompressionException {
        if (is == null) {
            throw new NullPointerException("is");
        }
        try {
            System.out.println("Running extractUsingItext");
            PdfReader reader = new PdfReader(is);
            for (int i = 0; i < reader.getXrefSize(); i++) {
                PdfObject pdfObj = reader.getPdfObject(i);
                if (pdfObj == null) {
                    System.err.println("Unable to read PDF Object");
                    continue;
                }
                if (!pdfObj.isStream()) {
                    continue;
                }
                PdfStream stream = (PdfStream) pdfObj;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                if (pdfsubtype == null) {
                    continue;
                }
                if (pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);

                    PdfObject pdfWidth = stream.get(PdfName.WIDTH);
                    int widht;
                    int height;
                    if (pdfWidth == null) {
                        System.out.println("Image without set width");
                        if (pdfWidth.isNumber()) {
                            System.out.println("Width = " + pdfWidth);
                        }
                    }
                    PdfObject pdfHeight = stream.get(PdfName.HEIGHT);
                    if (pdfHeight == null) {
                        System.out.println("Image without set width");
                        if (pdfHeight.isNumber()) {
                            System.out.println(", Height = " + pdfHeight);
                        }
                    }

                    PdfObject pdfBitsPerComponent = stream.get(PdfName.BITSPERCOMPONENT);
                    if (pdfBitsPerComponent == null) {
                        System.out.println("Image without set width");
                        if (pdfBitsPerComponent.isNumber()) {
                            System.out.println(", Bits per component = " + pdfBitsPerComponent);
                        }
                    }

//                    InputStream in = new ByteArrayInputStream(img);
//                    BufferedImage image = javax.imageio.ImageIO.read(in);




                // I've got raw bytes of image
                    java.awt.Image image = Toolkit.getDefaultToolkit().createImage(img);



                }

            }
        } catch (IOException ex) {
            throw new PdfRecompressionException("unable to read from input stream", ex);
        }
    }


    /**
     * replace images by they recompressed version according to JBIG2 standard
     * positions and image data given in imagesData
     * @param pdfName represents name of original pdf file
     * @param os represents output stream for writing changed pdf file
     * @param imagesData contains compressed images according to JBIG2 standard and informations about them
     * @throws PdfRecompressionException if version of pdf is lower than 1.4 or was catch DocumentException or IOException
     */
    public void replaceImageUsingIText(String pdfName, OutputStream os, Jbig2ForPdf imagesData) throws PdfRecompressionException {
        if (pdfName == null) {
            throw new NullPointerException("pdfName");
        }

        if (os == null) {
            throw new NullPointerException("os");
        }

        if (imagesData == null) {
            throw new IllegalArgumentException("imagesData is null => nothing to recompress");
        }


        PdfReader pdf;
        PdfStamper stp = null;
        try {
            pdf = new PdfReader(pdfName);



            stp = new PdfStamper(pdf, os);
            PdfWriter writer = stp.getWriter();
            int version;
            if ((version = Integer.parseInt(String.valueOf(pdf.getPdfVersion()))) < 4) {
                writer.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
            }

            List<PdfImage> jbig2Images = imagesData.getListOfJbig2Images();
            for (int i = 0; i < jbig2Images.size(); i++) {
                PdfImage jbImage = jbig2Images.get(i);
                PdfImageInformation jbImageInfo = jbImage.getPdfImageInformation();

                Image img = Image.getInstance(jbImageInfo.getWidth(), jbImageInfo.getHeight(), jbImage.getImageData(), imagesData.getGlobalData());

                PdfDictionary pg = pdf.getPageN(jbImageInfo.getPageNumber());
                PdfDictionary res =
                        (PdfDictionary) PdfReader.getPdfObject(pg.get(PdfName.RESOURCES));
                PdfDictionary xobj =
                        (PdfDictionary) PdfReader.getPdfObject(res.get(PdfName.XOBJECT));

                PdfObject obj = xobj.get((new PdfName(jbImageInfo.getKey())));
                if (obj.isIndirect()) {
                    PdfDictionary tg = (PdfDictionary) PdfReader.getPdfObject(obj);
                    PdfName type =
                            (PdfName) PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));
                    if (PdfName.IMAGE.equals(type)) {
                        PdfReader.killIndirect(obj);
                        Image maskImage = img.getImageMask();

                        if (maskImage != null) {
                            writer.addDirectImageSimple(maskImage);
                        }
                        writer.addDirectImageSimple(img, (PRIndirectReference) obj);
                    }
                }
            }
            stp.close();
        } catch (IOException ioEx) {
            throw new PdfRecompressionException(ioEx);
        } catch (DocumentException dEx) {
            throw new PdfRecompressionException(dEx);
        }

    }
}