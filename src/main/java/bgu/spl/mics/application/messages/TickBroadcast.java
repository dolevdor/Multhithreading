package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    int currTime;
    public TickBroadcast(int currTime) {
        this.currTime = currTime;
    }

    public int getTime(){
        return currTime;
    }

}