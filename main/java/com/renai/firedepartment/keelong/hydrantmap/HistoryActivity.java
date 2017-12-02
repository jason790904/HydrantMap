package com.renai.firedepartment.keelong.hydrantmap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    SQLiteDatabase db;

    ArrayList<Integer> logIdList;

    ListView listView;
    HistoryItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView =(ListView) findViewById(R.id.historyListView);

        db = new MyDBHelper(this).getReadableDatabase();
        logIdList = new ArrayList<>();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent();
            intent.setClass(this,ManualActivity.class);
            intent.putExtra(ManualActivity.ARG_MANUAL_TYPE,ManualActivity.MANUAL_TYPE_HISTORY);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onResume(){
        super.onResume();
        if(db==null)
            db = new MyDBHelper(this).getReadableDatabase();

        Cursor cs = db.query(MyDBHelper.TABLE_LOG,new String[]{"*"},MyDBHelper.COLUMN_ID+" <> 0",null,null,null,MyDBHelper.COLUMN_ID+MyDBHelper.DESC);

        List<HashMap<String,String>> dataset = new ArrayList<HashMap<String, String>>() ;
        adapter = new HistoryItemAdapter(this,dataset);

        if(cs.getCount()>0&&cs.moveToFirst()){
            do{
                HashMap<String,String> data = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("年 MM月 dd日");
                SimpleDateFormat year = new SimpleDateFormat("yyyy");
                SimpleDateFormat time = new SimpleDateFormat("HH:mm.ss");
                Date logDate = new Date(cs.getLong(cs.getColumnIndex(MyDBHelper.COLUMN_LOG_DATE)));
                int ROCyear = Integer.valueOf(year.format(logDate))-1911;
                data.put(HistoryItemAdapter.YYMMDD,ROCyear + sdf.format(logDate));
                data.put(HistoryItemAdapter.TIME,time.format(logDate));

                String log = cs.getString(cs.getColumnIndex(MyDBHelper.COLUMN_CHANGE_LOG));
                data.put(HistoryItemAdapter.IS_READ,(cs.getInt(cs.getColumnIndex(MyDBHelper.COLUMN_LOG_READ))==1)?"":"new");


                if(log.compareTo("all")==0){
                    Cursor tempCs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{MyDBHelper.COLUMN_ID},null,null,null,null,null);
                    data.put(HistoryItemAdapter.RANGE,"更動編號:全部");
                    data.put(HistoryItemAdapter.TOTAL_COUNT,"共"+tempCs.getCount()+"支");
                }else{
                    int count = 0;
                    String result ;
                    String[] range = log.split(";");
                    if(range.length>2){
                        result = range[0]+",...,"+range[range.length-1]+"\t   ";
                    }else
                        result  =  TextUtils.join(",",range)+"\t   ";
                    for(String s : range){
                        if(s.contains("-")){
                            String[] numbers = s.split("-");
                            count += Math.max(Integer.valueOf(numbers[0]),Integer.valueOf(numbers[1]))-Math.min(Integer.valueOf(numbers[0]),Integer.valueOf(numbers[1]))+1;
                        }else
                            count++;
                    }

                    data.put(HistoryItemAdapter.RANGE,"更動編號:"+result);
                    data.put(HistoryItemAdapter.TOTAL_COUNT,"共"+count+"支");
                }

                logIdList.add(cs.getInt(cs.getColumnIndex(MyDBHelper.COLUMN_ID)));
                dataset.add(data);

            }while (cs.moveToNext());

        }

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent();
                intent.setClass(HistoryActivity.this,InvestgationResultActivity.class);
                intent.putExtra(InvestgationResultActivity.BOOLEAN_ARG_ISINVESTGATION,false);

                ContentValues cv = new ContentValues();
                cv.put(MyDBHelper.COLUMN_LOG_READ,1);

                db.update(MyDBHelper.TABLE_LOG,cv,MyDBHelper.COLUMN_ID+" = "+logIdList.get(position),null);
                Cursor cs = db.query(MyDBHelper.TABLE_LOG,new String[]{MyDBHelper.COLUMN_CHANGE_LOG},MyDBHelper.COLUMN_ID +" = "+logIdList.get(position),null,null,null,null);
                if(cs.moveToFirst()) {
                    String log = cs.getString(0);
                    ArrayList<String> ranges = new ArrayList<String>();
                    if(log.compareTo("all")==0)
                        ranges.add(log);
                    else {
                        for (String s : log.split(";"))
                            ranges.add(s);
                    }
                    intent.putStringArrayListExtra(InvestgationResultActivity.STRINGARRAY_ARG_RANGES,ranges);
                    startActivity(intent);
                }
            }


        });

    }



    public class HistoryItemAdapter extends BaseAdapter
    {

        public static final String YYMMDD = "yyddmm";
        public static final String TIME = "time";
        public static final String RANGE = "range";
        public static final String TOTAL_COUNT = "count";
        public static final String IS_READ = "isRead";
        private LayoutInflater mLayInf;
        List<HashMap<String, String>> dataset;

        TextView textIsRead;
        TextView textIsCount;
        TextView textIsDate;
        TextView textIsRange;
        TextView textIsTime ;


        public HistoryItemAdapter(Context context,  List<HashMap<String, String>> itemList)
        {
            mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dataset = itemList;
        }

        @Override
        public int getCount()
        {
            //取得 ListView 列表 Item 的數量
            return dataset.size();
        }

        @Override
        public Object getItem(int position)
        {
            //取得 ListView 列表於 position 位置上的 Item
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            //取得 ListView 列表於 position 位置上的 Item 的 ID
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。
            View v = mLayInf.inflate(R.layout.history_item, parent, false);


             textIsRead = (TextView)v.findViewById(R.id.history_item_isRead);
             textIsCount = (TextView)v.findViewById(R.id.history_item_Count);
             textIsDate = (TextView)v.findViewById(R.id.history_item_Date);
             textIsRange = (TextView)v.findViewById(R.id.history_item_Range);
             textIsTime = (TextView)v.findViewById(R.id.history_item_Time);

            textIsRead.setText(dataset.get(position).get(IS_READ));
            textIsCount.setText(dataset.get(position).get(TOTAL_COUNT));
            textIsDate.setText(dataset.get(position).get(YYMMDD));
            textIsTime.setText(dataset.get(position).get(TIME));
            textIsRange.setText(dataset.get(position).get(RANGE));




            return v;
        }

    }

}
