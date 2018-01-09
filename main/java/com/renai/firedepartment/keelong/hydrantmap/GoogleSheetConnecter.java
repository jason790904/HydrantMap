package com.renai.firedepartment.keelong.hydrantmap;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by b7918101 on 2017/7/24.
 */

public class GoogleSheetConnecter {
    private com.google.api.services.sheets.v4.Sheets mService = null;

    public static final String SHEET_ID= your_sheet_id;

    public static final String TABLE_HYDRANT="hydrant";
    public static final String TABLE_LOG = "log";

    public static final String COLUMN_ID_1 = "A";
    public static final String COLUMN_STATE_2= "B";
    public static final String COLUMN_TYPE_3 = "C";
    public static final String COLUMN_MARKED_4= "D";
    public static final String COLUMN_MARKED_STATE_5 = "E";
    public static final String COLUMN_PRESS_6 = "F";
    public static final String COLUMN_DIST_7 = "G";
    public static final String COLUMN_VIL_8 = "H";
    public static final String COLUMN_ADDRESS_9 = "I";
    public static final String COLUMN_LATITUDE_10= "J";
    public static final String COLUMN_LONGITUDE_11 = "K";
    public static final String COLUMN_PS_12 = "L";
    public static final String COLUMN_TRAFFIC_LEVEL_13 = "M";

    public static final int TOTAL_COLUMN_COUNT=13;

    public static  final String RANGE_SERVER_MESSEGE = TABLE_LOG+"!C1:C2";
    public static final String RANGE_VERSION = TABLE_LOG+"!D1";
    public static final String COLUMN_VERSION_CODE = "D";
    public static final String COLUMN_VERSION_CHANGELOG = "F";

    public static final int CMD_CODE_END_APPLICATION = -1;
    public static final int CMD_CODE_NORMAL = 0;



    GoogleSheetConnecter(GoogleAccountCredential credential,HttpTransport transport, JsonFactory jsonFactory) {

        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("HydrantMap")
                .build();

    }

    public ValueRange Get(String range , String valueRenderOption, String dateTimeRenderOption) throws IOException{
        ValueRange response;

        Sheets.Spreadsheets.Values.Get request = mService.spreadsheets().values().get(SHEET_ID , range);
        request.setValueRenderOption(valueRenderOption);
        request.setDateTimeRenderOption(dateTimeRenderOption);

        response = request.execute();

        return  response;
    }

    public UpdateValuesResponse update(String range,String valueInputOption,ValueRange requestBody)throws Exception{
        UpdateValuesResponse response ;

        Sheets.Spreadsheets.Values.Update request = mService.spreadsheets().values().update(SHEET_ID,range,requestBody);
        request.setValueInputOption(valueInputOption);
        response = request.execute();

        return response;
    }


    public ClearValuesResponse Clear(String range,ClearValuesRequest requestBody)throws  Exception{
        ClearValuesResponse response;

        Sheets.Spreadsheets.Values.Clear request = mService.spreadsheets().values().clear(SHEET_ID,range,requestBody);
        response = request.execute();

        return response;
    }

    public AppendValuesResponse Append(String range,String valueInputOption, String insertDataOption, ValueRange requestBody)throws Exception{
        AppendValuesResponse response;

        Sheets.Spreadsheets.Values.Append request = mService.spreadsheets().values().append(SHEET_ID,range,requestBody);
        request.setValueInputOption(valueInputOption);
        request.setInsertDataOption(insertDataOption);

        response = request.execute();

        return response;
    }

    public BatchUpdateValuesResponse BatchUpdate(String valueInputOption , List<ValueRange> data)throws Exception{
        BatchUpdateValuesResponse response;
        BatchUpdateValuesRequest requestBody = new BatchUpdateValuesRequest();
        requestBody.setValueInputOption(valueInputOption);
        requestBody.setData(data);

        Sheets.Spreadsheets.Values.BatchUpdate request = mService.spreadsheets().values().batchUpdate(SHEET_ID,requestBody);

        response = request.execute();
        return response;
    }

    public BatchGetValuesResponse BatchGet(List<String> ranges,String valueRenderOption,String dateTimeRenderOption)throws Exception{
        BatchGetValuesResponse response;

        Sheets.Spreadsheets.Values.BatchGet request = mService.spreadsheets().values().batchGet(SHEET_ID);
        request.setRanges(ranges);
        request.setValueRenderOption(valueRenderOption);
        request.setDateTimeRenderOption(dateTimeRenderOption);

        response = request.execute();

        return response;
    }

    public static class DateTimeRenderOption{
        public static String SERIAL_NUMBER = "SERIAL_NUMBER";
        public static String FORMATTED_STRING = "FORMATTED_STRING";
    }
    public static class ValueRenderOption{
        public static String FORMATTED_VALUE="FORMATTED_VALUE";
        public static String UNFORMATTED_VALUE = "UNFORMATTED_VALUE";
        public static String FORMULA = "FORMULA";
    }

    public static class ValueInputOption{
        public static String INPUT_VALUE_OPTION_UNSPECIFIED = "INPUT_VALUE_OPTION_UNSPECIFIED";
        public static String RAW = "RAW";
        public static String USER_ENTERED ="USER_ENTERED";
    }

    public static class InsertDataOption{
        public static String OVERWRITE = "OVERWRITE";
        public static String INSERT_ROWS = "INSERT_ROWS";
    }

    public static int updateDBFromSheet (SQLiteDatabase db , GoogleSheetConnecter sc) throws Exception{

        int dbVersion = MyDBHelper.getDataVersion(db);
        ValueRange getVersionResponse = sc.Get(TABLE_LOG+"!A1",ValueRenderOption.FORMATTED_VALUE,DateTimeRenderOption.FORMATTED_STRING);
        List<List<Object>> value = getVersionResponse.getValues();
        int sheetVersion = 0;
        if(value != null)
         sheetVersion = Integer.parseInt(value.get(0).get(0).toString());
        if(sheetVersion != 0) {
            if (dbVersion < sheetVersion) {
                if(sheetVersion-dbVersion<12){
                   ValueRange getLogResponse = sc.Get(TABLE_LOG+"!B"+String.valueOf(dbVersion+1)+":B"+String.valueOf(sheetVersion)
                           ,ValueRenderOption.UNFORMATTED_VALUE,DateTimeRenderOption.FORMATTED_STRING);
                  List<List<Object>> values = getLogResponse.getValues();
                  int i = dbVersion+1;
                  for(List row : values) {
                      String changeTargetId = row.get(0).toString();
                      if (changeTargetId.compareTo("all") == 0) {
                          replaceAllHydrantFromSheet(db, sc);
                          MyDBHelper.setDataVersion(db,sheetVersion,"all");
                          return sheetVersion-dbVersion;
                      }else {
                          List<String> ranges = getRangesFromString(changeTargetId);
                          replaceHydrantFromSheet(db, sc, ranges);
                      }
                      MyDBHelper.setDataVersion(db, i,changeTargetId);
                      i++;
                  }
                }else{
                    replaceAllHydrantFromSheet(db,sc);
                    MyDBHelper.setDataVersion(db,sheetVersion,"all");
                }
                return sheetVersion-dbVersion;
            }else if(dbVersion>sheetVersion){

                MyDBHelper.clearData(db);

                return updateDBFromSheet(db,sc);
            }else
                return 0;

        }
        return -2;

    }

    public static int uploadDataToSheet(SQLiteDatabase db,GoogleSheetConnecter sc)throws Exception {

        int resuilt = 0;
        if (updateDBFromSheet(db, sc) >= 0) {
            Cursor cs = db.query(MyDBHelper.TABLE_TEMP,
                    new String[]{" * "},
                    null,null,null,null,MyDBHelper.COLUMN_ID+" ASC");
            if(cs.getCount()>0){
                resuilt = cs.getCount();
                cs.moveToFirst();
                List<ValueRange> vr = new ArrayList<>();
                String changedId = "";
                int lastId=-1;
                do{
                    String range;
                    List<List<Object>> values = new ArrayList<>();

                    range = TABLE_HYDRANT+"!"+COLUMN_ID_1+
                            cs.getString(cs.getColumnIndex(MyDBHelper.COLUMN_ID))+
                            ":"+ COLUMN_TRAFFIC_LEVEL_13 +
                            cs.getString(cs.getColumnIndex(MyDBHelper.COLUMN_ID));


                    Hydrant hydrant = new Hydrant(cs);
                    List<Object> row = hydrant.parseHydrantToSheetRow();
                    values.add(row);
                    ValueRange valueRange = new ValueRange();
                    valueRange.setValues(values);
                    valueRange.setRange(range);
                    vr.add(valueRange);

                    MyDBHelper.replaceHydrant(db,hydrant);

                    if(lastId == -1){
                        changedId += String.valueOf(hydrant.getId());
                    }else if (hydrant.getId()==lastId+1){
                        if(changedId.charAt(changedId.length()-1)!='-')
                            changedId +="-";
                    }else{
                        if(changedId.charAt(changedId.length()-1)=='-')
                            changedId += lastId+";"+hydrant.getId();
                        else
                            changedId +=";"+hydrant.getId();
                    }
                    lastId = hydrant.getId();
                }while (cs.moveToNext());

                if(changedId.charAt(changedId.length()-1)=='-')
                    changedId+=lastId+";";
                else
                    changedId+=";";

                cs.close();
                sc.BatchUpdate(ValueInputOption.USER_ENTERED,vr);
                ValueRange getVersionResponse = sc.Get(TABLE_LOG+"!A1",ValueRenderOption.FORMATTED_VALUE,DateTimeRenderOption.FORMATTED_STRING);
                List<List<Object>> value = getVersionResponse.getValues();
                int sheetVersion;
                if(value != null) {
                    sheetVersion = Integer.parseInt(value.get(0).get(0).toString());

                    ValueRange valueRange = new ValueRange();
                    List<List<Object>> values = new ArrayList<>();
                    List<Object> row = new ArrayList<>();
                    row.add(0,String.valueOf(sheetVersion + 1));
                    row.add(1,changedId);
                    values.add(row);
                    valueRange.setValues(values);
                    sc.Append(TABLE_LOG+"!A:B",ValueInputOption.USER_ENTERED,InsertDataOption.INSERT_ROWS,valueRange);

                    row.remove(1);
                    sc.update(TABLE_LOG + "!A1", ValueInputOption.USER_ENTERED, valueRange);

                    MyDBHelper.setDataVersion(db,sheetVersion+1,changedId);
                    db.delete(MyDBHelper.TABLE_TEMP,null,null);
                }

            }

        }
        return resuilt;
    }


    public static void replaceAllHydrantFromSheet(SQLiteDatabase db,GoogleSheetConnecter sc)throws Exception{
        MyDBHelper.clearData(db);
        ValueRange response = sc.Get(TABLE_HYDRANT+"!"+COLUMN_ID_1+":"+COLUMN_TRAFFIC_LEVEL_13,ValueRenderOption.UNFORMATTED_VALUE,DateTimeRenderOption.FORMATTED_STRING);
        List<List<Object>> value = response.getValues();
        if(value!=null){
            for (List rows : value){
                if(rows.size()==TOTAL_COLUMN_COUNT) {

                    MyDBHelper.replaceHydrant(db, rows);
                }else if(rows.size()>1){
                    throw new Exception("Error Occur On \nRange:"+response.getRange()+"\tIndexOfRow:"+value.indexOf(rows));
                }
            }
        }
    }

    public static List<String> getRangesFromString(String log){
        List<String> result = new ArrayList<>();
        String s = "";
        for(int i = 0 ;i<log.length();i++){
            if(log.charAt(i)==';'){
                result.add(TABLE_HYDRANT+"!"+COLUMN_ID_1+s+":"+COLUMN_TRAFFIC_LEVEL_13+s);
                s = "";
            }else if(log.charAt(i) =='-'){
                String s2 = TABLE_HYDRANT+"!"+COLUMN_ID_1+s+":"+COLUMN_TRAFFIC_LEVEL_13;
                s = "";
                for( i++ ; log.charAt(i)!=';';i++){
                    s+=log.charAt(i);
                }
                s2 +=s;
                result.add(s2);
                s="";
            }else
                s +=log.charAt(i);
        }
        return result;
    }

    public static int replaceHydrantFromSheet(SQLiteDatabase db,GoogleSheetConnecter sc,List<String> ranges)throws Exception{
        int replacedHydrantCount = 0;
        BatchGetValuesResponse response = sc.BatchGet(ranges,ValueRenderOption.UNFORMATTED_VALUE,DateTimeRenderOption.FORMATTED_STRING);
        List<ValueRange> valueRanges = response.getValueRanges();
        for(ValueRange vr: valueRanges){
            List<List<Object>> values = vr.getValues();
            if(values!=null){
                for(List row : values){
                    if(row.size()==TOTAL_COLUMN_COUNT) {
                        Hydrant hydrant = new Hydrant(row);
                        MyDBHelper.replaceHydrant(db, hydrant);
                        replacedHydrantCount++;
                    }else if(row.size() > 1 ){
                        throw new Exception("Error Occur On \nRange:"+vr.getRange()+"\tIndexOfRow:"+values.indexOf(row));
                    }
                }

            }
        }
        return replacedHydrantCount;
    }



}
