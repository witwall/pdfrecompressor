#include "ocrResult.h"
#include <cmath>
#include <string.h>

using namespace std;


void OcrResult::addRecognizedChar(char character, int confidence) {
  this->charConfidences.insert(pair<char, int>(character, confidence));
  this->numOfChars++;
}


/**
 *  *chars .......... array of recognized chars
 *  *confidences .... array of confidences of chars 
 *		      (connected by index number = position in the array)
 *      -- last item in array is -1
 */
void OcrResult::setCharsWithConfidences(char *chars, int *confidences) {
  int i = 0;
  while (confidences[i] != -1) {
    this->charConfidences.insert(pair<char, int>(chars[i], confidences[i]));
    i++;
  }
  this->numOfChars+=i;
  this->recognizedText = chars;
}

void OcrResult::setRecognizedTextWithMeanConfidence(char * chars, int meanConfidence) {
  this->recognizedText = chars;
  this->meanConfidence = meanConfidence;
}

/**
 * counts distance between PIX int this OcrResult and another PIX
 * takes into account sizes of PIX
 */
float OcrResult::getPixDistance(PIX * otherPix) {
  float distance = 0.0;
  PIX *thisPix = this->pix;
  distance += fabs(thisPix->w - otherPix->w);
  distance += fabs(thisPix->h - otherPix->h);

  distance *= 0.1;

  PIX * pixd;
  pixd = pixXor(NULL, thisPix, otherPix);

  l_int32 init = 0;
  l_int32 *thisCount = &init;
  l_int32 *diffCount = &init;

  // counting number of ON pixels in thisPix
  if (pixCountPixels(thisPix, thisCount, NULL)) {
    fprintf(stderr, "Unable to count pixels\n");
    pixDestroy(&pixd);
    return 0;
  }

  // counting number of ON pixels in thisPix
  if (pixCountPixels(pixd, diffCount, NULL)) {
    fprintf(stderr, "Unable to count pixels\n");
    pixDestroy(&pixd);
    return 0;
  }

//   distance += ((*diffCount)/(thisPix->w * thisPix->h));
  distance += (*diffCount)/(*thisCount);

  return distance;
}

/**
 * counts distance of two ocr results (it should be checked before, that we compare only ocr results
 * with same lettersRecognized
 */
float OcrResult::getDistance(OcrResult * ocrResult) {
  float distance = 0.0;
  distance += strcmp(this->getRecognizedText(),ocrResult->getRecognizedText());
  distance += (getPixDistance(ocrResult->pix)*0.7);
  float confDiff = fabs(this->getConfidence()-ocrResult->getConfidence());

  float uncertainity = 0.0;
  if (this->getConfidence() > ocrResult->getConfidence()) {
    uncertainity = 100 - ocrResult->getConfidence();
  } else {
    uncertainity = 100 - this->getConfidence();
  }
  distance += (((confDiff/3.0)+1) * (uncertainity + 1))*0.3;
  distance *= (uncertainity/50);

  return distance;
}
