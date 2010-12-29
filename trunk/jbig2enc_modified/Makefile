CC=g++
LEPTONICA=../leptonlib-1.67
TESSERACT=../tesseract-ocr
TESSERACT_INCLUDES=-I${TESSERACT}/api -I${TESSERACT}/ccmain -I${TESSERACT}/ccutil -I${TESSERACT}/ccstruct -I${TESSERACT}/image -I${TESSERACT}/viewer -I${TESSERACT}/dict -I${TESSERACT}/classify -I${TESSERACT}/display -I${TESSERACT}/wordrec -I${TESSERACT}/cutil -I${TESSERACT}/textord
#-Iccops

# For example, a fink MacOSX install:
# EXTRA=-I/sw/include/ -I/sw/include/libpng -I/sw/include/libjpeg -L/sw/lib
#CFLAGS=-I${LEPTONICA}/src ${TESSERACT_INCLUDES} -Wall -I/usr/include -I/usr/local/include -L/usr/lib -L/usr/local/lib -O3 ${EXTRA}
CFLAGS=-I${LEPTONICA}/src -Wall -I/usr/include -I/usr/local/include -L/usr/lib -L/usr/local/lib -O3 ${EXTRA}


jbig2: libjbig2enc.a jbig2.cc
	#$(CC) -o jbig2 jbig2.cc -L. -ljbig2enc ${LEPTONICA}/src/liblept.a ${TESSERACT}/api/.libs/libtesseract_api.a $(CFLAGS) -lpng -ljpeg -ltiff -lgif -lm 
	$(CC) -o jbig2 jbig2.cc -L. -ljbig2enc ${LEPTONICA}/src/.libs/liblept.a $(CFLAGS) -lpng -ljpeg -ltiff -lgif -lm 


libjbig2enc.a: jbig2enc.o jbig2arith.o jbig2sym.o jbig2comparator.o 
	# jbig2ocr.o ocrResult.o ocrResults.o
	ar -rcv libjbig2enc.a jbig2enc.o jbig2arith.o jbig2sym.o jbig2comparator.o #jbig2ocr.o ocrResult.o ocrResults.o

jbig2enc.o: jbig2enc.cc jbig2arith.h jbig2sym.h jbig2structs.h jbig2segments.h jbig2comparator.h
	$(CC) -c jbig2enc.cc $(CFLAGS)
jbig2comparator.o: jbig2comparator.cc jbig2comparator.h jbig2ocr.h
	$(CC) -c jbig2comparator.cc $(CFLAGS)
jbig2ocr.o: jbig2ocr.cc jbig2ocr.h ocrResult.h
	$(CC) -c jbig2ocr.cc $(CFLAGS)
ocrResult.o: ocrResult.h ocrResult.cc
	$(CC) -c ocrResult.cc $(CFLAGS)
ocrResults.o: ocrResults.h ocrResults.cc ocrResult.h
	$(CC) -c ocrResults.cc $(CFLAGS)
jbig2arith.o: jbig2arith.cc jbig2arith.h
	$(CC) -c jbig2arith.cc $(CFLAGS)
jbig2sym.o: jbig2sym.cc jbig2arith.h
	$(CC) -c jbig2sym.cc -DUSE_EXT $(CFLAGS)
clean:
	rm -f *.o jbig2 libjbig2enc.a
