/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfrecompression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 * @author radim
 */
public class Tools {

    /**
     * run jbig2enc
     * @param jbig2enc represents path to jbig2enc
     * @param image input image to be compressed
     */
    public static void runJbig2enc(String jbig2enc, List<String> imageList, double defaultThresh, Boolean autoThresh, int bwThresh, Boolean silent) throws PdfRecompressionException {
        if (jbig2enc == null) {
            throw new NullPointerException("No path to encoder given!");
        }

        if (imageList == null) {
            throw new NullPointerException("imageList");
        }


        if (imageList.isEmpty()) {
            if (!silent) {
                System.err.println("there are no images for running jbig2enc at (given list is empty)");
            }
            return;
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

        run += " -T " + bwThresh;

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
     * @param filesToDelete list of fileNames to be deleted
     */
    public static void deleteFilesFromList(List<String> filesToDelete, boolean silent) {
        for (int i = 0; i < filesToDelete.size(); i++) {
            File fileToDelete = new File(filesToDelete.get(i));
            if (!fileToDelete.delete()) {
                if (!silent) {
                    System.err.println("problem to delete file: " + fileToDelete.getPath());
                }
            }
        }
    }

    /**
     * @param filesToDelete list of fileNames to be deleted
     */
    public static void deleteFilesFromList(File[] filesToDelete, boolean silent) {
        for (int i = 0; i < filesToDelete.length; i++) {
            File fileToDelete = filesToDelete[i];
            if (!fileToDelete.delete()) {
                if (!silent) {
                    System.err.println("problem to delete file: " + fileToDelete.getPath());
                }
            }
        }
    }

    public static void copy(File fromFile, File toFile) throws IOException {
        

        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFile.getName());
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFile.getName());
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFile.getName());
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFile.getName());
            }
            System.out.println("Overwrite existing file " + toFile.getName());
            System.out.flush();
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyDir(File sourceDir, File destinationDir) throws IOException {
        
        if (sourceDir == null) {
            throw new NullPointerException("sourceDir");
        }

        if (destinationDir == null) {
            throw new NullPointerException("toDir");
        }

        if ((!sourceDir.exists()) || (!destinationDir.exists())) {
            throw new IllegalArgumentException(sourceDir.getPath() + " or "
                    + destinationDir.getPath() + " doesn't exist");
        }

        if (!sourceDir.isDirectory())  {
            throw new IllegalArgumentException(sourceDir.getPath() + " is not a directory");
        }

        if (!destinationDir.isDirectory()) {
            throw new IllegalArgumentException(destinationDir.getPath() + " is not a directory");
        }

        File[] sourceFiles = sourceDir.listFiles();
        for (int i = 0; i < sourceFiles.length; i++) {
            if (!sourceFiles[i].isDirectory()) {
                copy(sourceFiles[i], destinationDir);
            }
        }
    }
}
