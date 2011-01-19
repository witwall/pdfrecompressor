/*
 *  Copyright 2011 Radim Hatlapatka.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 */
public class Jbig2enc {

    private String jbig2enc;
    private double defaultThresh = 0.85;
    private Boolean autoThresh = false;
    private int bwThresh = 188;
    private Boolean silent = false;

    public Jbig2enc(String jbig2enc) {
        if (jbig2enc == null) {
            throw new NullPointerException("No path to encoder given!");
        }
        this.jbig2enc = jbig2enc;
    }

    public Jbig2enc() {
        this.jbig2enc = "jbig2";
    }

    public Boolean getAutoThresh() {
        return autoThresh;
    }

    public void setAutoThresh(Boolean autoThresh) {
        if (autoThresh != null) {
            this.autoThresh = autoThresh;
        }
    }

    public Integer getBwThresh() {
        return bwThresh;
    }

    public void setBwThresh(int bwThresh) {
        if ((bwThresh < 0) || (bwThresh > 255)) {
            throw new IllegalArgumentException("bwThresh");
        }
        this.bwThresh = bwThresh;
    }

    public Double getDefaultThresh() {
        return defaultThresh;
    }

    public void setDefaultThresh(double defaultThresh) {
        if ((defaultThresh < 0.4) || (defaultThresh > 0.9)) {
            throw new IllegalArgumentException("defaultThresh");
        }
        this.defaultThresh = defaultThresh;
    }

    public String getJbig2enc() {
        return jbig2enc;
    }

    public void setJbig2enc(String jbig2enc) {
        this.jbig2enc = jbig2enc;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    /**
     * run jbig2enc with symbol coding used and output in format suitable for PDF
     * @param defaultThresh what thresholding value should be used
     * @param autoThresh if improvement of jbig2enc should be used
     * @param bwThresh setting black/white thresholding
     * @param silent turns off error output
     * @param jbig2enc represents path to jbig2enc
     * @param imageList list of images to be compressed
     */
    public void run(List<String> imageList, String basename) throws PdfRecompressionException {
        if (basename == null) {
            basename = "output";
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



        List<String> toRun = new ArrayList<String>();

        toRun.add(jbig2enc);
        toRun.add("-s");
        toRun.add("-p");
        toRun.add("-b");
        toRun.add(basename);
        toRun.add("-t");
        toRun.add(String.valueOf(defaultThresh));
        toRun.add("-T");
        toRun.add(String.valueOf(bwThresh));

        if (autoThresh) {
            toRun.add("-autoThresh");
        }

        toRun.addAll(imageList);

        String[] run = new String[toRun.size()];
        run = toRun.toArray(run);

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
                    System.err.println("jbig2enc ended with error " + exitValue);
                }
                Tools.deleteFilesFromList(imageList, silent);
                System.exit(3);
            }
        } catch (IOException ex) {
            if (!silent) {
                System.err.println("runJbig2enc caused IOException");
                ex.printStackTrace(System.err);
            }

        } catch (InterruptedException ex2) {
            if (!silent) {
                ex2.printStackTrace(System.err);
            }
        } finally {
            Tools.deleteFilesFromList(imageList, silent);
        }
    }
}
