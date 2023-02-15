public class Doctor {
    public enum Types { CONSULTANT,SENIOR,JUNIOR }    

    public String name;
    public Boolean brain,xray,online;
    public Types type;  // 


// constructor

    public Doctor(String name, Types type) {
        this.name = name;
        this.brain= false;
        this.xray= false;
        this.online= false;
        this.type = type;     
        }
}    