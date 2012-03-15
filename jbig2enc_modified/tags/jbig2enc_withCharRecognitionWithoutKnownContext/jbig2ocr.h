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

    public:
		
	  /*
       * initializes language for OCR 
	   */
	  OcrEngine(const char * lang) {
	    this->lang = lang;
	  }

      virtual ~OcrEngine() {};

      virtual void init() = 0;

      virtual OcrResult * recognizeLetter(PIX * pix) = 0; 
};

class TesseractOcr : public OcrEngine {
  private:
	tesseract::TessBaseAPI api;

  public:
	TesseractOcr(const char * lang) : OcrEngine(lang) { }

    ~TesseractOcr() {
      api.End();
	}

    void init();

    OcrResult * recognizeLetter(PIX * pix);
};

#endif
