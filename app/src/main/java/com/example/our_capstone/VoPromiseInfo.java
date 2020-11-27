package com.example.our_capstone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VoPromiseInfo implements Serializable {
   private Calendar date_time;
   private String location;
   private Double lat,lon;
   private ArrayList attender;
   private ArrayList late_comer = new ArrayList();

    VoPromiseInfo(){
        date_time=Calendar.getInstance();
        attender = new ArrayList<Member>();
        late_comer = new ArrayList();
    }
   VoPromiseInfo(Calendar date_time,String location,Double lon,Double lat,ArrayList attender,ArrayList late_comer){
       this.date_time=date_time;
       this.location=location;
       this.lon=lon;
       this.lat=lat;
       this.attender=attender;
       this.late_comer=late_comer;
   }
   List<Member> VO_mem(){return new ArrayList<Member>();} //방 전체 멤버리스트
   protected void set_date_time(Calendar date_time){this.date_time=date_time;}
   protected void set_location_info(Double lat,Double lon){this.lat=lat;this.lon=lon;}
   protected void set_location(String location){this.location=location;}
   protected void set_attender(ArrayList attender){this.attender=attender;}
   protected void set_late_comer(ArrayList late_comer){ this.late_comer=late_comer;}
   protected Calendar get_date_time(){return this.date_time;}
   protected String get_location(){return this.location;}
   protected Double get_lat(){return this.lat;}
   protected Double get_lon(){return this.lon;}
   protected ArrayList get_attender(){return this.attender;}
   protected ArrayList get_late_comer(){return this.late_comer;}


    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() { return "날짜: "+this.date_time.toString()+"장소:"+location;}

    static class Member{
        String name;
        String birth;
        boolean isSelected;
        Member(){
        name="";
        birth="";
        isSelected=false;
        }
       Member(String name,String birth){
           this.name=name;
           this.birth=birth;
           this.isSelected=false;
       }
       public void set_name(String name){
            this.name=name;
       }
       public void set_birth(String birth){
           this.birth=birth;
       }
       public void set_Selected(boolean is_Selected){this.isSelected=is_Selected;}
       public String get_name(){return this.name;}
       public String get_birth(){return this.birth;}
       public boolean isSelected() {
            return this.isSelected;
       }
   }
}
