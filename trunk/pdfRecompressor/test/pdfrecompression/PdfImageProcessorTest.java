/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfrecompression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class PdfImageProcessorTest {

    File testDir = new File("testData");

    public PdfImageProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
        if (testDir.exists()) {
            Tools.deleteFilesFromList(testDir.listFiles(), false);
        } else {
            testDir.createNewFile();
        }

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of extractImagesUsingPdfParser method, of class PdfImageProcessor.
     */
    @Test
    public void testExtractImagesUsingPdfParser() throws Exception {
        List<PdfFile> pdfs = new ArrayList<PdfFile>();
        pdfs.add(new PdfFile("item_plus_oneLZW.pdf", 7, 1, 0));
//        pdfs.add(new PdfFile("newV6Colored.pdf", 0, 0, 1));
//        pdfs.add(new PdfFile("repaired.pdf", 0, 0, 1));
        pdfs.add(new PdfFile("standardColored.pdf", 0, 0, 11));
//        pdfs.add(new PdfFile("suzuki.pdf", 0, 0, 2));
        pdfs.add(new PdfFile("unoptimized.pdf", 7, 1, 0));
        pdfs.add(new PdfFile("twoLayered.pdf", 4, 0, 0));
        pdfs.add(new PdfFile("oneLayered.pdf", 3, 0, 0));

        String dict = "testFiles/";

        for (int i = 0; i < pdfs.size(); i++) {
            PdfImageProcessor pdfProcessing = new PdfImageProcessor();
            PdfFile pdf = pdfs.get(i);
            pdfProcessing.extractImagesUsingPdfParser(dict + pdf.getFileName(), null, null, false, false);
            List<String> images = pdfProcessing.getNamesOfImages();
            Tools.deleteFilesFromList(images, true);
            assertEquals(pdf.getFileName(), pdf.getNumOfBiImages(), images.size());

            images.clear();
            pdfProcessing.extractImagesUsingPdfParser(dict + pdf.getFileName(), null, null, false, true);
            images = pdfProcessing.getNamesOfImages();
            Tools.deleteFilesFromList(images, true);
            assertEquals(pdf.getFileName(), pdf.getNumOfAllImWithoutLZW(), pdfProcessing.getNamesOfImages().size());
        }
    }

    /**
     * Test of replaceImageUsingIText method, of class PdfImageProcessor.
     */
    @Test
    public void testReplaceImageUsingIText() throws Exception {
        List<PdfFile> pdfs = new ArrayList<PdfFile>();

        List<PdfImageInformation> itemLZW = new ArrayList<PdfImageInformation>();
        itemLZW.add(new PdfImageInformation("im1", 2294, 3502, 54, 0));
        itemLZW.add(new PdfImageInformation("im1", 2299, 3490, 111, 0));
        itemLZW.add(new PdfImageInformation("im1", 2302, 3505, 190, 0));
        itemLZW.add(new PdfImageInformation("im1", 2297, 3615, 251, 0));
        itemLZW.add(new PdfImageInformation("im1", 2294, 3614, 319, 0));
        itemLZW.add(new PdfImageInformation("im1", 2295, 3538, 390, 0));
        // itemLZW.add(new PdfImageInformation("im1", 2299, 3554, 416, 0)); //LZW
        itemLZW.add(new PdfImageInformation("im1", 2138, 3539, 429, 0));

        pdfs.add(new PdfFile("item_plus_oneLZW", 7, 1, 0, itemLZW));



//        pdfs.add(new PdfFile("newV6Colored", 0, 0, 1));
//        pdfs.add(new PdfFile("repaired", 0, 0, 1));
//        pdfs.add(new PdfFile("standardColored", 0, 0, 11));
//        pdfs.add(new PdfFile("suzuki", 0, 0, 2));

        List<PdfImageInformation> twoLayered = new ArrayList<PdfImageInformation>();
        twoLayered.add(new PdfImageInformation("im1", 2758, 4425, 44, 0));
        twoLayered.add(new PdfImageInformation("im1", 2778, 4346, 53, 0));
        twoLayered.add(new PdfImageInformation("im1", 2780, 4422, 64, 0));
        twoLayered.add(new PdfImageInformation("im1", 2836, 4490, 84, 0));
        pdfs.add(new PdfFile("twoLayered", 4, 0, 0, twoLayered));

        List<PdfImageInformation> oneLayered = new ArrayList<PdfImageInformation>();
        oneLayered.add(new PdfImageInformation("im1", 2808, 4380, 5, 0));
        oneLayered.add(new PdfImageInformation("im1", 2808, 4438, 13, 0));
        oneLayered.add(new PdfImageInformation("im1", 2810, 4380, 23, 0));
        pdfs.add(new PdfFile("oneLayered", 3, 0, 0, oneLayered));

        File dict = new File("testFiles" + File.separator + "testReplacing");

        OutputStream out = null;

        for (int i = 0; i < pdfs.size(); i++) {
            out = new FileOutputStream(dict + "testOutput.pdf");
            try {
                PdfFile pdf = pdfs.get(i);
                File dictData = new File(dict, pdf.getFileName());
                Tools.deleteFilesFromList(testDir.listFiles(), false);
                Tools.copyDir(dictData, testDir);
                Jbig2ForPdf pdfImages = new Jbig2ForPdf(testDir.getPath());
                pdfImages.setJbig2ImagesInfo(pdf.getImages());
                PdfImageProcessor pdfProcessor = new PdfImageProcessor();
                pdfProcessor.replaceImageUsingIText(dictData + File.separator + pdf.getFileName() + ".pdf", out,
                        pdfImages, false);
            } catch (PdfRecompressionException ex) {
                ex.printStackTrace();
                fail(pdfs.get(i).getFileName() + ": unable to replace images - exception thrown");
            }finally {
                out.close();
            }
        }
    }
}


