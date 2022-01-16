package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.outputs.JSONOutput;
import bgu.spl.mics.application.outputs.StudentOutput;
import bgu.spl.mics.application.services.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    private static CountDownLatch countDownLatch;
    private static ArrayList<StudentService> student_threads = new ArrayList<StudentService>();
    private static ArrayList<TimeService> timeservice_thread = new ArrayList<TimeService>();
    private static ArrayList<GPUService> gpu_threads = new ArrayList<GPUService>();
    private static ArrayList<CPUService> cpu_threads = new ArrayList<CPUService>();
    private static ArrayList<ConferenceService> conference_threads = new ArrayList<ConferenceService>();




    public static void main(String[] args) {
        initializeBus(args[0]);
        int threadNo = gpu_threads.size() + cpu_threads.size() + conference_threads.size();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        countDownLatch = new CountDownLatch(threadNo);
        for (GPUService gpu_Service : gpu_threads) {
            gpu_Service.setCountDownLatch(countDownLatch);
            Thread thread = new Thread(gpu_Service, "gpu " + gpu_Service.getGpu().getId());
            threads.add(thread);
            thread.start();
        }
        int p = 0;
        for (CPUService cpu_Service : cpu_threads) {
            cpu_Service.setCountDownLatch(countDownLatch);
            Thread thread = new Thread(cpu_Service, "cpu " + p);
            threads.add(thread);
            thread.start();
            p++;
        }
        int a = 0;
        for (ConferenceService conference_Service : conference_threads) {
            conference_Service.setCountDownLatch(countDownLatch);
            Thread thread = new Thread(conference_Service, "conference " + a);
            threads.add(thread);
            thread.start();
            a++;
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException exception) {
        }

        for (StudentService student_Service : student_threads) {
            int i = 0;
            Thread t = new Thread(student_Service, "student " + i);
            threads.add(t);
            t.start();
            i++;
        }

        Thread thread_1 = new Thread(timeservice_thread.get(0), "time service");
        threads.add(thread_1);
        thread_1.start();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException exception) {
            }
        }

        JSONOutput.GetInstance().setCpuTimeUsed(Cluster.getInstance().getGlobalCpuTime());
        JSONOutput.GetInstance().setBatchesProcessed(Cluster.getInstance().getBatchesGlobal());
        JSONOutput.GetInstance().setGpuTimeUsed(Cluster.getInstance().getGlobalGpuTime());


        for (StudentService stu : student_threads){
            Student student = stu.getStudent();
            StudentOutput studentOutput = new StudentOutput(student.getName(), student.getDepartment(), student.getDegree(),
                    student.getPublications(), student.getPapersRead(), stu.getTrainedModels());
            JSONOutput.GetInstance().addStudentOutputs(studentOutput);
        }
        JSONOutput.GetInstance().buildJson();
        //  for (Thread t : thread
    }
    //gh

    private static void initializeBus (String input) {
        try {

            JsonElement parser =JsonParser.parseReader(new FileReader(input));
            JsonObject json_Obj =parser.getAsJsonObject();

            JsonArray students_Arr =(JsonArray) json_Obj.get("Students");
            JsonArray gpu_Arr =(JsonArray) json_Obj.get("GPUS");
            JsonArray cpu_Arr =(JsonArray) json_Obj.get("CPUS");
            JsonArray conferences_Arr =(JsonArray) json_Obj.get("Conferences");
            int ticks =json_Obj.get("TickTime").getAsInt();

            int duration =json_Obj.get("Duration").getAsInt();

            parse_input_gpus(gpu_Arr);
            parse_input_cpus(cpu_Arr);
            parse_input_conferences(conferences_Arr);

            TimeService timeService =new TimeService(ticks, duration);
            timeservice_thread.add(timeService);
            parse_input_Students(students_Arr);

        } catch (FileNotFoundException exception) {}
    }


    //parse the students
    private static void parse_input_Students(JsonArray studentsArray){

        for (JsonElement student_e : studentsArray) {

            JsonObject JSonStudent = student_e.getAsJsonObject();
            JsonArray modelsArray = (JsonArray) JSonStudent.get("models");

            String name = JSonStudent.get("name").getAsString();
            String department = JSonStudent.get("department").getAsString();

            String status = JSonStudent.get("status").getAsString();
            Student student = new Student(name, department, status);


            for (JsonElement model_e : modelsArray) {
                JsonObject JSonModel = model_e.getAsJsonObject();
                String modelName = JSonModel.get("name").getAsString();
                String type = JSonModel.get("type").getAsString();
                int size = JSonModel.get("size").getAsInt();
                Data data = new Data(type, size);
                Model model = new Model(data, modelName, student);
                student.addModel(model);
            }
            StudentService student_S = new StudentService("student service", student);
            student_threads.add(student_S);
        }
    }



//parse the gpus
    private static void parse_input_gpus(JsonArray gpus_arr){
        for(int j=0; j<gpus_arr.size();j++){//
            String gpu_type = gpus_arr.get(j).getAsString();
            int type=2;
            if (gpu_type.equals("RTX3090"))
                type = 0;
            else if (gpu_type.equals("RTX2080"))
                type = 1;

            GPU gpu = new GPU(type, j);
            GPUService gpu_service = new GPUService("gpu service", gpu, countDownLatch);
            gpu_threads.add(gpu_service);
        }
    }

//parse the conference
    private static void parse_input_conferences(JsonArray conferences_arr){


        for(int i = 0; i< conferences_arr.size(); i++){
            JsonObject json_conference = conferences_arr.get(i).getAsJsonObject();
            int date = json_conference.get("date").getAsInt();
            String name = json_conference.get("name").getAsString();
            ConfrenceInformation conference_info = new ConfrenceInformation(name, date);
            ConferenceService Conference_service = new ConferenceService("conference service", conference_info, countDownLatch);
            conference_threads.add(Conference_service);

        }
    }

    //parse the cpus
    private static void parse_input_cpus(JsonArray cpus_arr){

        for(int i =0; i< cpus_arr.size(); i++){
            int size = cpus_arr.get(i).getAsInt();
            CPU cpu = new CPU(size);
            cpu_threads.add(new CPUService("cpu service", cpu, countDownLatch));
        }
    }

}

