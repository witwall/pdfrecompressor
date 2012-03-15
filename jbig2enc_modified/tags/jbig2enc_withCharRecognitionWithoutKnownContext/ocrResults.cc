#include "ocrResults.h"

using namespace std;

void OcrResultA::addTemplateResult(OcrResult * ocrResult, int position) {
  if (!ocrResult) {
    fprintf(stderr, "No OCR result information given");
    return;
  }
  if ((position < 0) || (position > this->n)) {
    fprintf(stderr, "position out of range");
    return;
  }

  int confidence = ocrResult->getConfidence();
  if (getBestTemplateOCR().getConfidence() < confidence) {
    int oldBestPixIndex = getIndexOfBestTemplateOCR();
    addIndexOfEquivalentTemplate(oldBestPixIndex);
    setIndexOfBestTemplate(position);
  } else {
    addIndexOfEquivalentTemplate(position);    
  }
  
}
