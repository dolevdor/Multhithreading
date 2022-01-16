package bgu.spl.mics.application.outputs;

import bgu.spl.mics.application.objects.Data;
import com.google.gson.JsonObject;

public class ModelOutput {

    private String name;
    private Data data;
    private String status;
    private String result;
    private String type;
    private String size;

    public ModelOutput(String name, Data data, String status, String result){
        this.name = name;
        this.data = data;
        this.status = status;
        this.result = result;
        this.size = String.valueOf(data.getSize());
        this.type = String.valueOf(data.getType());
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        JsonObject dataObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("size", size);
        jsonObject.addProperty("data", String.valueOf(dataObject));
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("results", result);
        return jsonObject;
    }


}