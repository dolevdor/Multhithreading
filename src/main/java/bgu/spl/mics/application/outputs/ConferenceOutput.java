package bgu.spl.mics.application.outputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class ConferenceOutput {

    private String name;
    private int date;
    private List<ModelOutput> publications;

    public ConferenceOutput(String name, int date, List<ModelOutput> publications){
        this.name = name;
        this.date = date;
        this.publications = publications;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("date", date);
        JsonArray jsonArray = new JsonArray();
        for (ModelOutput modelOutput : publications) {
            jsonArray.add(modelOutput.toJson());
        }
        jsonObject.add("publications", jsonArray);
        return jsonObject;
    }

    public String getName(){
        return name;
    }
}