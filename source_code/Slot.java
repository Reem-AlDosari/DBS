public class Slot {

    public int room,hour,day;
    public Boolean morning,brain,xray,online;

    // Constructor. 
    // In the following the starting index for all variables is assumed to be 0
    // Therefore index 0 here corresponds to room #1 in the problem

    public Slot(int day, int hour, int room) {
        this.room = room;
        this.hour = hour;
        this.day = day;
        this.morning= (hour <=3)? true:false;
        this.brain= (day==1 || day==3)? true:false; //Brain surgery is available every Monday and Wednesday
        this.xray= (room==0)? true:false; // x-ray device at room 1
        this.online= (room==1)? true:false; // online streaming for online consultation at room 2        
        }
}    