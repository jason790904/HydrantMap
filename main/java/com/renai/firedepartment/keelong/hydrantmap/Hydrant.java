package com.renai.firedepartment.keelong.hydrantmap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by b7918101 on 2017/8/10.
 */

public class Hydrant {

    private int _Id;
    private ArrayList<Integer> mStates;
    //private String mStates;
    private int mType = -1;
    private boolean hasMark = false;
    private String mMark_States = MARK_STATES.NULL;
    private String mPress = null;
    private int mDist = -1;
    private String mVil = null;
    private String mAddress = null;
    private LatLng mLatlng = null ;
    private String mPs = null;
    private DecimalFormat df;
    private int mTrafficLevel = 0;


    public Hydrant(int id){

        df = new DecimalFormat("#.00");

        mStates = new ArrayList<>();
        _Id = id;

        mPs = "NONE";
    }

    public Hydrant(List<Object> row){
        mStates = new ArrayList<>();
        df = new DecimalFormat("#.00");

        _Id = Integer.parseInt(row.get(0).toString());
        String s[] = row.get(1).toString().split(",");
        for(String ss:s){
            mStates.add(Integer.valueOf(ss));
        }
        //mStates = row.get(1).toString();
            mType = Integer.parseInt(row.get(2).toString());
            if (Integer.parseInt(row.get(3).toString()) == 1)
                hasMark = true;
            else
                hasMark = false;
            mMark_States = hasMark? row.get(4).toString():MARK_STATES.NULL;
            mPress = df.format(Double.valueOf(row.get(5).toString()));
            mDist = Integer.valueOf(row.get(6).toString());
        String vil = row.get(7).toString();
        vil = (vil.charAt(vil.length()-1)=='里')?vil:(vil+"里");
            mVil = vil;
            mAddress = row.get(8).toString();
            mLatlng = new LatLng(Double.parseDouble(row.get(9).toString()), Double.parseDouble(row.get(10).toString()));
            mPs = row.get(11).toString();
            mTrafficLevel = Integer.valueOf(row.get(12).toString());

    }
    public Hydrant(Cursor cs){
        mStates = new ArrayList<>();
        df = new DecimalFormat("#.00");
        _Id = Integer.parseInt(cs.getString(0));
        String s[] =cs.getString(1).split(",");
        for(String ss:s){
            mStates.add(Integer.valueOf(ss));
        }
        mType = cs.getInt(2);
        if (cs.getInt(3) == 1)
            hasMark = true;
        else
            hasMark = false;
        mMark_States = hasMark?cs.getString(4):MARK_STATES.NULL;
        mPress = df.format(Double.valueOf(cs.getString(5)));
        mDist = cs.getInt(6);
        String vil = cs.getString(7);
        vil = (vil.charAt(vil.length()-1)=='里')?vil:(vil+"里");
        mVil = vil;
        mAddress = cs.getString(8);
        mLatlng = new LatLng(cs.getDouble(9), cs.getDouble(10));
        mPs = cs.getString(11);
        mTrafficLevel = cs.getInt(12);

    }

    public List<Object> parseHydrantToSheetRow(){
        
        List<Object> row = new ArrayList<>();
        row.add(String.valueOf(_Id));
        String ss = mStates.get(0).toString();
        int i = 1;
        while (i < mStates.size()){
            ss+=","+mStates.get(i);
            i++;
        }
        row.add(ss);
        row.add(String.valueOf(mType));
        row.add(hasMark? "1":"0");
        row.add(mMark_States);
        row.add(mPress);
        row.add(mDist);
        row.add(mVil);
        row.add(mAddress);
        row.add(String.valueOf(mLatlng.latitude));
        row.add(String.valueOf(mLatlng.longitude));
        row.add(mPs);
        row.add(mTrafficLevel);

        return row;
    }


    public void set_Id(int id){
        _Id =id;
    }

    public int getId(){
        return _Id;
    }


    public void  setStates(ArrayList<Integer> states){
        mStates = states;
    }
    public void addStates(int state){
        if (mStates==null)
            mStates = new ArrayList<>();
        mStates.add(state);
    }
    public void removeStates(int state){
        if(mStates != null && mStates.contains(state)){
            mStates.remove((Object)state);
        }
    }

    public ArrayList<Integer> getStates(){
        return mStates;
    }

    public String getStatesForString(){
        String ss = mStates.get(0).toString();
        int i = 1;
        while (i < mStates.size()){
            ss+=","+mStates.get(i);
            i++;
        }
        return ss;
    }
    public static ArrayList<Integer> getStatesOf(String statesString){
        String s[] =statesString.split(",");
        ArrayList<Integer> result = new ArrayList<>();
        for(String ss:s){
            result.add(Integer.valueOf(ss));
        }
        return result;
    }

    public void setPs(String ps){
        mPs = ps;
    }

    public String getPs(){
        return mPs;
    }

    public void setType(int type){
        mType = type;
    }

    public int getType(){
       return mType;
    }

    public void setMarkExist(boolean isMarkHas){
        hasMark = isMarkHas;
    }

    public boolean hasMark(){
      return hasMark;
    }

    public void setMark_States(String mark_states){
        mMark_States = mark_states;
    }

    public String getMark_States( ){
          return mMark_States  ;
    }

    public void setPress(String press){
        mPress = df.format(Double.valueOf(press));
    }

     public String getPress( ){
         return mPress  ;
     }

    public void setDist(int dist){
        mDist = dist;
    }

    public int getDist() {
        return mDist;
    }
    public void setVil(String vil){mVil = vil.charAt(vil.length()-1)=='里'?vil:(vil+"里");}
    public String getVil(){return mVil;}

    public  void setAddress(String address){
        mAddress = address;
    }

    public  String getAddress( ){
          return mAddress  ;
    }

    public  void setLatlng(double latitude, double longitude){
          mLatlng = new LatLng(latitude,longitude);
    }

    public  void setLatlng(LatLng newPosition){
        mLatlng = newPosition;
    }

    public  LatLng getLatlng(){
        return mLatlng ;
    }

    public int  getDutyCode(){
        if(_Id>0){
            return _Id/1000;
        }else
            return -1;
    }

    public void setTrafficLevel(int newLevel){
        mTrafficLevel = newLevel;
    }

    public int getTrafficLevel(){
        return mTrafficLevel;
    }

    public static class ATTRIBUTE{
        public static final int ID = 0;
        public static final int STATES = 1;
        public static final int TYPE = 2;
        public static final int MARK_SET = 3;
        public static final int MARK_STATE = 4;
        public static final int PRESS = 5;
        public static final int ADDRESS = 6;
        public static final int POSITION = 7;
        public static final int PS = 8;
        public static final int TRAFFIC_LEVEL = 9;

    }

    public ArrayList<Integer> findDiffrent(Hydrant anotherHydrant){
        ArrayList<Integer> result = new ArrayList<>();

        if(mStates.size() == anotherHydrant.getStates().size()){
            for(int s :anotherHydrant.getStates()){
                if(!mStates.contains(s)){
                    result.add(ATTRIBUTE.STATES);
                    break;
                }
            }
        }else
            result.add(ATTRIBUTE.STATES);
        if(mType != anotherHydrant.getType())
            result.add(ATTRIBUTE.TYPE);
        if(hasMark !=anotherHydrant.hasMark())
            result.add(ATTRIBUTE.MARK_SET);
        if(mMark_States.compareTo(anotherHydrant.getMark_States())!=0)
            result.add(ATTRIBUTE.MARK_STATE);
        if(mPress.compareTo(anotherHydrant.getPress())!=0)
            result.add(ATTRIBUTE.PRESS);

        if(mDist !=anotherHydrant.getDist())
            result.add(ATTRIBUTE.ADDRESS);
        else if(mVil.compareTo(anotherHydrant.getVil())!=0)
            result.add(ATTRIBUTE.ADDRESS);
        else if(mAddress.compareTo(anotherHydrant.getAddress())!=0)
            result.add(ATTRIBUTE.ADDRESS);

        if(mLatlng.latitude != anotherHydrant.getLatlng().latitude)
            result.add(ATTRIBUTE.POSITION);
        else if(mLatlng.longitude != anotherHydrant.getLatlng().longitude)
            result.add(ATTRIBUTE.POSITION);

        if(mPs.compareTo(anotherHydrant.getPs())!=0)
            result.add(ATTRIBUTE.PS);
        if(mTrafficLevel!=anotherHydrant.getTrafficLevel())
            result.add(ATTRIBUTE.TRAFFIC_LEVEL);

        return result;
    }

    public int getUsableState(){
        int[] warning = {
                STATES.DIRT,
                STATES.NEED_RISE,
                STATES.ETC,
        };
        int[] broken= {
                STATES.COVER_BROKEN,
                STATES.BURY,
                STATES.NO_WATER,
                STATES.RING_BROKEN,
                STATES.STRIPPED_THREAD,
                STATES.SWITCH_BROKEN,
                STATES.TUBE_CANT_SET
        };

        if(mStates.contains(STATES.PRESS_LESS)||mStates.contains(STATES.REMOVED)){
            return USABLE_STATE.PRESS_LESS;
        }

        for(int state:broken){
            if(mStates.contains(state))
                return USABLE_STATE.BROKEN;
        }

        for(int state:warning){
            if(mStates.contains(state))
                return USABLE_STATE.WARNING;
        }

        return USABLE_STATE.GOOD;
    }

    public Bitmap getIconBitmap(Context context){
        Bitmap bd;
        if(mType == TYPE.UNDER){
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapFromVector(context,R.drawable.ic_under_good);
                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapFromVector(context,R.drawable.ic_under_warning);
                    break;
                default:
                    bd = bitmapFromVector(context,R.drawable.ic_under_broken);
                    break;

            }
        }else if(mType == TYPE.GROUND){
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapFromVector(context,R.drawable.ic_ground_good);
                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapFromVector(context,R.drawable.ic_ground_warning);

                    break;
                default:
                    bd = bitmapFromVector(context,R.drawable.ic_ground_broken);

                    break;

            }
        }else{
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapFromVector(context,R.drawable.ic_antislip_good);

                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapFromVector(context,R.drawable.ic_antislip_warning);

                    break;
                default:
                    bd = bitmapFromVector(context,R.drawable.ic_antislip_broken);

                    break;

            }
        }
        return bd;
    }

    public BitmapDescriptor getBitmapDescriptor(Context context){
        BitmapDescriptor bd;
        if(mType == TYPE.UNDER){
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_under_good);
                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_under_warning);
                    break;
                case USABLE_STATE.PRESS_LESS:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_under_press_less);
                    break;
                default:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_under_broken);
                    break;

            }
        }else if(mType == TYPE.GROUND){
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_ground_good);
                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_ground_warning);

                    break;
                case USABLE_STATE.PRESS_LESS:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_ground_press_less);
                    break;
                default:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_ground_broken);

                    break;

            }
        }else{
            switch (getUsableState()){
                case USABLE_STATE.GOOD:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_antislip_good);

                    break;
                case USABLE_STATE.WARNING:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_antislip_warning);

                    break;
                case USABLE_STATE.PRESS_LESS:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_antislip_press_less);
                    break;
                default:
                    bd = bitmapDescriptorFromVector(context,R.drawable.ic_antislip_broken);

                    break;

            }
        }
        return bd;
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static Bitmap bitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public String pharseStatesToString(){
        String resuilt="";
        if(mStates.size()>0){
            resuilt = getChineseState(mStates.get(0));

            for(int i = 1;i<mStates.size();i++)
                resuilt += ","+getChineseState(mStates.get(i));

        }

        return resuilt;
    }

    public static String getChineseState(int stateCode){
        String resuilt = "";
        switch (stateCode){
            case  STATES.GOOD:
                resuilt = "良好";
                break;
            case  STATES.DIRT :
                resuilt = "除汙";
                break;
            case  STATES.PRESS_LESS :
                resuilt = "水壓不足";
                break;
            case  STATES.BURY:
                resuilt = "埋沒";
                break;
            case  STATES.NO_WATER:
                resuilt = "無水";
                break;
            case  STATES.NEED_RISE :
                resuilt = "提昇";
                break;
            case  STATES.STRIPPED_THREAD:
                resuilt = "滑牙";
                break;
            case  STATES.ETC:
                resuilt = "其他";
                break;
            case  STATES.COVER_BROKEN :
                resuilt = "箱蓋卡死";
                break;
            case  STATES.TUBE_CANT_SET:
                resuilt = "立管無法置入" ;
                break;
            case  STATES.RING_BROKEN :
                resuilt = "接環毀損";
                break;
            case  STATES.SWITCH_BROKEN :
                resuilt = "開關毀損";
                break;
            case STATES.REMOVED:
                resuilt = "已移除";
                break;

        }
        return resuilt;
    }

    public static String getChineseType(int typeCode){
        switch (typeCode){
            case TYPE.ANTISLIP:
                return "新式開關";
            case TYPE.GROUND:
                return "地上式";
            case TYPE.UNDER:
                return "地下式";
            default:
                return null;
        }
    }

    public static String getChineseDist(int distCode){
        String resuilt = "";
        switch (distCode){
            case  DIST.AN_LE:
                resuilt = "安樂區";
                break;
            case  DIST.NUAN_NUAN:
                resuilt = "暖暖區";
                break;
            case  DIST.QI_DU:
                resuilt = "七堵區";
                break;
            case  DIST.REN_AI :
                resuilt = "仁愛區";
                break;
            case  DIST.XIN_YI :
                resuilt = "信義區";
                break;
            case  DIST.ZHONG_SHAN:
                resuilt = "中山區" ;
                break;
            case  DIST.ZHONG_ZHENG :
                resuilt = "中正區";
                break;

        }
        return resuilt;
    }


    public static class TYPE{
        public static final int GROUND=0;
        public static final int UNDER=1;
        public static final int ANTISLIP=2;
        public static List<Integer> getAllType(){
            ArrayList<Integer> resuilt =  new ArrayList<Integer>();
            resuilt.add(GROUND);
            resuilt.add(UNDER);
            resuilt.add(ANTISLIP);
            return resuilt;
        }
    }

    public static class MARK_STATES{
        public static final String GOOD="良好";
        public static final String NOT_FOUND="未發現";
        public static final String POSITION_INCORRECT = "位置不正確";
        public static final String NULL = "無";
        public static final String BROKEN = "斷裂";
        public static final String ETC = "其他";

        public static List<String> getAllStates(){
            ArrayList<String> resuilt = new ArrayList<>();
            resuilt.add(GOOD);
            resuilt.add(NOT_FOUND);
            resuilt.add(POSITION_INCORRECT);
            resuilt.add(BROKEN);

            //this  below must be the last
            resuilt.add(ETC);
            return resuilt;
        }
    }

    public static class STATES{
        public static final int GOOD = 0;
        public static final int DIRT = 1;
        public static final int PRESS_LESS = 2;
        public static final int BURY = 3;
        public static final int NO_WATER = 4;
        public static final int NEED_RISE = 5;
        public static final int STRIPPED_THREAD = 6;
        public static final int COVER_BROKEN = 7;
        public static final int TUBE_CANT_SET = 8;
        public static final int RING_BROKEN = 9;
        public static final int SWITCH_BROKEN = 10;
        public static final int REMOVED = -1;
        public static final int ETC = 100;
        public static List<Integer> getAllStates(){
            List<Integer> resuilt = new ArrayList<>();
            resuilt.add(GOOD);
            resuilt.add(DIRT);
            resuilt.add(PRESS_LESS);
            resuilt.add(BURY);
            resuilt.add(NO_WATER);
            resuilt.add(NEED_RISE);
            resuilt.add(STRIPPED_THREAD);
            resuilt.add(COVER_BROKEN);
            resuilt.add(TUBE_CANT_SET);
            resuilt.add(RING_BROKEN);
            resuilt.add(SWITCH_BROKEN);
            resuilt.add(ETC);
            resuilt.add(REMOVED);

            return resuilt;
        }
    }

    public static class USABLE_STATE{
        public static final int GOOD = 0;
        public static final int WARNING = -1;
        public static final int BROKEN = -2;
        public static final int PRESS_LESS = -3;
        public static final int REMOVED = -4;
    }

    public static class DIST{
        public static final int REN_AI =1;
        public static final int AN_LE=7;
        public static final int ZHONG_ZHENG=9;
        public static final int ZHONG_SHAN=4;
        public static final int QI_DU=6;
        public static final int XIN_YI=5;
        public static final int NUAN_NUAN=8;
        public static List<Integer> getAllDist(){
            List<Integer> result = new ArrayList<>();
            result.add(REN_AI);
            result.add(AN_LE);
            result.add(ZHONG_ZHENG);
            result.add(ZHONG_SHAN);
            result.add(QI_DU);
            result.add(XIN_YI);
            result.add(NUAN_NUAN);

            return result;
        }

    }

    public static class DUTY{
        public static final int CODE_RENAI = 1;
        public static final int CODE_XINER = 2;
        public static final int CODE_BAIFU = 3;
        public static final int CODE_ZONGSHAN = 4;
        public static final int CODE_XINYI = 5;
        public static final int CODE_QIDU = 6;
        public static final int CODE_ANLE = 7;
        public static final int CODE_NAUNNAUN = 8;
        public static final int CODE_ZONGZHENG = 9;
        public static ArrayList<Integer> getAllDuty(){
            ArrayList<Integer> result = new ArrayList<>();
            result.add(CODE_RENAI);
            result.add(CODE_XINER);
            result.add(CODE_BAIFU);
            result.add(CODE_ZONGSHAN);
            result.add(CODE_XINYI);
            result.add(CODE_QIDU);
            result.add(CODE_ANLE);
            result.add(CODE_NAUNNAUN);
            result.add(CODE_ZONGZHENG);
            return result;
        }
        public static String getDutyName(int dutyCode){
            switch (dutyCode){
                case CODE_RENAI:
                    return "仁愛分隊";
                case CODE_XINER:
                    return "信二分隊";
                case CODE_BAIFU:
                    return "百福分隊";
                case CODE_XINYI:
                    return "信義分隊";
                case CODE_QIDU:
                    return "七堵分隊";
                case CODE_ANLE:
                    return "安樂分隊";
                case CODE_NAUNNAUN:
                    return "暖暖分隊";
                case CODE_ZONGZHENG:
                    return "中正分隊";
                case CODE_ZONGSHAN:
                    return "中山分隊";

            }
            return null;
        }




    }

    public static class VIL{
        public static List<String> getVil(int Dist){
            List<String> resuilt =new ArrayList<>();
            switch (Dist){
                case  DIST.AN_LE:
                    resuilt.add("武崙里");
                    resuilt.add("定國里");
                    resuilt.add("定邦里");
                    resuilt.add("五福里");
                    resuilt.add("興寮里");
                    resuilt.add("永康里");
                    resuilt.add("七賢里");
                    resuilt.add("慈仁里");
                    resuilt.add("外寮里");
                    resuilt.add("新西里");
                    resuilt.add("干城里");
                    resuilt.add("新崙里");
                    resuilt.add("內寮里");
                    resuilt.add("鶯安里");
                    resuilt.add("樂一里");
                    resuilt.add("四維里");
                    resuilt.add("西川里");
                    resuilt.add("嘉仁里");
                    resuilt.add("安和里");
                    resuilt.add("長樂里");
                    resuilt.add("鶯歌里");
                    resuilt.add("壯觀里");
                    resuilt.add("中崙里");
                    resuilt.add("三民里");
                    resuilt.add("六合里");
                    break;
                case  DIST.NUAN_NUAN:
                    resuilt.add("碇和里");
                    resuilt.add("八堵里");
                    resuilt.add("八南里");
                    resuilt.add("暖西里");
                    resuilt.add("暖暖里");
                    resuilt.add("碇祥里");
                    resuilt.add("八中里");
                    resuilt.add("暖東里");
                    resuilt.add("碇安里");
                    resuilt.add("碇內里");
                    resuilt.add("八西里");
                    resuilt.add("暖同里");
                    resuilt.add("過港里");
                    break;
                case  DIST.QI_DU:
                    resuilt.add("百福里");
                    resuilt.add("永安里");
                    resuilt.add("瑪西里");
                    resuilt.add("長安里");
                    resuilt.add("正光里");
                    resuilt.add("永平里");
                    resuilt.add("堵南里");
                    resuilt.add("正明里");
                    resuilt.add("友二里");
                    resuilt.add("友一里");
                    resuilt.add("堵北里");
                    resuilt.add("瑪南里");
                    resuilt.add("長興里");
                    resuilt.add("自強里");
                    resuilt.add("泰安里");
                    resuilt.add("實踐里");
                    resuilt.add("富民里");
                    resuilt.add("六堵里");
                    resuilt.add("八德里");
                    resuilt.add("瑪東里");
                    break;
                case  DIST.REN_AI :
                    resuilt.add("書院里");
                    resuilt.add("明德里");
                    resuilt.add("朝棟里");
                    resuilt.add("玉田里");
                    resuilt.add("花崗里");
                    resuilt.add("光華里");
                    resuilt.add("獅球里");
                    resuilt.add("水錦里");
                    resuilt.add("曲水里");
                    resuilt.add("博愛里");
                    resuilt.add("林泉里");
                    resuilt.add("誠仁里");
                    resuilt.add("和明里");
                    resuilt.add("兆連里");
                    resuilt.add("同風里");
                    resuilt.add("英仁里");
                    resuilt.add("崇文里");
                    resuilt.add("文安里");
                    resuilt.add("文昌里");
                    resuilt.add("智仁里");
                    resuilt.add("福仁里");
                    resuilt.add("德厚里");
                    resuilt.add("仁德里");
                    resuilt.add("新店里");
                    resuilt.add("吉仁里");
                    resuilt.add("虹橋里");
                    resuilt.add("龍門里");
                    resuilt.add("育仁里");
                    resuilt.add("忠勇里");
                    break;
                case  DIST.XIN_YI :
                    resuilt.add("孝岡里");
                    resuilt.add("仁義里");
                    resuilt.add("孝賢里");
                    resuilt.add("東光里");
                    resuilt.add("仁壽里");
                    resuilt.add("義和里");
                    resuilt.add("信綠里");
                    resuilt.add("孝深里");
                    resuilt.add("智慧里");
                    resuilt.add("東信里");
                    resuilt.add("義幸里");
                    resuilt.add("東安里");
                    resuilt.add("禮儀里");
                    resuilt.add("禮東里");
                    resuilt.add("智誠里");
                    resuilt.add("東明里");
                    resuilt.add("孝忠里");
                    resuilt.add("孝德里");
                    resuilt.add("義昭里");
                    resuilt.add("義民里");
                    break;
                case  DIST.ZHONG_SHAN:
                    resuilt.add("和慶里");
                    resuilt.add("太白里");
                    resuilt.add("西榮里");
                    resuilt.add("新建里");
                    resuilt.add("仙洞里");
                    resuilt.add("仁正里");
                    resuilt.add("通明里");
                    resuilt.add("德安里");
                    resuilt.add("居仁里");
                    resuilt.add("文化里");
                    resuilt.add("西康里");
                    resuilt.add("西定里");
                    resuilt.add("民治里");
                    resuilt.add("通化里");
                    resuilt.add("安平里");
                    resuilt.add("和平里");
                    resuilt.add("安民里");
                    resuilt.add("健民里");
                    resuilt.add("中山里");
                    resuilt.add("德和里");
                    resuilt.add("西華里");
                    resuilt.add("中興里");
                    resuilt.add("中和里");
                    resuilt.add("協和里");
                    break;
                case  DIST.ZHONG_ZHENG :
                    resuilt.add("碧砂里");
                    resuilt.add("正砂里");
                    resuilt.add("新富里");
                    resuilt.add("社寮里");
                    resuilt.add("平寮里");
                    resuilt.add("中濱里");
                    resuilt.add("真砂里");
                    resuilt.add("建國里");
                    resuilt.add("八斗里");
                    resuilt.add("入船里");
                    resuilt.add("中正里");
                    resuilt.add("正濱里");
                    resuilt.add("信義里");
                    resuilt.add("長潭里");
                    resuilt.add("中船里");
                    resuilt.add("義重里");
                    resuilt.add("海濱里");
                    resuilt.add("和憲里");
                    resuilt.add("正義里");
                    resuilt.add("港通里");
                    resuilt.add("砂灣里");
                    resuilt.add("德義里");
                    resuilt.add("正船里");
                    resuilt.add("新豐里");
                    resuilt.add("砂子里");
                    resuilt.add("中砂里");
                    break;
                default:
                    resuilt.add("無");
                    break;
            }
            return resuilt;

        }
    }

    public static class TRAFFIC{
        public static ArrayList<String> LevelName(){
            ArrayList<String> result = new ArrayList<>();
            result.add("");
            result.add("移動式泵浦");
            result.add("小型水泵車");
            result.add("水箱車");
            result.add("水庫車");
            return result;
        }
    }

}
