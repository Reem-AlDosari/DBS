import java.util.ArrayList;
import java.util.List;

import java.util.Random;
import java.util.Scanner;

public class App {
	private static Scanner in;
    static Slot[] slot;
    static Doctor[] doctor;

    static Doctor[][] shuffled_doctor;
    

    static int counter;

    final static String[] HoursName= {"  8-9", " 9-10","10-11","11-12"," 12-1","  1-2","  2-3","  3-4"};

    final static int Ndays=5;        
    final static int Nhours=8;
    final static int Nrooms=3;
    final static int Nslots=Ndays*Nhours*Nrooms;

    static Boolean SearchDone=false;            // flag to terminate the search algorithm
    static List<Doctor> SrSchedule,JrSchedule;  // Stores the scheduling results for:
                                                // SrSchedule: Consultant and Senior doctors
                                                // JrSchedule: Junior doctors
    
    final static String VacantSlotName="None";

    public static void main(String[] args) throws Exception {
        int i;
        in = new Scanner(System.in);
        // Define slot array
        slot = new Slot[Nslots];
        i=0;

        for(int r=0;r<Nrooms;r++){
            for(int h=0;h<Nhours;h++){
                for(int d=0;d<Ndays;d++){
                
                    slot[i++]=new Slot(d, h, r);
                }
            }
        }
        SlotShuffle();  // Shuffle the slots randomly



      
        System.out.println("Please enter the names of consultants.");
        String namestr_consultant=in.next( );
        System.out.println("Please enter the names of seniors doctors.");
        String namestr_senior=in.next( );
        System.out.println("Please enter the names of junior doctors.");
        String namestr_junior=in.next( );
        System.out.println("Who are the doctors who will do the Brain Surgery?");
        String namestr_brain=in.next( );
        System.out.println("Who are the doctors who needs a surgeryroom with an x-ray machine?");
        String namestr_xray=in.next( );
        System.out.println("Who are the doctors who needs a surgeryroom equipped with on-line streaming?");
        String namestr_online=in.next( );   
      
        // Split the strings to get the name arrays
        String[] names_consultant= namestr_consultant.split(",");
        String[] names_senior= namestr_senior.split(",");
        String[] names_junior= namestr_junior.split(",");
        String[] names_brain= namestr_brain.split(",");
        String[] names_xray= namestr_xray.split(",");
        String[] names_online= namestr_online.split(",");

        int Nconsultants=names_consultant.length;
        int Nseniors=names_senior.length;
        int Njuniors=names_junior.length;
        int Ndoctors=Nconsultants+Nseniors+Njuniors;

        // Define and populate the doctors array and their names and types
        doctor=new Doctor[Ndoctors+1]; // added 1 to allow empty slots

        i=0;
        for (String name : names_consultant) {
            doctor[i++]=new Doctor(name,Doctor.Types.CONSULTANT);            
        }

        for (String name : names_senior) {
            doctor[i++]=new Doctor(name,Doctor.Types.SENIOR);            
        }

        for (String name : names_junior) {
            doctor[i++]=new Doctor(name,Doctor.Types.JUNIOR);            
        }

        //doctor[i++]=new Doctor(VacantSlotName,Doctor.Types.SENIOR);  // Add two more doctors for empty slots
        doctor[i++]=new Doctor(VacantSlotName,Doctor.Types.JUNIOR);


        // Populate the brain/xray/online flags
        // In the following loops, we search for the doctors who's names match the brain/xray/online names
        for (String name : names_brain) {
            for (Doctor d : doctor) {
                if(d.name.equals(name)){
                    d.brain=true;
                }
            }
        }

        for (String name : names_xray) {
            for (Doctor d : doctor) {
                if(d.name.equals(name)){
                    d.xray=true;
                }
            }
        }

        for (String name : names_online) {
            for (Doctor d : doctor) {
                if(d.name.equals(name)){
                    d.online=true;
                }
            }
        }

        // Here we create an array of doctor tables.
        //The purpose is to have a differently ordered table of doctors.
        // This helps randomize the doctor selection for each recursion. 
        //The resulting array is shuffled_doctor[Nslots][doctor.length]
        shuffled_doctor= new Doctor[Nslots][doctor.length]; // create shuffled copies of the doctor table         
        for(int k=0;k<Nslots;k++){
            for(int j=0;j<doctor.length;j++){
                shuffled_doctor[k][j]=doctor[j];
            }
            DoctorShuffle(shuffled_doctor[k]);
        }

        counter=0;

        scheduleSearch(Nslots);
        System.out.println("\nConsultant & Senior doctor scheduling completed. No. of explored alternatives= "+counter);
        System.out.println("====================  Consultant & Senior Scedule =========================");
        schedulePrint(SrSchedule);

        scheduleSearchJunior(Nslots);
        System.out.println("\n\n\nJunior doctor scheduling completed. No. of explored alternatives= "+counter);
        System.out.println("====================        Junior  Scedule       =========================");
        schedulePrint(JrSchedule);
    }


    private static void SlotShuffle(){
        Random rand=new Random();
        int shuffle_iterations=slot.length*100;        

        for (int i=0;i<shuffle_iterations;i++) {
            int r1= rand.nextInt(slot.length  );
            int r2= rand.nextInt(slot.length  );
            Slot tempslot=slot[r1];
            slot[r1]=slot[r2];
            slot[r2]=tempslot;
        }
    }

    private static void DoctorShuffle(Doctor[] d){
        Random rand=new Random();
        int shuffle_iterations=d.length*10;        

        for (int i=0;i<shuffle_iterations;i++) {
            int r1= rand.nextInt(d.length  );
            int r2= rand.nextInt(d.length  );
            Doctor tempdoctor=d[r1];
            d[r1]=d[r2];
            d[r2]=tempdoctor;
        }
    }

    // Schedule search method: Initializes a list then starts the recursive search process
    public static void scheduleSearch(int nslots) {
        SearchDone=false;
        List<Doctor> schedule = new ArrayList<Doctor>();
        scheduleRecursiveSearch(nslots, schedule);
    }

    // Recursive search method
    private static void scheduleRecursiveSearch(int nslots, List<Doctor> schedule) {
        if(!SearchDone){
            counter++;

            if (nslots == 0) {
                if(CheckExistAtLeastOnce(schedule)){
                    //schedulePrint(schedule);
                    SearchDone=true;
                    SrSchedule= new ArrayList<>(schedule);
                }            
            } else {
                Doctor[] doctor_table=shuffled_doctor[nslots-1];
                for (Doctor doc : doctor_table) {

                    // We either exploring the consultant/senior schedule or the junior schedule. 
                    // The following skips the unnecessary doctors based on their types and the isjuniorsearch flag
                    if(doc.type == Doctor.Types.JUNIOR) continue;  

                    schedule.add(doc); // Add current elelement (doctor)
                    if(scheduleCheck(schedule)){
                        scheduleRecursiveSearch(nslots - 1, schedule); // Recursive call
                    }                
                    schedule.remove(schedule.size() - 1); // Remove last element
                }
            }
        }
    }

    private static Boolean scheduleCheck(List<Doctor> schedule){
        return CheckNoDuplicate(schedule) 
                && CheckMorningForConsulants(schedule)
                && CheckBrain(schedule)
                && CheckXray(schedule)
                && CheckOnline(schedule); 
    }


    // Check for illegal duplicates. A doctor cannot exist at the same time(hour/day) in different rooms.
    private static Boolean CheckNoDuplicate(List<Doctor> schedule){
        for (int i=0;i<schedule.size()-1;i++) {
            for (int j=i+1;j<schedule.size();j++) {
                if(    (slot[i].day  == slot[j].day) 
                    && (slot[i].hour == slot[j].hour)
                    && (schedule.get(i).name.equals(schedule.get(j).name))  
                    &&  !(schedule.get(i).name.equals(VacantSlotName))) // empty slots are not considered duplicates
                    {
                        return false;
                    }
            }
        }

        return true;
    }

    // Check for that consultants are only assigned morning slots
    private static Boolean CheckMorningForConsulants(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if( schedule.get(i).type == Doctor.Types.CONSULTANT  && !slot[i].morning){
                return false;
            }
        }
        return true;
    }

    // Check that brain doctors are assigned the appropriate slots on Monday and Wednesday.
    private static Boolean CheckBrain(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if( schedule.get(i).brain  && !slot[i].brain){
                return false;
            }
        }
        return true;
    }
    
    // Check that doctors who need x-ray are assigned the appropriate rooms.
    private static Boolean CheckXray(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if( schedule.get(i).xray  && !slot[i].xray){
                return false;
            }
        }
        return true;
    }

    // Check that doctors who need online are assigned the appropriate rooms.
    private static Boolean CheckOnline(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if( schedule.get(i).online  && !slot[i].online){
                return false;
            }
        }
        return true;
    }


    // Check that doctor appears at least once in the schedule.

    private static Boolean CheckExistAtLeastOnce(List<Doctor> schedule){
        for (Doctor doc : doctor) {
            if( doc.type == Doctor.Types.JUNIOR) {
                continue;  // skip juniors. In this method we only consider consultant and senior doctors
            }
            Boolean doc_exist=false;
            for (Doctor scheddoctor : schedule) {
                if(doc.name.equals(scheddoctor.name)){
                    doc_exist=true;
                    break;
                }
            }
            if(!doc_exist){
                return false;
            }
        }
        return true;
    }

    private static void schedulePrint(List<Doctor> schedule){
        int i=0;
        
        int[] slotmap=new int[slot.length];        

        // Printing requires specific ordering. The following finds the required mapping array (slotmap)
        for(int r=0;r<Nrooms;r++){
            for(int h=0;h<Nhours;h++){
                for(int d=0;d<Ndays;d++){        
                    for (int j=0;j<slot.length;j++) {
                        if((slot[j].room == r ) &&(slot[j].hour==h)&&(slot[j].day==d)){
                            slotmap[i++]=j;
                        }
                    }
                }
            }
        }

        i=0;
        for(int r=0;r<Nrooms;r++){
            System.out.println("\n======================================================");
            System.out.printf(" Room: %d \n",r+1); // index 0 corresponds to Room#1 in the problem. Therefore we need to add 1 in printing
            System.out.print("        Sun            Mon           Tue           Wed          Thu ");
            for(int h=0;h<Nhours;h++){
                System.out.printf("\n%s   ",HoursName[h]);
                for(int d=0;d<Ndays;d++){
                    System.out.print(schedule.get(slotmap[i++]).name+"          ");
                }
            }
        }
    }


    //===================================================================================
    //==  The following are methods for the "Junior" scheduling problem
    //==  We could have integrated it with the above functions but we prefare to write 
    //==  it seperately for improved readability and handling of its special constraints.
    //===================================================================================

    // Schedule search method. Initializes a list then starts the recursive search process (Junior version)
    public static void scheduleSearchJunior(int nslots) {
        List<Doctor> schedule = new ArrayList<Doctor>();
        SearchDone=false;
        scheduleRecursiveSearchJunior(nslots, schedule);
    }
    // Recursive search method (Junior version)
    private static void scheduleRecursiveSearchJunior(int nslots, List<Doctor> schedule) {
        if(!SearchDone){
            if (nslots == 0) {
                if(CheckJuniorExistAtLeastOnce(schedule)){
                    //schedulePrint(schedule);
                    SearchDone=true;
                    JrSchedule= new ArrayList<>(schedule);
                }            
            } else {
                for (Doctor doc : doctor) {

                    // We either exploring the consultant/senior schedule or the junior schedule. 
                    // The following skips the unnecessary doctors based on their types and the isjuniorsearch flag
                    if(  ( doc.type != Doctor.Types.JUNIOR)) continue;  

                    schedule.add(doc); // Add current elelement (doctor)
                    if(scheduleCheckJunior(schedule)){
                        scheduleRecursiveSearchJunior(nslots - 1, schedule); // Recursive call
                    }                
                    schedule.remove(schedule.size() - 1); // Remove last element
                }
            }
        }
    }

    // schedule validity check (Junior version)
    private static Boolean scheduleCheckJunior(List<Doctor> schedule){
        return CheckNoDuplicate(schedule) 
                && CheckBrain(schedule)
                && CheckXray(schedule)
                && CheckOnline(schedule)
                && CheckRoom3Junior(schedule)  // Room3 is not big enough for juniors
                && CheckSeniorWithEveryJunior(schedule);
    }


    // Check that no junior doctor is assigned to Room#3 becuase it is too small
    private static Boolean CheckRoom3Junior(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if((!schedule.get(i).name.equals(VacantSlotName)) && slot[i].room==3-1 ){
                return false;
            }
        }
        return true;
    }

    // Check that there is a senior with each junior
    private static Boolean CheckSeniorWithEveryJunior(List<Doctor> schedule){
        for (int i=0;i<schedule.size();i++) {
            if((!schedule.get(i).name.equals(VacantSlotName)) ){
                if(schedule.get(i).name.equals(VacantSlotName)){ 
                    return false;
                }
            }
        }
        return true;
    }

        // Check that doctor appears at least once in the schedule.
    
    private static Boolean CheckJuniorExistAtLeastOnce(List<Doctor> schedule){
        for (Doctor doc : doctor) {
            if( doc.type != Doctor.Types.JUNIOR) {
                continue;  // skip juniors. In this method we only consider consultant and senior doctors
            }
            Boolean doc_exist=false;
            for (Doctor scheddoctor : schedule) {
                if(doc.name.equals(scheddoctor.name)){
                    doc_exist=true;
                    break;
                }
            }
            if(!doc_exist){
                return false;
            }
        }
        return true;
    }

}
