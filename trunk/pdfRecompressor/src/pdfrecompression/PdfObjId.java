/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pdfrecompression;

/**
 *
 * @author radim
 */
public class PdfObjId {
    private int objectNumber;
    private int generationNumber;

    public PdfObjId(int objectNumber, int generationNumber) {
        this.objectNumber = objectNumber;
        this.generationNumber = generationNumber;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public void setGenerationNumber(int generationNumber) {
        this.generationNumber = generationNumber;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    @Override
    public String toString() {
        return objectNumber + " " + generationNumber + " obj";
    }

    public String getAsObjReference() {
        return objectNumber + " " + generationNumber + " R";
    }

    public String getAsObjIdentifier() {
        return this.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof PdfObjId) {
                PdfObjId objId = (PdfObjId) obj;
                return (objId.objectNumber == this.objectNumber) && (objId.generationNumber == this.generationNumber);
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.objectNumber;
        hash = 97 * hash + this.generationNumber;
        return hash;
    }



}
