/*
 * =====================================================================================
 *
 *       Filename:  jbig2ocr.h
 *
 *    Description:  Class defining methods needed for running an OCR engine
 *
 *        Version:  1.0
 *        Created:  01/10/2012 03:11:54 PM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  Radim Hatlapatka (radim), hata.radim@gmail.com
 *        Company:  
 *
 * =====================================================================================
 */
#ifndef _JBIG2OCR_H_
#define _JBIG2OCR_H_

#include <pix.h>
#include "ocrResult.h"
#include <baseapi.h>

class OcrEngine {
  protected:
    const char * lang;
    int sourceResolution;

    public:
		
	  /*
       * initializes language for OCR and sourceResolution in ppi
	   */
	  OcrEngine(const char * lang, int sourceResolution) {
	    this->lang = lang;
        this->sourceResolution = sourceResolution;
	  }

      void setResolution(int ppi) {
        this->sourceResolution = ppi;
      }

      virtual ~OcrEngine() {};

      virtual void init() = 0;

      virtual OcrResult * recognizeLetter(PIX * pix) = 0; 
};

class TesseractOcr : public OcrEngine {
  private:
	tesseract::TessBaseAPI api;

  public:
	TesseractOcr(const char * lang) : OcrEngine(lang,0) { }
    TesseractOcr(const char * lang, int sourceResolution) : OcrEngine(lang, sourceResolution) {}

    ~TesseractOcr() {
      api.End();
	}

    void init();

    OcrResult * recognizeLetter(PIX * pix);
    void recognizeLetterDetailInfo(PIX * pix);
};

#endif
