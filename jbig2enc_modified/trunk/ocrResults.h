#ifndef _OCRRESULTS_H_
#define _OCRRESULTS_H_

#include <allheaders.h>
#include <pix.h>
#include <vector>
#include "ocrResult.h"

class OcrResultA {
  private:
    static OcrResult *ocrResults;
    static int n; // number of ocrResults

    char * recognizedText;
    int theBestTemplate; // -1 means not known
    std::vector<int> indexesOfTheEquivalentTemplates;
  
  public:
    OcrResultA() {
      theBestTemplate = -1;
    }

    void addIndexOfEquivalentTemplate(int pixIndex) {
      indexesOfTheEquivalentTemplates.push_back(pixIndex);
    }

    void setIndexOfBestTemplate(int newBestPixIndex) {      
      this->theBestTemplate = newBestPixIndex;
    }

    void init(OcrResult * ocrResults, int n) {
      if (ocrResults != NULL) {
        this->ocrResults = ocrResults;
        this->n = n;
      }
    }

    void init(int n) {
      if (n <= 0) {
        fprintf(stderr, "Number of OcrResults must be a positive number");
        return;
      }
      this->ocrResults = new OcrResult[n];
      this->n = n;
    }

    ~OcrResultA() {
      delete[] ocrResults;
      /*charConfidences->clear;*/
    }

    void addTemplateResult(OcrResult *resultToBeAdd, int position);


    int getIndexOfBestTemplateOCR() {
      return this->theBestTemplate;
    }

    OcrResult getBestTemplateOCR() {
      return ocrResults[this->theBestTemplate];
    }

    static int getOcrResultALength() {
      return n;
    }
    
    static OcrResult * getOcrResultA() {
      return ocrResults;
    }
};
#endif
