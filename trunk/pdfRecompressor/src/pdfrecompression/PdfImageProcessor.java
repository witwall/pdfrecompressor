/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfrecompression;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
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
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.common.PDStream;
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

    public void extractImagesUsingPdfParser(String pdfFile, String password, Set<Integer> pagesToProcess) throws PdfRecompressionException {
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




        PDFParser parser = null;
        COSDocument doc = null;
        try {
            parser = new PDFParser(inputStream);
            parser.parse();
            doc = parser.getDocument();


            List<COSObject> objs = doc.getObjectsByType(COSName.XOBJECT);
            for (COSObject obj : objs) {
                COSBase subtype = obj.getItem(COSName.SUBTYPE);
                if (subtype.toString().equalsIgnoreCase("COSName{Image}")) {
                    COSBase imageObj = obj.getObject();
                    String cosNameKey = obj.getItem(COSName.NAME).toString();
                    int startOfKey = cosNameKey.indexOf("{") + 1;
                    String key = cosNameKey.substring(startOfKey, cosNameKey.length() - 1);
                    int objectNum = obj.getObjectNumber().intValue();
                    int genNum = obj.getGenerationNumber().intValue();                    
                    PDXObjectImage image = (PDXObjectImage) PDXObjectImage.createXObject(imageObj);

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

                    String name = getUniqueFileName(prefix, image.getSuffix());
//                        System.out.println("Writing image:" + name);
                    image.write2file(name);


                    PdfImageInformation pdfImageInfo =
                            new PdfImageInformation(key, image.getWidth(), image.getHeight(), objectNum, genNum);
                    originalImageInformations.add(pdfImageInfo);

                    namesOfImages.add(name + "." + image.getSuffix());

                }

            }
        } catch (IOException ex) {
            throw new PdfRecompressionException("Unable to parse PDF document", ex);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException ex) {
                    throw new PdfRecompressionException(ex);
                }
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

        Map<PdfObjId, PdfImage> jbig2Images = imagesData.getMapOfJbig2Images();
        

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

            Iterator itImages = jbig2Images.values().iterator();
            String key;
            if (itImages.hasNext()) {
                PdfImage myImg = (PdfImage) itImages.next();
                key = myImg.getPdfImageInformation().getKey();
            } else {
                key = "im1";
            }

            for (int pageNum = 1; pageNum <= pdf.getNumberOfPages(); pageNum++) {

                PdfDictionary pg = pdf.getPageN(pageNum);
                PdfDictionary resPg =
                        (PdfDictionary) PdfReader.getPdfObject(pg.get(PdfName.RESOURCES));
                
                PdfDictionary xobjResPg =
                        (PdfDictionary) PdfReader.getPdfObject(resPg.get(PdfName.XOBJECT));

                PdfObject obj = null;
                if (xobjResPg != null) {
                    for (Iterator it = xobjResPg.getKeys().iterator(); it.hasNext();) {
                        PdfObject pdfObjIndirect = xobjResPg.get((PdfName) it.next());
                        if (pdfObjIndirect.isIndirect()) {
                            PdfDictionary pdfObj2 = (PdfDictionary) PdfReader.getPdfObject(pdfObjIndirect);
                            PdfDictionary xobj2Res = (PdfDictionary) PdfReader.getPdfObject(pdfObj2.get(PdfName.RESOURCES));

                            if (xobj2Res != null) {
                                for (Iterator it2 = xobj2Res.getKeys().iterator(); it2.hasNext();) {
                                    PdfObject resObj = xobj2Res.get((PdfName) it2.next());
                                }
                                PdfDictionary xobj = (PdfDictionary) PdfReader.getPdfObject(xobj2Res.get(PdfName.XOBJECT));
                                if (xobj == null) {
                                    continue;
                                }
                                obj = xobj.get(new PdfName(key));
                            } else {
                                obj = xobjResPg.get(new PdfName(key));
                            }
                        }
                    }
                }



                if ((obj != null) && obj.isIndirect()) {
                    PdfDictionary tg = (PdfDictionary) PdfReader.getPdfObject(obj);
                    PdfName type =
                            (PdfName) PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));
                    if (PdfName.IMAGE.equals(type)) {
                        PRIndirectReference ref = (PRIndirectReference) obj;
                        PdfObjId imId = new PdfObjId(ref.getNumber(), ref.getGeneration());
                        PdfImage jbImage = jbig2Images.get(imId);
                        PdfImageInformation jbImageInfo = jbImage.getPdfImageInformation();
                        Image img = Image.getInstance(jbImageInfo.getWidth(), jbImageInfo.getHeight(), jbImage.getImageData(), imagesData.getGlobalData());

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
        } finally {
            Run.deleteFilesFromList(imagesData.getJbFileNames());
        }
    }
}
