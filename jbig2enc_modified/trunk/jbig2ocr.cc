// Copyright 2006 Google Inc. All Rights Reserved.
// Author: agl@imperialviolet.org (Adam Langley)
//
// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <map>
#include <vector>
#include <algorithm>

#include <stdio.h>
#include <string.h>

#include <allheaders.h>
#include <pix.h>

#include "ocrResult.h"

// from tesseract
#include <baseapi.h>
using namespace tesseract;

#include <math.h>
#if defined(sun)
#include <sys/types.h>
#else
#include <stdint.h>
#endif

#define u64 uint64_t
#define u32 uint32_t
#define u16 uint16_t
#define u8  uint8_t



/* need repair, linking causes error

 *  box contains info about position and size of PIX
 */
//OcrResult * recognizeLetter(PIX * pix, BOX * box) {
OcrResult * recognizeLetter(PIX * pix) {
  // using api of tesseract
  tesseract::TessBaseAPI api;
  //api.SetOutputName("recognized");
  api.Init("tesseract", "eng");  
  tesseract::PageSegMode pagesegmode = static_cast<tesseract::PageSegMode>(10);
  api.SetPageSegMode(pagesegmode);
  api.SetImage(pix);
  char * recognizedText = api.GetUTF8Text();
  int *confidences = api.AllWordConfidences();
  OcrResult * result = new OcrResult(pix);
  result->setCharsWithConfidences(recognizedText, confidences);
  fprintf(stderr, "recognized text: %s", recognizedText);
  return result;
}

