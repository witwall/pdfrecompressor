#ifndef _OCRRESULT_H_
#define _OCRRESULT_H_

#include <allheaders.h>
#include <pix.h>
#include <map>
#include "result.h"

class OcrResult : public Result {
  private:
    std::map<char, int> charConfidences;
    char * recognizedText;
    int numOfChars;
    int meanConfidence;
    float getPixDistance(PIX * pix);
 
  public:
    OcrResult() {
      this->numOfChars = 0;
    }

    OcrResult(PIX *pix) {
      this->pix = pix;
      this->numOfChars = 0;
    }

    ~OcrResult() {
      charConfidences.clear();
    }

    std::map<char, int> getCharConfidences() {
      return this->charConfidences;
    }
    
	/*int getConfidence() {*/
	/*if (charConfidences.size()==0) {*/
	/*return -1;*/
	/*}*/
	/*std::map<char, int>::iterator it;*/
	/*int sum = 0;*/
	/*for (it = this->charConfidences.begin(); it != this->charConfidences.end(); it++) {*/
	/*sum += it->second;*/
	/*}*/
	/*return sum / charConfidences.size();*/
	/*}*/

    int getConfidence() {
      return this->meanConfidence;
    }

    void setConfidence(int meanConfidence) {
      this->meanConfidence = meanConfidence;
    }

    char * getRecognizedText() {
      return recognizedText;
    }

    int getNumOfChars() {
      return numOfChars;
    }

    void addRecognizedChar(char symbol, int confidence);

    /**
     *  *chars .......... array of recognized chars
     *  *confidences .... array of confidences of chars 
			  (connected by index number = position in the array)
     *  last item in array has value -1
     */
    void setCharsWithConfidences(char *chars, int *confidences);

    void setRecognizedTextWithMeanConfidence(char * chars, int meanConfidence);

    virtual float getDistance(OcrResult * ocrResults);
  
};
#endif
