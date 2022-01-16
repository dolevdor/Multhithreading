package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed = 0;
    private int size;

    public Data(String type, int size){
        if (type.equals("Images"))
            this.type = Type.Images;
        else if (type.equals("Text"))
            this.type = Type.Images;
        else
            this.type = Type.Tabular;
        this.size = size;
    }

    public int getSize(){return size;}

    public void setProcessed(int x){
        processed = processed + x;
    }

    public int getType(){
        if (type == Type.Images)
            return 0;
        if (type == Type.Text)
            return 1;
        else
            return 2;
    }
}
