package bgu.spl.mics.application.outputs;

import bgu.spl.mics.application.objects.Model;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class StudentOutput {

    private String name;
    private String department;
    private String status;
    private int publications;
    private int papersRead;
    private LinkedList<ModelOutput> trainedModels;

    public StudentOutput(String name, String department, String status, int publications, int papersRead, LinkedList<ModelOutput> trainedModels){
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = publications;
        this.papersRead = papersRead;
        this.trainedModels = trainedModels;
    }

    public void addModel(ModelOutput modelOutput){
        trainedModels.add(modelOutput);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("department", department);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("publications", publications);
        jsonObject.addProperty("papersRead", papersRead);
        JsonArray list = new JsonArray();
        for (ModelOutput model: trainedModels) {
            list.add(model.toJson());
        }
        jsonObject.add("trainedModels", list);
        return jsonObject;
    }
}