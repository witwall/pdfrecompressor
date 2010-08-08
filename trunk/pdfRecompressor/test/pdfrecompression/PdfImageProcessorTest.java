/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdfrecompression;

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

    public PdfImageProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        
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
        pdfs.add(new PdfFile("item_plus_oneLZW.pdf", 7,  1, 0));
//        pdfs.add(new PdfFile("newV6Colored.pdf", 0, 0, 1));
        pdfs.add(new PdfFile("repaired.pdf", 0, 0, 1));
        pdfs.add(new PdfFile("standardColored.pdf", 0, 0, 11));
//        pdfs.add(new PdfFile("suzuki.pdf", 0, 0, 2));
        pdfs.add(new PdfFile("unoptimized.pdf", 7, 1, 0));
        pdfs.add(new PdfFile("twoLayered.pdf", 4, 0, 0));
        pdfs.add(new PdfFile("oneLayered.pdf", 3, 0, 0));

        String dict = "testFiles/";

        for (int i = 0; i< pdfs.size(); i++) {
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
        fail("The test case is a prototype.");
    }

}