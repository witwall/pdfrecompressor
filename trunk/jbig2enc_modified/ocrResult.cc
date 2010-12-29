#include "ocrResult.h"

using namespace std;


void OcrResult::addRecognizedChar(char character, int confidence) {
  this->charConfidences.insert(pair<char, int>(character, confidence));
  this->numOfChars++;
}


/**
 *  *chars .......... array of recognized chars
 *  *confidences .... array of confidences of chars 
		      (connected by index number = position in the array)
 *  n ............... number of symbols with confidences
 */
void OcrResult::setCharsWithConfidences(char *chars, int *confidences, int n) {
  for (int i = 0; i < n; i++) {
    this->charConfidences.insert(pair<char, int>(chars[i], confidences[i]));
  }
  this->numOfChars+=n;
}
