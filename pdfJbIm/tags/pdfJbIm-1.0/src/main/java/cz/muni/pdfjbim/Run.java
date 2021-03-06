
/*
 *  Copyright 2010 Radim Hatlapatka.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package cz.muni.pdfjbim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class Run {

    private static final Logger logger = LoggerFactory.getLogger(Run.class);

    /**
     * @param args the command line arguments
     * @throws PdfRecompressionException 
     */
    public static void main(String[] args) throws PdfRecompressionException {
        if (args.length < 4) {
            usage();
        }

        String jbig2enc = null;
        String pdfFile = null;
        String outputPdf = null;
        String password = null;
        Double defaultThresh = 0.85;
        Integer bwThresh = 188;
        Boolean autoThresh = false;
        Set<Integer> pagesToProcess = null;
        Boolean silent = false;
        Boolean binarize = false;
        String basename = "output";


        // parsing arguments of main method
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-h")) {
                usage();
            }
            if (args[i].equalsIgnoreCase("-input")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                pdfFile = args[i];
                continue;
            }

            if (args[i].equalsIgnoreCase("-pathToEnc")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                jbig2enc = args[i];
                continue;
            }

            if (args[i].equalsIgnoreCase("-output")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                outputPdf = args[i];
                continue;
            }

            if (args[i].equalsIgnoreCase("-passwd")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                password = args[i];
                continue;
            }

            if (args[i].equalsIgnoreCase("-basename")) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                basename = args[i];
                continue;
            }

            if (args[i].equalsIgnoreCase("-thresh")) {
                i++;
                if (i >= args.length) {
                    usage();
                }

                defaultThresh = Double.parseDouble(args[i]);
                if ((defaultThresh > 0.9) || (defaultThresh < 0.5)) {
                    System.err.println("Invalid threshold value: (0.5..0.9)\n");
                    usage();
                }
                continue;
            }
            

            if (args[i].equalsIgnoreCase("-bw_thresh")) {
                i++;
                if (i >= args.length) {
                    usage();
                }

                bwThresh = Integer.parseInt(args[i]);
                if ((bwThresh < 0) || (bwThresh > 255)) {
                    System.err.println("Invalid bw threshold value: (0..255)\n");
                    usage();
                }
                continue;
            }

            if (args[i].equalsIgnoreCase("-binarize")) {
                binarize = true;
                continue;
            }

            if (args[i].equalsIgnoreCase("-autoThresh")) {
                autoThresh = true;
                continue;
            }

            if (args[i].equalsIgnoreCase("-q")) {
                silent = true;
                continue;
            }

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
                continue;
            }
        }

        if ((jbig2enc == null) || (pdfFile == null)) {
            usage();
        }

        if (outputPdf == null) {
            outputPdf = pdfFile;
        }

        // originalPdf is an input PDF which shall be recompressed
        File originalPdf = new File(pdfFile);

        // initialization for counting time of recompression
        long sizeOfInputPdf = new File(pdfFile).length();
        double startTime = System.currentTimeMillis();

        // PdfImageProcessor handles extraction of pdf and putting recompressed images
        PdfImageExtractor imageExtractor = new PdfImageExtractor();

        imageExtractor.setSilent(silent); // if stderr output shall be printed or not

        // image extraction
        imageExtractor.extractImages(pdfFile, password, pagesToProcess, binarize);

        // returns names of extracted images as List
        List<String> jbig2encInputImages = imageExtractor.getNamesOfImages();
        if (jbig2encInputImages.isEmpty()) {
            if (!silent) {
                logger.info("No images in " + pdfFile + " to recompress");
            }
//            System.exit(0);
        } else {
            // setting parameters for jbig2enc
            Jbig2enc jbig2 = new Jbig2enc(jbig2enc);

            jbig2.setAutoThresh(autoThresh); // engages modified version of the jbig2 encoder
            jbig2.setBwThresh(bwThresh);
            jbig2.setDefaultThresh(defaultThresh);
            jbig2.setSilent(silent);

            // engages jbig2enc with set parameters and creates output files based on basename
            jbig2.run(jbig2encInputImages, basename);
        }

        // getting informations about images that were in PDF such as size, position in PDF,...
        List<PdfImageInformation> pdfImagesInfo = imageExtractor.getOriginalImageInformations();

        // reading output of encoder and associating with informations about them
        Jbig2ForPdf pdfImages = new Jbig2ForPdf(".", basename);
        pdfImages.setJbig2ImagesInfo(pdfImagesInfo);

        // creating output
        OutputStream out = null;
        try {
            File fileName = new File(outputPdf);

            if (fileName.createNewFile()) {
                if (!silent) {
                    logger.info("file " + outputPdf + " was created");
                }
            } else {
                if (!silent) {
                    logger.info("file " + outputPdf + " already exist => will be rewriten");
                }
            }
            out = new FileOutputStream(fileName);

            // replaces images with their recompressed version based on image info and is stored
            // in output stream (out)
            PdfImageReplacer imageReplacer = new PdfImageReplacer();
            imageReplacer.setSilent(true);
            imageReplacer.replaceImageUsingIText(pdfFile, out, pdfImages);

            // counting some logging info concerning sizes of input vs output
            long sizeOfOutputPdf = fileName.length();
            float saved = (((float) (sizeOfInputPdf - sizeOfOutputPdf)) / sizeOfInputPdf) * 100;
            System.out.println("Size of pdf before recompression = " + sizeOfInputPdf);
            System.out.println("Size of pdf file after recompression = " + sizeOfOutputPdf);
            System.out.println("=> Saved " + String.format("%.2f", saved) + " % from original size");
        } catch (IOException ex) {
            logger.warn("writing output to the file caused error", ex);
            System.exit(2);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex2) {
                    if (!silent) {
                        ex2.printStackTrace(System.err);
                    }
                }
            }
        }


        // counting some logging info concernig time taken by recompressor
        int time = (int) (System.currentTimeMillis() - startTime) / 1000;
        int hour = time / 3600;
        int min = (time % 3600) / 60;
        int sec = (time % 3600) % 60;
        System.out.print("\n" + pdfFile + " succesfully recompressed in ");
        System.out.println(String.format("%02d:%02d:%02d", hour, min, sec));
        System.out.println("Totaly was recompressed " + pdfImages.getMapOfJbig2Images().size() + " images");
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
                + "-thresh <valueOfDefaultThresholding>: value that is set to encoder with switch -t\n"
                + "-autoThresh: engage automatic thresholding (special comparing between two symbols to make better compression ratio)\n"
                + "-bw_thresh <value of BW thresholding>: sets value for bw thresholding to encoder (in jbig2enc it is switch -T)\n"
                + "-pages <list of page numbers> -pagesEnd: list of pages that should be recompressed (taken only pages that exists, other ignored) -- now it is not working\n"
                + "-binarize: enables to process not bi-tonal images (normally only bi-tonal images are processed and other are skipped)\n"
                + "-basename <basename> sets the basename for output files of jbig2enc"
                + "-q: silent mode -- no error output is printed");
        System.exit(1);
    }
}
