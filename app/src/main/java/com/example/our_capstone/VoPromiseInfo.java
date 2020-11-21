package com.example.our_capstone;

import java.util.ArrayList;
import java.util.Date;

public class VoPromiseInfo {
   private Date date;
   private String location;
   private ArrayList attender = new ArrayList();
   private ArrayList late_comer = new ArrayList();

   VoPromiseInfo(Date date,String location,ArrayList attender,ArrayList late_comer){
       this.date=date;
       this.location=location;
       this.attender=attender;
       this.late_comer=late_comer;
   }
   protected void set_date(Date date){ this.date=date;}
   protected void set_location(String location){this.location=location;}
   protected void set_attender(ArrayList attender){this.attender=attender;}
   protected void set_late_comer(ArrayList late_comer){ this.late_comer=late_comer;}
   protected Date get_Date(){return this.date;}
   protected String get_location(){return this.location;}
   protected ArrayList get_attender(){return this.attender;}
   protected ArrayList get_late_comer(){return this.late_comer;}
}
