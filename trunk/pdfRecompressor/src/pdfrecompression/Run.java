/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfrecompression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 * @version 1.0
 */
public class Run {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PdfRecompressionException {
        if (args.length < 4) {
            usage();
        }

        String jbig2enc = null;
        String pdfFile = null;
        String outputPdf = null;
        String password = null;
        double defaultThresh = 0.85;
        Boolean autoThresh = false;
        Set<Integer> pagesToProcess = null;
        Boolean silent = false;
        Boolean binarize = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-input")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                pdfFile = args[i];
            } else {
                if (args[i].equalsIgnoreCase("-pathToEnc")) {
                    i++;
                    if (i >= args.length) {
                        usage();
                    }
                    jbig2enc = args[i];
                } else {
                    if (args[i].equalsIgnoreCase("-output")) {
                        i++;
                        if (i >= args.length) {
                            usage();
                        }
                        outputPdf = args[i];
                    } else {
                        if (args[i].equalsIgnoreCase("-passwd")) {
                            i++;
                            if (i >= args.length) {
                                usage();
                            }
                            password = args[i];
                        }
                    }
                    if (args[i].equalsIgnoreCase("-thresh")) {
                        i++;
                        if (i >= args.length) {
                            usage();
                        }

                        defaultThresh = Double.parseDouble(args[i]);
                        if ((defaultThresh > 0.9) || (defaultThresh < 0.5)) {
                            usage();
                        }
                    } else {
                        if (args[i].equalsIgnoreCase("-binarize")) {
                            binarize = true;
                        } else {
                            if (args[i].equalsIgnoreCase("-autoThresh")) {
                                autoThresh = true;
                            } else {
                                if (args[i].equalsIgnoreCase("-q")) {
                                    silent = true;
                                } else {
                                    if (args[i].equalsIgnoreCase("-pages")) {
                                        pagesToProcess = new HashSet<Integer>();
                                        i++;
                                        if (i >= args.length) {
                                            usage();
                                        }
                                        try {
                                            while (!args[i].equalsIgnoreCase("-pagesEnd")) {
                                                int page = Integer.parseInt(args[i]);
                                                pagesToProcess.add(page);
                                                i++;
                                                if (i >= args.length) {
                                                    usage();
                                                }
                                            }
                                        } catch (NumberFormatException ex) {
                                            System.err.println("list of page numbers can contain only numbers");
                                            usage();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((jbig2enc == null) || (pdfFile == null)) {
            usage();
        }

        if (outputPdf == null) {
            outputPdf = pdfFile;
        }

        File originalPdf = new File(pdfFile);

//        System.out.println("Processing " + pdfFile);
        long sizeOfInputPdf = new File(pdfFile).length();
        double startTime = System.currentTimeMillis();

        PdfImageProcessor pdfProcessing = new PdfImageProcessor();

        pdfProcessing.extractImagesUsingPdfParser(pdfFile, password, pagesToProcess, silent, binarize);
//        pdfProcessing.extractImagesUsingPdfObjectAccess(pdfFile, password, pagesToProcess, silent, binarize);
        List<String> jbig2encInputImages = pdfProcessing.getNamesOfImages();
        if (jbig2encInputImages.isEmpty()) {
            if (!silent) {
                System.out.println("No images in " + pdfFile + " to recompress");
            }
            System.exit(0);
        }
        runJbig2enc(jbig2enc, jbig2encInputImages, defaultThresh, autoThresh, silent);

        List<PdfImageInformation> pdfImagesInfo = pdfProcessing.getOriginalImageInformations();
        Jbig2ForPdf pdfImages = new Jbig2ForPdf(".");
        pdfImages.setJbig2ImagesInfo(pdfImagesInfo);

        OutputStream out = null;

        try {
            File fileName = new File(outputPdf);

            if (fileName.createNewFile()) {
                System.out.println("file " + outputPdf + " was created");
            } else {
                System.out.println("file " + outputPdf + " already exist => will be rewriten");
            }
            out = new FileOutputStream(fileName);
            pdfProcessing.replaceImageUsingIText(pdfFile, out, pdfImages, silent);
            long sizeOfOutputPdf = fileName.length();
            float saved = (((float) (sizeOfInputPdf - sizeOfOutputPdf)) / sizeOfInputPdf) * 100;
            System.out.println("Size of pdf before recompression = " + sizeOfInputPdf);
            System.out.println("Size of pdf file after recompression = " + sizeOfOutputPdf);
            System.out.println("=> Saved " + String.format("%.2f", saved) + " % from original size");
        } catch (IOException ex) {
            if (!silent) {
                System.err.println("writing output to the file caused error");
                ex.printStackTrace();
            }
            System.exit(2);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex2) {
                    if (!silent) {
                        ex2.printStackTrace();
                    }
                }
            }
        }

        int time = (int) (System.currentTimeMillis() - startTime) / 1000;
        int hour = time / 3600;
        int min = (time % 3600) / 60;
        int sec = (time % 3600) % 60;
        System.out.print("\n" + pdfFile + " succesfully recompressed in ");
        System.out.println(String.format("%02d:%02d:%02d", hour, min, sec));
        System.out.println("Totaly was recompressed " + pdfImages.getMapOfJbig2Images().size() + " images");

    }

    /**
     * @param filesToDelete list of fileNames to be deleted
     */
    public static void deleteFilesFromList(List<String> filesToDelete, boolean silent) {
        for (int i = 0; i < filesToDelete.size(); i++) {
            File fileToDelete = new File(filesToDelete.get(i));
            if (!fileToDelete.delete()) {
                if (!silent) {
                    System.err.println("problem to delete file: " + fileToDelete.getName());
                }
            }
        }
    }

    /**
     * run jbig2enc
     * @param jbig2enc represents path to jbig2enc
     * @param image input image to be compressed
     */
    private static void runJbig2enc(String jbig2enc, List<String> imageList, double defaultThresh, Boolean autoThresh, Boolean silent) throws PdfRecompressionException {
        if (jbig2enc == null) {
            throw new NullPointerException("No path to encoder given!");
        }

        if (imageList == null) {
            throw new NullPointerException("imageList");
        }


        if (imageList.isEmpty()) {
            throw new IllegalArgumentException("there are no images for running jbig2enc at (given list is empty)");
        }

        String images = "";
        for (int i = 0; i < imageList.size(); i++) {
            images = images + " " + imageList.get(i);
        }


        String run = jbig2enc + " -s -p";
        if (autoThresh) {
            run += " -autoThresh";
        }

        run += " -t " + defaultThresh;

        run += images;
        Runtime runtime = Runtime.getRuntime();
        Process pr1;
        try {
            pr1 = runtime.exec(run);
            InputStream erStream = pr1.getErrorStream();
            int exitValue = pr1.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(erStream));
            String line;
            while ((line = reader.readLine()) != null) {
//                writes only a number of symbols recognised by encoder and number of pages
//                String[] word = line.split(" ");
//                for (int i = 0; i < word.length; i++) {
//                    if (word[i].contains("symbols:")) {
//                        int differenciator = word[i].indexOf(":");
//                        String symNum = word[i].substring(differenciator+1);
//                        System.out.print(";" + symNum);
//                    }
//                    if (word[i].contains("pages:")) {
//                        int differenciator = word[i].indexOf(":");
//                        String pageNum = word[i].substring(differenciator+1);
//                        System.out.print(";" + pageNum);
//                    }
//                }


                System.out.println(line);
            }
            if (exitValue != 0) {
                if (!silent) {
                    System.err.println(run + " ended with error " + exitValue);
                }
                deleteFilesFromList(imageList, silent);
                System.exit(3);
            }
        } catch (IOException ex) {
            if (!silent) {
                System.err.println("runJbig2enc caused IOException");
            }
            ex.printStackTrace();
        } catch (InterruptedException ex2) {
            if (!silent) {
                ex2.printStackTrace();
            }
        } finally {
            deleteFilesFromList(imageList, silent);
        }
    }

    /**
     * write usage of main method
     */
    private static void usage() {
        System.err.println("Usage: -pathToEnc <Path to jbig2enc> -input <pdf file> [OPTIONAL]\n");
        System.err.println("Mandatory options:\n"
                + "-pathToEnc <Path to jbig2enc>: path to trigger of jbig2enc (usually file named jbig2)\n"
                + "-intput <pdf file>: pdf file that should be recompressed\n");

        System.err.println("OPTIONAL parameters:\n"
                + "-output <outputPdf>: name of output pdf file (if not given used input pdf file\n"
                + "-passwd <password>: password used for decrypting file\n"
                + "-thresh <valueOfDefaultThresholding>: value that is set to enkoder with switch -t\n"
                + "-autoThresh: engage automatic thresholding (special comparing between two symbols to make better compression ratio)\n"
                + "-pages <list of page numbers> -pagesEnd: list of pages that should be recompressed (taken only pages that exists, other ignored) -- now it is not working\n"
                + "-binarize: enables to process not bi-tonal images (normally only bi-tonal images are processed and other are skipped)\n"
                + "-q: silent mode -- no error output is printed");
        System.exit(1);
    }
}
