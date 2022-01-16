package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {
    private Model model;
   // private Future f;

    public TrainModelEvent(Model model){
      //  f=null;
        this.model = model;
    }

    public Model getModel(){

        return model;
    }
//
   // public void Resolve(Model result) {
  //       model =result;
//        f.resolve(model);
//        this.notifyAll();
    }
//
//
//    public boolean isSent(){return f!=null;}
//    public boolean isResolved(){return f.isDone();}
//    public void SetFuture(Future<Model> f){ this.f=f;}
//}
