#ifndef _OCRRESULT_H_
#define _OCRRESULT_H_

#include <allheaders.h>
#include <pix.h>
#include <map>

class OcrResult {
  private:
    PIX * pix;
    std::map<char, int> charConfidences;
    char * recognizedText;
    int numOfChars;
    int meanConfidence;
  
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

    PIX * getPix() {
      return pix;
    }

    void setPix(PIX * pix) {
      this->pix = pix;
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

    float getDistance(OcrResult * ocrResults);

    float getPixDistance(PIX * pix);

};
#endif
