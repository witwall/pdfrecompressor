/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfJbIm.webapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import pdfJbIm.api.Jbig2ForPdf;
import pdfJbIm.api.PdfImageInformation;
import pdfJbIm.api.PdfImageProcessor;
import pdfJbIm.api.PdfRecompressionException;
import pdfJbIm.api.Tools;


/**
 *
 * @author radim
 */
public class pdfJbImServlet extends HttpServlet {

    private final static String JBIG2ENC = "/home/radim/projects/pdfrecompressor/trunk/jbig2enc_modified/jbig2";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
        } finally {
            out.close();
        }
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        Locale locale = request.getLocale();
//        ResourceBundle textFields;
//        try {
//            textFields = ResourceBundle.getBundle("textFields", locale);
//            request.setCharacterEncoding("utf-8");
//            response.setContentType("text/html;_charset=utf-8");
//        } catch (MissingResourceException misResEx) {
//            textFields = ResourceBundle.getBundle("textFields");
//        }
        if (ServletFileUpload.isMultipartContent(request)) {
            // Parse the HTTP request...
        }



        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(2500000); /* the unit is bytes */

//        File repositoryPath = new File("/tmp");
//        diskFileItemFactory.setRepository(repositoryPath);

        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        servletFileUpload.setSizeMax(10512184); /* the unit is bytes */
        InputStream is = null;
        double thresh = 0.85;
        int bwThresh = 188;
        Boolean binarize = false;
        Boolean silent = false;
        String outputPdf;
        long sizeOfInputPdf = 0;
        try {
            List fileItemsList = servletFileUpload.parseRequest(request);

            Iterator it = fileItemsList.iterator();
            while (it.hasNext()) {
                FileItem fileItem = (FileItem) it.next();
                if (fileItem.isFormField()) {
                    /* The file item contains a simple name-value pair of a form field */
                    String fieldName = fileItem.getFieldName();
                    String value = fileItem.getString();

                    if (fieldName.equals("thresh") && (value != null) && (!value.isEmpty())) {
                        try {
                            thresh = Double.parseDouble(value);
                        } catch (NumberFormatException ex) {
                            request.setAttribute("error", thresh + " must be a number with a floating point");
                        }
                    }
                } else {
                    /* The file item contains an uploaded file */
                    is = fileItem.getInputStream();


                    // System.out.println("Processing " + pdfFile);
                    sizeOfInputPdf = fileItem.getSize();
                }
            }
        } catch (SizeLimitExceededException ex) {
            /* The size of the HTTP request body exceeds the limit */
            request.setAttribute("error", "size of file for upload exceeded a limit");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } catch (FileUploadException ex) {
            Logger.getLogger(pdfJbImServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        File fileName = optimizePdf(is, binarize, silent, thresh, bwThresh, sizeOfInputPdf);
        request.setAttribute("success", fileName.getPath());
        request.getRequestDispatcher("/result.jsp").forward(request, response);


    }

    private File optimizePdf(InputStream is, Boolean binarize, Boolean silent, double thresh, int bwThresh, long sizeOfInputPdf) {
        double startTime = 0.0; //System.currentTimeMillis();
        PdfImageProcessor pdfProcessing = new PdfImageProcessor();
        OutputStream out = null;
        Jbig2ForPdf pdfImages = null;
        File fileName = null;
        try {
            File originalPdf = File.createTempFile("pdfJbIm", "pdf");
            String pdfFile = originalPdf.getName();

            pdfProcessing.extractImagesUsingPdfParser(is, pdfFile, null, null, false, binarize);
            // pdfProcessing.extractImagesUsingPdfObjectAccess(pdfFile, password, pagesToProcess, silent, binarize);
            List<String> jbig2encInputImages = pdfProcessing.getNamesOfImages();
            if (jbig2encInputImages.isEmpty()) {
                if (!silent) {
                   Logger.getLogger(pdfJbImServlet.class.getName()).log(Level.SEVERE, "No images in {0} to recompress", pdfFile);
                }
                // System.exit(0);
            }
            Tools.runJbig2enc(JBIG2ENC, jbig2encInputImages, thresh, true, bwThresh, originalPdf.getName(), silent);

            List<PdfImageInformation> pdfImagesInfo = pdfProcessing.getOriginalImageInformations();
            pdfImages = new Jbig2ForPdf(".", originalPdf.getName());
            pdfImages.setJbig2ImagesInfo(pdfImagesInfo);

            fileName = File.createTempFile("pdfJbIm-compressed", "pdf");
            out = new FileOutputStream(fileName);
            pdfProcessing.replaceImageUsingIText(pdfFile, out, pdfImages, silent);
            long sizeOfOutputPdf = fileName.length();
            float saved = (((float) (sizeOfInputPdf - sizeOfOutputPdf)) / sizeOfInputPdf) * 100;
//                        System.out.println("Size of pdf before recompression = " + sizeOfInputPdf);
//                        System.out.println("Size of pdf file after recompression = " + sizeOfOutputPdf);
//                        System.out.println("=> Saved " + String.format("%.2f", saved) + " % from original size");
            int time = (int) (System.currentTimeMillis() - startTime) / 1000;
            int hour = time / 3600;
            int min = (time % 3600) / 60;
            int sec = (time % 3600) % 60;
            System.out.print("\n" + pdfFile + " succesfully recompressed in ");
            System.out.println(String.format("%02d:%02d:%02d", hour, min, sec));
            System.out.println("Totaly was recompressed " + pdfImages.getMapOfJbig2Images().size() + " images");
            
        } catch (PdfRecompressionException ex) {
            Logger.getLogger(pdfJbImServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
//                        if (!silent) {
//                            System.err.println("writing output to the file caused error");
//                            ex.printStackTrace();
//                        }
            System.exit(2);
        } finally {
            return fileName;
        }
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet for running pdfJbIm";
    }
}
