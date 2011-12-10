#include "ocrResult.h"

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
