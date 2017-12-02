package com.renai.firedepartment.keelong.hydrantmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by b7918101 on 2017/7/16.
 */

public class MyDBHelper extends SQLiteOpenHelper {

    // 資料庫名稱
    public static final String DATABASE_NAME = "hydrant.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION =2;
    // 資料庫物件，固定的欄位變數
    public static final String TABLE_HYDRANT = "hydrant";
    public static final String TABLE_LOG = "log";
    public static final String TABLE_TEMP="tmpHydrant";
    public static final String TABLE_ACCOUNT_NAME="accountName";
    public static final String COLUMN_NAME = "name";
    public static  final String COLUMN_ID = "_id";
    public static  final String COLUMN_STATE = "state";
    public static  final String COLUMN_TYPE ="type";
    public static  final String COLUMN_MARKED="marked";
    public static  final String COLUMN_MARKED_STATE="marked_state";
    public static  final String COLUMN_PRESS = "press";
    public static  final String COLUMN_ADDRESS = "address";
    public static  final String COLUMN_DIST = "dist";
    public static  final String COLUMN_VIL = "vil";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE="latitude";
    public static  final String COLUMN_PS = "ps";
    public static final String COLUMN_DATA_VERSION="dataversion";
    public static final String COLUMN_CHANGE_LOG = "changelog";
    public static final String COLUMN_LOG_DATE = "log_date";
    public static final String COLUMN_LOG_READ = "log_read";
    public static final String COLUMN_TRAFFIC_LEVEL = "traffic_level";




    public static final  String ASC = " ASC";
    public static final String DESC =" DESC";

    public static  final int COLUMN_INDEX_ID = 0;
    public static  final int COLUMN_INDEX_STATE = 1;
    public static  final int COLUMN_INDEX_TYPE =2;
    public static  final int COLUMN_INDEX_MARKED=3;
    public static  final int COLUMN_INDEX_MARKED_STATE=4;
    public static  final int COLUMN_INDEX_PRESS =5;
    public static  final int COLUMN_INDEX_DIST = 6;
    public static  final int COLUMN_INDEX_VIL = 7;
    public static  final int COLUMN_INDEX_ADDRESS =8;
    public static final int COLUMN_INDEX_LATITUDE=9;
    public static final int COLUMN_INDEX_LONGITUDE =10;
    public static  final int COLUMN_INDEX_PS =11;
    public static  final int COLUMN_INDEX_TRAFFIC_LEVEL =12;

    public static final String LOG_NONE_TO_UPLOAD = "NONE";


    public MyDBHelper(Context context){

        super(context,DATABASE_NAME,null,VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE  TABLE "+TABLE_HYDRANT+
                "  (" +
                COLUMN_ID+" INTEGER PRIMARY KEY  NOT NULL , " +
                COLUMN_STATE+"  , "+
                COLUMN_TYPE +"  , " +
                COLUMN_MARKED + " INTEGER , "+
                COLUMN_MARKED_STATE + "  , "+
                COLUMN_PRESS + " NUMERIC   , "+
                COLUMN_DIST + "  , "+
                COLUMN_VIL + "  , "+
                COLUMN_ADDRESS + "  , "+
                COLUMN_LATITUDE + " , "+
                COLUMN_LONGITUDE + "  , "+
                COLUMN_PS +" , "+
                COLUMN_TRAFFIC_LEVEL +" INTEGER "+
                ")");

        db.execSQL("CREATE  TABLE "+TABLE_TEMP+
                "  (" +
                COLUMN_ID+" INTEGER PRIMARY KEY  NOT NULL , " +
                COLUMN_STATE+"  , "+
                COLUMN_TYPE +"  , " +
                COLUMN_MARKED + " INTEGER , "+
                COLUMN_MARKED_STATE + "  , "+
                COLUMN_PRESS + " NUMERIC   , "+
                COLUMN_DIST + "  , "+
                COLUMN_VIL + "  , "+
                COLUMN_ADDRESS + "  , "+
                COLUMN_LATITUDE + " , "+
                COLUMN_LONGITUDE + "  , "+
                COLUMN_PS +" , "+
                COLUMN_TRAFFIC_LEVEL +" INTEGER "+
                ")");


        db.execSQL("CREATE  TABLE "+TABLE_LOG+
                "  ( _id INTEGER PRIMARY KEY NOT NULL , "
                + COLUMN_DATA_VERSION + " , " +
                COLUMN_CHANGE_LOG +" , "+
                COLUMN_LOG_DATE + " , "+
                COLUMN_LOG_READ +
                " INTEGER DEFAULT 0 )");
        ContentValues cv =new ContentValues();
        cv.put("_id",0);
        cv.put(COLUMN_DATA_VERSION,0);
        cv.put(COLUMN_CHANGE_LOG,"NONE");
        cv.put(COLUMN_LOG_DATE,System.currentTimeMillis());
        cv.put(COLUMN_LOG_READ,1);
        db.insert(TABLE_LOG,null,cv);

        db.execSQL("CREATE  TABLE IF NOT EXISTS "+TABLE_ACCOUNT_NAME+
                "  ( "+COLUMN_ID+" INTEGER PRIMARY KEY NOT NULL , "
                + COLUMN_NAME + ")");
    }


    public static void insertHydrant(SQLiteDatabase db,Hydrant hydrant){
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COLUMN_ID,hydrant.getId());
        cv.put(MyDBHelper.COLUMN_ADDRESS,hydrant.getAddress());
        cv.put(MyDBHelper.COLUMN_DIST,hydrant.getDist());
        cv.put(MyDBHelper.COLUMN_VIL,hydrant.getVil());
        cv.put(MyDBHelper.COLUMN_LATITUDE,hydrant.getLatlng().latitude);
        cv.put(MyDBHelper.COLUMN_LONGITUDE,hydrant.getLatlng().longitude);
        cv.put(MyDBHelper.COLUMN_MARKED,hydrant.hasMark()?"1":"0");
        cv.put(MyDBHelper.COLUMN_MARKED_STATE,hydrant.getMark_States());
        cv.put(MyDBHelper.COLUMN_PRESS,hydrant.getPress());
        cv.put(MyDBHelper.COLUMN_PS,(hydrant.getPs().compareTo("")==0)?"NONE":hydrant.getPs());
        cv.put(MyDBHelper.COLUMN_STATE,hydrant.getStatesForString());
        cv.put(MyDBHelper.COLUMN_TYPE,hydrant.getType());
        cv.put(MyDBHelper.COLUMN_TRAFFIC_LEVEL,hydrant.getTrafficLevel());

        db.insert(TABLE_HYDRANT,null,cv);

    }

    public static void replaceHydrant(SQLiteDatabase db,List<Object> row){
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COLUMN_ID,Integer.valueOf(row.get(COLUMN_INDEX_ID).toString()));
        cv.put(MyDBHelper.COLUMN_ADDRESS,row.get(COLUMN_INDEX_ADDRESS).toString());
        cv.put(MyDBHelper.COLUMN_DIST,row.get(COLUMN_INDEX_DIST).toString());

        String vil = row.get(COLUMN_INDEX_VIL).toString();

        cv.put(MyDBHelper.COLUMN_VIL,(vil.charAt(vil.length()-1)=='里')?vil:(vil+"里"));
        cv.put(MyDBHelper.COLUMN_LATITUDE,Double.parseDouble(row.get(COLUMN_INDEX_LATITUDE).toString()));
        cv.put(MyDBHelper.COLUMN_LONGITUDE,Double.parseDouble(row.get(COLUMN_INDEX_LONGITUDE).toString()));
        cv.put(MyDBHelper.COLUMN_MARKED,row.get(COLUMN_INDEX_MARKED).toString());
        cv.put(MyDBHelper.COLUMN_MARKED_STATE,row.get(COLUMN_INDEX_MARKED_STATE).toString());
        cv.put(MyDBHelper.COLUMN_PRESS,row.get(COLUMN_INDEX_PRESS).toString());
        cv.put(MyDBHelper.COLUMN_PS,row.size()<=COLUMN_INDEX_PS?"NONE":row.get(COLUMN_INDEX_PS).toString());
        cv.put(MyDBHelper.COLUMN_STATE,row.get(COLUMN_INDEX_STATE).toString());
        cv.put(MyDBHelper.COLUMN_TYPE,Integer.parseInt(row.get(COLUMN_INDEX_TYPE).toString()));
        cv.put(MyDBHelper.COLUMN_TRAFFIC_LEVEL,Integer.valueOf(row.get(COLUMN_INDEX_TRAFFIC_LEVEL).toString()));

        db.replace(TABLE_HYDRANT,null,cv);
        Log.e("replaceHydrant",cv.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HYDRANT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMP);

            this.onCreate(db);
        }
    }

    public static int getDataVersion(SQLiteDatabase db){
        String[] s = {COLUMN_DATA_VERSION};
        Cursor cs = db.query(TABLE_LOG,s,null,null,null,null,null);
        cs.moveToLast();
        int version = cs.getInt(cs.getColumnIndex(COLUMN_DATA_VERSION));

        return version;
    }

    public static void setDataVersion(SQLiteDatabase db, int newVersion,String changedId){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CHANGE_LOG,changedId);
        cv.put(COLUMN_DATA_VERSION,newVersion);
        cv.put(COLUMN_LOG_DATE,System.currentTimeMillis());
        cv.put("_id",newVersion);
        cv.put(COLUMN_LOG_READ,0);

       db.insert(TABLE_LOG,"NONE",cv);

    }

    public static int clearData (SQLiteDatabase db){
        int clearRows = db.delete(TABLE_HYDRANT,null,null)
                        + db.delete(TABLE_LOG,null,null)
                        +db.delete(TABLE_TEMP,null,null);
        ContentValues cv =new ContentValues();
        cv.put("_id",0);
        cv.put(COLUMN_DATA_VERSION,0);
        cv.put(COLUMN_CHANGE_LOG,"NONE");
        cv.put(COLUMN_LOG_DATE,System.currentTimeMillis());
        cv.put(COLUMN_LOG_READ,1);
        db.insert(TABLE_LOG,null,cv);

        return clearRows;
    }



    public static void replaceHydrant(SQLiteDatabase db,Hydrant hydrant){
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COLUMN_ID,hydrant.getId());
        cv.put(MyDBHelper.COLUMN_ADDRESS,hydrant.getAddress());
        cv.put(MyDBHelper.COLUMN_DIST,hydrant.getDist());
        cv.put(MyDBHelper.COLUMN_VIL,hydrant.getVil());
        cv.put(MyDBHelper.COLUMN_LATITUDE,hydrant.getLatlng().latitude);
        cv.put(MyDBHelper.COLUMN_LONGITUDE,hydrant.getLatlng().longitude);
        cv.put(MyDBHelper.COLUMN_MARKED,hydrant.hasMark()?"1":"0");
        cv.put(MyDBHelper.COLUMN_MARKED_STATE,hydrant.getMark_States());
        cv.put(MyDBHelper.COLUMN_PRESS,hydrant.getPress());
        cv.put(MyDBHelper.COLUMN_PS,(hydrant.getPs().compareTo("")==0)?"NONE":hydrant.getPs());
        cv.put(MyDBHelper.COLUMN_STATE,hydrant.getStatesForString());
        cv.put(MyDBHelper.COLUMN_TYPE,hydrant.getType());
        cv.put(MyDBHelper.COLUMN_TRAFFIC_LEVEL,hydrant.getTrafficLevel());

        db.replace(TABLE_HYDRANT,null,cv);

    }
    public static void replaceTempHydrant(SQLiteDatabase db,Hydrant hydrant){
        ContentValues cv = new ContentValues();
        cv.put(MyDBHelper.COLUMN_ID,hydrant.getId());
        cv.put(MyDBHelper.COLUMN_ADDRESS,hydrant.getAddress());
        cv.put(MyDBHelper.COLUMN_DIST,hydrant.getDist());
        cv.put(MyDBHelper.COLUMN_VIL,hydrant.getVil());
        cv.put(MyDBHelper.COLUMN_LATITUDE,hydrant.getLatlng().latitude);
        cv.put(MyDBHelper.COLUMN_LONGITUDE,hydrant.getLatlng().longitude);
        cv.put(MyDBHelper.COLUMN_MARKED,hydrant.hasMark()?"1":"0");
        cv.put(MyDBHelper.COLUMN_MARKED_STATE,hydrant.getMark_States());
        cv.put(MyDBHelper.COLUMN_PRESS,hydrant.getPress());
        cv.put(MyDBHelper.COLUMN_PS,(hydrant.getPs().compareTo("")==0)?"NONE":hydrant.getPs());
        cv.put(MyDBHelper.COLUMN_STATE,hydrant.getStatesForString());
        cv.put(MyDBHelper.COLUMN_TYPE,hydrant.getType());
        cv.put(MyDBHelper.COLUMN_TRAFFIC_LEVEL,hydrant.getTrafficLevel());

        db.replace(TABLE_TEMP,null,cv);
    }

    public static int getTempCount(SQLiteDatabase db){
        Cursor cs = db.query(MyDBHelper.TABLE_TEMP,
                new String[]{COLUMN_ID},
                null,null,null,null,null);

        return cs.getCount();
    }

    public static ArrayList<String> findNonExistDatas(SQLiteDatabase db , ArrayList<String> queryRanges)throws Exception{
        ArrayList<String> result = new ArrayList<>();
        for(String s:queryRanges){
            if(s.contains("-")){
                String[] childs = s.split("-");
                if(childs.length>2){
                    throw new Exception("Query Statement Error!");
                }else{
                    int max = Math.max(Integer.valueOf(childs[0]), Integer.valueOf(childs[1]));
                    int min = Math.min(Integer.valueOf(childs[0]), Integer.valueOf(childs[1]));
                    int queryCount = max - min +1;
                    Cursor cs = db.query(TABLE_HYDRANT,new String[]{COLUMN_ID},COLUMN_ID+ " >= "+min +" AND "+ COLUMN_ID+ " <= "+max +" AND NOT ( " + COLUMN_STATE + " LIKE \'%"+Hydrant.STATES.REMOVED+"%\' )",null,null,null,COLUMN_ID+" ASC");
                    if( cs.getCount() == queryCount) {
                        break;
                    }else if(cs.getCount()>0 && cs.moveToFirst()){
                        int tempMax ;
                        int tempMin = min;
                        do {
                            if (cs.getInt(0) == tempMin) {
                                tempMin++;

                            }else{
                                tempMax = cs.getInt(0)-1;
                                if(tempMin==tempMax)
                                    result.add(String.valueOf(tempMin));
                                else
                                    result.add(tempMin+"-"+tempMax);
                                tempMin = cs.getInt(0)+1;
                            }
                        }while (cs.moveToNext());
                        if(tempMin < max){
                            result.add(String.valueOf(tempMin)+"-"+max);
                        }else if(tempMin == max){
                            result.add(String.valueOf(max));
                        }
                    }else {
                        if(queryCount==1)
                            result.add(String.valueOf(min));
                        else
                            result.add(s);
                    }

                }
            }else{
                Cursor cs = db.query(TABLE_HYDRANT,new String[]{COLUMN_ID},COLUMN_ID+ " = "+s+" AND NOT ( " + COLUMN_STATE + " LIKE \'%"+Hydrant.STATES.REMOVED+"%\' )",null,null,null,null);
                if(cs.getCount()<=0)
                    result.add(s);
            }
        }
        return result;
    }

    public static void copyDatasIntoTemp(SQLiteDatabase db , ArrayList<String> ranges) throws Exception{
        ArrayList<Cursor> result = getDatas(db,ranges);

        for(Cursor cs:result){
            if(cs.getCount()>0&&cs.moveToFirst()){
                do{
                    Hydrant hydrant =new Hydrant(cs);
                    if(!hydrant.getStates().contains(Hydrant.STATES.REMOVED))
                        replaceTempHydrant(db,hydrant);
                }while (cs.moveToNext());
            }
        }
        Cursor cs = db.query(TABLE_TEMP,new String[]{COLUMN_ID},null,null,null,null,null);
        if(cs.getCount()<=0)
            throw new Exception("輸入的範圍未包含任何消防栓");

    }

    public static ArrayList<Cursor> getDatas(SQLiteDatabase db , ArrayList<String> ranges) throws Exception{
        ArrayList<Cursor> result = new ArrayList<>();
        for(String s : ranges){
            if(s.contains("-")){
                String[] childS = s.split("-");
                if(childS.length>2){
                    throw new Exception("Query statement error:"+s);
                }else {
                    int n1 = Integer.valueOf(childS[0]);
                    int n2 = Integer.valueOf(childS[1]);
                    result.add(db.query(TABLE_HYDRANT,new String[]{"*"},COLUMN_ID+" >= "+Math.min(n1,n2)+" AND "+COLUMN_ID+" <= "+Math.max(n1,n2),null,null,null,null));
                }
            }else{
                result.add(db.query(TABLE_HYDRANT,new String[]{"*"},COLUMN_ID+" = "+s,null,null,null,null));
            }
        }

        return result;
    }

}
