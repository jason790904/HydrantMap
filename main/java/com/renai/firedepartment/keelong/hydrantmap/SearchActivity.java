package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SearchActivity extends AppCompatActivity {


    RecyclerView recyclerViewUp;
    RecyclerView recyclerViewDown;

    FloatingActionButton btnAddUp;
    FloatingActionButton btnAddDown;
    FloatingActionButton btnQuery;

    searchConditionAdapter adapterUp;
    searchConditionAdapter adapterDown;


    boolean isDragToUp =false;
    boolean isDownDraging = false;

    boolean isDragToDown =false;
    boolean isUpDraging = false;


    SQLiteDatabase db;

    AlertDialog.Builder typeSelectDialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);






        db = new MyDBHelper(this).getWritableDatabase();


        recyclerViewUp = (RecyclerView)findViewById(R.id.search_page_recyclerViewUp);
        recyclerViewDown = (RecyclerView)findViewById(R.id.search_page_recyclerViewDown);
        btnAddDown= (FloatingActionButton)findViewById(R.id.search_page_addButtonDown);
        btnAddUp=(FloatingActionButton)findViewById(R.id.search_page_addButtonUp);
        btnQuery = (FloatingActionButton)findViewById(R.id.search_page_queryButtin);


        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterUp.getItemCount()>0||adapterDown.getItemCount()>0)
                    initailQueryString();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                    builder.setMessage("未輸入任何條件").setTitle("訊息").setPositiveButton("確定",null);
                }

            }
        });

        adapterUp = new searchConditionAdapter();
        adapterDown = new searchConditionAdapter();

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        lm.setAutoMeasureEnabled(true);
        RecyclerView.LayoutManager lm2 = new LinearLayoutManager(this);
        lm2.setAutoMeasureEnabled(true);
        recyclerViewUp.setLayoutManager(lm);
        recyclerViewDown.setLayoutManager(lm2);



        recyclerViewUp.setAdapter(adapterUp);
        recyclerViewDown.setAdapter(adapterDown);


        SimpleItemTouchHelperCallback callback =
                new SimpleItemTouchHelperCallback(adapterUp);
        callback.setLongPressDragEnabled(false);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewUp);

        SimpleItemTouchHelperCallback callbackDown =
                new SimpleItemTouchHelperCallback(adapterDown);
        callbackDown.setLongPressDragEnabled(false);
        ItemTouchHelper touchHelperDown = new ItemTouchHelper(callbackDown);
        touchHelperDown.attachToRecyclerView(recyclerViewDown);




        adapterDown.setOnItemLongClickListener(new searchConditionAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {



                ClipData.Item item = new ClipData.Item(String.valueOf(position));
                ClipData clipData = new ClipData(String.valueOf(position),new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},item);
                MyDragShadowBuilder builder = new MyDragShadowBuilder(view);

                adapterDown.setmOnItemDragListener(new searchConditionAdapter.OnItemDragListener() {
                    @Override
                    public boolean onItemDrag(View v, DragEvent event, int position) {

                        switch (event.getAction()){
                            case DragEvent.ACTION_DRAG_STARTED:
                                break;

                            case DragEvent.ACTION_DRAG_ENDED:
                                if(isDragToUp&&isDownDraging) {
                                    adapterUp.onItemInsert(0,adapterDown.getQueryString(position),adapterDown.getText(position));
                                    adapterDown.onItemDismiss(position);
                                    recyclerViewUp.scrollToPosition(0);

                                }
                                isDragToDown = false;
                                isUpDraging = false;
                                isDownDraging=false;
                                isDragToUp = false;
                                adapterDown.setmOnItemDragListener(null);
                                break;

                        }
                        return true;
                    }
                });
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                    view.startDragAndDrop(clipData,builder,null,View.DRAG_FLAG_GLOBAL);
                else
                    view.startDrag(clipData,builder,null,View.DRAG_FLAG_GLOBAL);

                isDownDraging = true;
                return true;
            }
        });




        recyclerViewUp.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(isDownDraging&&event.getAction()==DragEvent.ACTION_DRAG_ENTERED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_dark_normal_background);
                    isDragToUp = true;
                }else if(isDownDraging&&event.getAction()==DragEvent.ACTION_DRAG_EXITED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light_normal_background);
                    isDragToUp = false;
                }else if(event.getAction()==DragEvent.ACTION_DRAG_ENDED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light_normal_background);

                }
                return true;
            }
        });

        adapterUp.setOnItemLongClickListener(new searchConditionAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {



                ClipData.Item item = new ClipData.Item(String.valueOf(position));
                ClipData clipData = new ClipData(String.valueOf(position),new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},item);
                MyDragShadowBuilder builder = new MyDragShadowBuilder(view);

                adapterUp.setmOnItemDragListener(new searchConditionAdapter.OnItemDragListener() {
                    @Override
                    public boolean onItemDrag(View v, DragEvent event, int position) {

                        switch (event.getAction()){
                            case DragEvent.ACTION_DRAG_STARTED:
                                break;

                            case DragEvent.ACTION_DRAG_ENDED:
                                if(isDragToDown&&isUpDraging) {
                                    adapterDown.onItemInsert(0,adapterUp.getQueryString(position),adapterUp.getText(position));
                                    adapterUp.onItemDismiss(position);
                                    recyclerViewDown.scrollToPosition(0);

                                }
                                isDragToDown = false;
                                isUpDraging = false;
                                isDownDraging=false;
                                isDragToUp = false;
                                adapterUp.setmOnItemDragListener(null);
                                break;

                        }
                        return true;
                    }
                });

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                    view.startDragAndDrop(clipData,builder,null,View.DRAG_FLAG_GLOBAL);
                else
                    view.startDrag(clipData,builder,null,View.DRAG_FLAG_GLOBAL);
                isUpDraging = true;
                return true;
            }
        });




        recyclerViewDown.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(isUpDraging&&event.getAction()==DragEvent.ACTION_DRAG_ENTERED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_dark_normal_background);
                    isDragToDown = true;
                }else if(isUpDraging&&event.getAction()==DragEvent.ACTION_DRAG_EXITED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light_normal_background);
                    isDragToDown = false;
                }else if(event.getAction()==DragEvent.ACTION_DRAG_ENDED){
                    v.setBackgroundResource(R.drawable.common_google_signin_btn_icon_light_normal_background);
                }
                return true;
            }
        });


        btnAddUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUp = true;
                showAddTypeDialog();
            }
        });

        btnAddDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUp = false;
                showAddTypeDialog();
            }
        });

    }

    private void initailQueryString() {
        String whereString = "";
        if(adapterUp.getItemCount()>0){
            whereString = " ( "+adapterUp.getQueryString(0);
            for(int i = 1;i<adapterUp.getItemCount();i++){
                whereString += " AND "+adapterUp.getQueryString(i);
            }
            whereString +=" ) ";
            if(adapterDown.getItemCount()>0)
                whereString += " AND ";
        }
        if(adapterDown.getItemCount()>0){
            whereString += " ( "+adapterDown.getQueryString(0);
            for(int i = 1;i<adapterDown.getItemCount();i++){
                whereString += " OR "+adapterDown.getQueryString(i);
            }
            whereString +=" ) ";
        }

        showQueryResult(whereString);

    }

    private void showQueryResult(String whereString) {

        Intent intent = new Intent();
        intent.putExtra(InvestgationResultActivity.BOOLEAN_ARG_ISINVESTGATION,false);
        intent.putExtra(InvestgationResultActivity.BOOLEAN_IS_SEARCH,true);
        intent.putExtra(InvestgationResultActivity.WHERE_STRING,whereString);
        intent.setClass(this,InvestgationResultActivity.class);
        startActivity(intent);

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
            intent.putExtra(ManualActivity.ARG_MANUAL_TYPE,ManualActivity.MANUAL_TYPE_SEARCH);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    int selectedType = 0;
    boolean isUp;

    final static int TYPE_ID = 0;
    final static int TYPE_TYPE = 1;
    final static int TYPE_PRESS = 2;
    final static int TYPE_STATES = 3;
    final static int TYPE_MARKER = 4;
    final static int TYPE_DUTY= 5;
    final static int TYPE_ADDRESS = 6;
    final static int TYPE_TRAFFIC_LEVEL = 7;
    final static int TYPE_PS = 8;


    private void showAddTypeDialog( ){
        selectedType = 0;
        if(typeSelectDialog == null){
            typeSelectDialog = new AlertDialog.Builder(this);
            final String[] typeList = {"編號","類型","壓力","消防栓狀況","告示牌","責任轄區","地址","道路寬度","備註"};
            typeSelectDialog.setSingleChoiceItems(typeList, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedType = which;
                }
            });
            typeSelectDialog.setTitle("選擇條件類型").setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                   showConditionDialog(selectedType);
                }
            });
            typeSelectDialog.setNegativeButton("取消",null).show();
        }else{
            typeSelectDialog.show();
        }


    }

    private void showConditionDialog(int type){
        switch (type){
            case TYPE_ID:
                final View id_content =LayoutInflater.from(this).inflate(R.layout.dialog_content_id,null);
                final Spinner logicSpinner = (Spinner) id_content.findViewById(R.id.dialog_id_spinner);
                final EditText IdInput = (EditText)id_content.findViewById(R.id.dialog_ideditText) ;
                IdInput.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_PHONE);
                final TextWatcher IdRangeTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String input = s.toString();
                        if(input.length()>0){
                            if(!input.contains("-")){
                                IdInput.setText(input+"-");
                                IdInput.setSelection(IdInput.getText().length()-1);
                            }
                        }
                    }
                };
                final TextView.OnEditorActionListener IdRangeActionListener = new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId == EditorInfo.IME_ACTION_DONE){
                                String input = v.getText().toString();
                                int dashPosition = input.indexOf("-");
                                EditText editText = ((EditText) v);
                                if(dashPosition>=0) {
                                    if(editText.getSelectionStart()<=dashPosition)
                                        editText.setSelection(dashPosition + 1, input.length());
                                    else
                                        return false;
                                }
                            return true;
                        }
                        return false;
                    }
                };
/*
                final View.OnClickListener IdRangeClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editText = (EditText)v;
                        if(editText.getText().toString().contains("-")){
                            int dashPosition = editText.getText().toString().indexOf("-");
                            if(editText.getSelectionStart() <= dashPosition){
                                editText.setSelection(0,dashPosition);
                            }else {
                                editText.setSelection(dashPosition + 1, editText.getText().length());

                            }
                        }
                    }
                };*/
                IdInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                //IdInput.setTextIsSelectable(true);

                logicSpinner.setAdapter( new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,new String[]{"範圍"," = "," > "," < "," >= "," <= "," != "}));
                logicSpinner.setSelection(0);
                logicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        IdInput.setText("");
                        if(position == 0){
                            IdInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                            IdInput.setOnEditorActionListener(IdRangeActionListener);
                            IdInput.addTextChangedListener(IdRangeTextWatcher);
                            //IdInput.setOnClickListener(IdRangeClickListener);
                        }else {
                            IdInput.removeTextChangedListener(IdRangeTextWatcher);
                            IdInput.setOnEditorActionListener(null);
                           // IdInput.setOnClickListener(null);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                final AlertDialog.Builder idDialogBuilder = new AlertDialog.Builder(this);

                idDialogBuilder.setView(id_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = ((EditText)id_content.findViewById(R.id.dialog_ideditText)).getText().toString();
                        if(input.compareTo("")!=0) {
                            String text = "ID ";
                            String queryString = MyDBHelper.COLUMN_ID;

                            if(logicSpinner.getSelectedItemPosition()==0){
                                int dashCount= 0;
                                for(char c:input.toCharArray()){
                                    if(c == '-')
                                        dashCount++;
                                }
                                if(dashCount>1 ){
                                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                                    errorDialog.setTitle("錯誤").setMessage("「-」符號只能有一個!").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showConditionDialog(TYPE_ID);
                                        }
                                    }).show();
                                    dialog.dismiss();
                                    return;
                                }

                                if(dashCount >0){
                                    int i = input.indexOf("-");
                                    if(i==0 || i==input.length()-1){
                                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                                        errorDialog.setTitle("錯誤").setMessage("「-」符號兩端必須連接數字!").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                showConditionDialog(TYPE_ID);
                                            }
                                        }).show();
                                        dialog.dismiss();
                                        return;
                                    }

                                    String[] split = input.split("-");
                                    int num1 = Integer.valueOf(split[0]);
                                    int num2 = Integer.valueOf(split[1]);
                                    queryString+= " >= "+ Math.min(num1,num2)+" AND "+MyDBHelper.COLUMN_ID+" <= "+Math.max(num1,num2);
                                    text+=" :"+new DecimalFormat("0000").format(Math.min(num1,num2))+"-"+new DecimalFormat("0000").format(Math.max(num1,num2));
                                }else{
                                    queryString+= " = "+ Integer.valueOf(input);
                                    text+=" = "+new DecimalFormat("0000").format(Integer.valueOf(input));
                                }

                            }else {
                                text += logicSpinner.getSelectedItem().toString() + new DecimalFormat("0000").format(Integer.valueOf(input));
                                queryString += logicSpinner.getSelectedItem().toString() + input + " ";
                            }

                            if (isUp) {
                                adapterUp.onItemInsert(queryString, text);
                            } else
                                adapterDown.onItemInsert(queryString, text);
                        }else{
                            AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                            errorDialog.setTitle("錯誤").setMessage("值不能為空值").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showConditionDialog(TYPE_ID);
                                }
                            }).show();
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;
                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();

                break;
            case TYPE_TYPE:

                final View type_content =LayoutInflater.from(this).inflate(R.layout.dialog_content_type,null);
                final Spinner typeSpinner = (Spinner) type_content.findViewById(R.id.dialog_type_spinner);
                String[] typeList = new String[3];
                typeList[Hydrant.TYPE.ANTISLIP] = "新式開關";
                typeList[Hydrant.TYPE.GROUND] = "地上式";
                typeList[Hydrant.TYPE.UNDER] = "地下式";

                typeSpinner.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,typeList));
                typeSpinner.setSelection(0);
                final AlertDialog.Builder typeDialogBuilder = new AlertDialog.Builder(this);
                typeDialogBuilder.setView(type_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = "類型 =  ";
                                String queryString = MyDBHelper.COLUMN_TYPE;
                                text += typeSpinner.getSelectedItem().toString();
                                queryString += " = "+Hydrant.TYPE.getAllType().get(typeSpinner.getSelectedItemPosition())+" ";
                                if(isUp){
                                    adapterUp.onItemInsert(queryString,text);
                                }else
                                    adapterDown.onItemInsert(queryString,text);
                            }
                        }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();

                break;
            case TYPE_PRESS:
                final View press_content =LayoutInflater.from(this).inflate(R.layout.dialog_content_id,null);
                final Spinner logicPressSpinner = (Spinner) press_content.findViewById(R.id.dialog_id_spinner);
                ((TextView)press_content.findViewById(R.id.dialog_id_label)).setText("測壓 ");
                logicPressSpinner.setAdapter( new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,new String[]{" = "," > "," < "," >= "," <= "," != "}));
                logicPressSpinner.setSelection(0);
                final EditText inputEditText = (EditText)press_content.findViewById(R.id.dialog_ideditText);
                inputEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER);
                final AlertDialog.Builder pressDialogBuilder = new AlertDialog.Builder(this);
                pressDialogBuilder.setView(press_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String input = inputEditText.getText().toString();
                                if(input.compareTo("")!=0 && input.indexOf(".")!=0) {
                                    String text = "測壓 ";
                                    String queryString = MyDBHelper.COLUMN_PRESS;
                                    text += logicPressSpinner.getSelectedItem().toString() + input;
                                    queryString += logicPressSpinner.getSelectedItem().toString() + input + " ";
                                    if (isUp) {
                                        adapterUp.onItemInsert(queryString, text);
                                    } else
                                        adapterDown.onItemInsert(queryString, text);
                                }else if(input.indexOf(".")==0){
                                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                                    errorDialog.setTitle("錯誤").setMessage("小數點前不能為空值").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showConditionDialog(TYPE_PRESS);
                                        }
                                    }).show();
                                    dialog.dismiss();
                                }else{
                                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                                    errorDialog.setTitle("錯誤").setMessage("值不能為空值").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showConditionDialog(TYPE_PRESS);
                                        }
                                    }).show();
                                    dialog.dismiss();
                                }
                            }
                        }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();
                break;
            case TYPE_STATES:
                final View state_content1 = LayoutInflater.from(this).inflate(R.layout.dialog_content_type,null);
                final Spinner page1Spinner = (Spinner) state_content1.findViewById(R.id.dialog_type_spinner);
                ((TextView)state_content1.findViewById(R.id.dialog_type_label)).setText("狀態 : ");
                page1Spinner.setAdapter(new ArrayAdapter<String>(this,R.layout.dialog_list_item_1,new String[]{"良好","異常"}));
                page1Spinner.setSelection(0);

                final DialogInterface.OnClickListener stateDialogListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(page1Spinner.getSelectedItemPosition()==0) {
                            String text = "狀態:  良好";
                            String queryString = MyDBHelper.COLUMN_STATE;
                            queryString += " = \'" +Hydrant.STATES.GOOD+"\'";
                            if (isUp) {
                                adapterUp.onItemInsert(queryString, text);
                            } else
                                adapterDown.onItemInsert(queryString, text);
                        }else{
                            final View state_content2 =LayoutInflater.from(SearchActivity.this).inflate(R.layout.dialog_content_states,null);
                            final Spinner page2stateSpinner1 = (Spinner) state_content2.findViewById(R.id.dialog_states_spinner1);
                            final Spinner page2stateSpinner2 = (Spinner) state_content2.findViewById(R.id.dialog_states_spinner2);
                            String[] stateList1 = new String[]{"不指定","包含","不包含"};
                            page2stateSpinner1.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,stateList1));

                            final List<Integer> stateCodeList = Hydrant.STATES.getAllStates();
                            stateCodeList.remove(Hydrant.STATES.getAllStates().indexOf(Hydrant.STATES.GOOD));
                            final String[] stateStringList = new String[stateCodeList.size()];
                            for(int i =0;i<stateCodeList.size();i++){
                                stateStringList[i] = Hydrant.getChineseState(stateCodeList.get(i));
                            }

                            page2stateSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        page2stateSpinner2.setAdapter(null);
                                        page2stateSpinner2.setEnabled(false);
                                    }else{
                                        page2stateSpinner2.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,stateStringList));
                                        page2stateSpinner2.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            page2stateSpinner1.setSelection(0);

                            page2stateSpinner2.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,stateStringList));
                            page2stateSpinner2.setSelection(0);

                            AlertDialog.Builder page2StateConditionDialog = new AlertDialog.Builder(SearchActivity.this);
                            page2StateConditionDialog.setTitle("輸入條件").setView(state_content2).setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String text = "狀態 ";
                                    String queryString = "( "+MyDBHelper.COLUMN_STATE;
                                    if(page2stateSpinner1.getSelectedItemPosition()!=0) {
                                        text += page2stateSpinner1.getSelectedItem().toString()+" ";
                                        if (page2stateSpinner1.getSelectedItemPosition() == 2)
                                            queryString = "NOT " + queryString;
                                        queryString += " = \'" + stateCodeList.get(page2stateSpinner2.getSelectedItemPosition()) + "\' OR "+MyDBHelper.COLUMN_STATE + " LIKE \'%," + stateCodeList.get(page2stateSpinner2.getSelectedItemPosition()) + ",%\' OR "
                                                + MyDBHelper.COLUMN_STATE + " LIKE \'" + stateCodeList.get(page2stateSpinner2.getSelectedItemPosition()) + ",%\' OR " +
                                                MyDBHelper.COLUMN_STATE + " LIKE \'%," + stateCodeList.get(page2stateSpinner2.getSelectedItemPosition()) + "\' )";
                                        text += page2stateSpinner2.getSelectedItem().toString();
                                    }else{
                                        text+= ": 異常";
                                        queryString =" NOT "+ queryString +  " = \'" +Hydrant.STATES.GOOD+"\' )";
                                    }
                                    if (isUp) {
                                        adapterUp.onItemInsert(queryString, text);
                                    } else
                                        adapterDown.onItemInsert(queryString, text);
                                }
                            }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showConditionDialog(TYPE_STATES);
                                    dialog.dismiss();
                                }
                            }).setNeutralButton("取消",null).show();
                            dialog.dismiss();
                        }

                    }
                };


                final AlertDialog.Builder stateDialogBuilder1 = new AlertDialog.Builder(this);
                stateDialogBuilder1.setView(state_content1).setTitle("輸入條件")
                        .setPositiveButton("確認",stateDialogListener ).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 selectedType = 0;

                                 showAddTypeDialog();
                              }
                        }).setNeutralButton("取消",null).show();


                break;
            case TYPE_DUTY:

                final View duty_content =LayoutInflater.from(this).inflate(R.layout.dialog_content_type,null);
                final Spinner dutySpinner = (Spinner) duty_content.findViewById(R.id.dialog_type_spinner);
                final ArrayList<Integer> dutyCodeList = Hydrant.DUTY.getAllDuty();
                ArrayList<String> dutyNameList = new ArrayList<>();
                for(int code:dutyCodeList)
                    dutyNameList.add(Hydrant.DUTY.getDutyName(code));
                ((TextView)duty_content.findViewById(R.id.dialog_type_label)).setText("轄區=");
                dutySpinner.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,dutyNameList));
                dutySpinner.setSelection(0);
                final AlertDialog.Builder dutyDialogBuilder = new AlertDialog.Builder(this);
                dutyDialogBuilder.setView(duty_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = "隸屬轄區 =  ";
                                String queryString = MyDBHelper.COLUMN_ID+" >= " + dutyCodeList.get(dutySpinner.getSelectedItemPosition()) +"000 AND "
                                        +MyDBHelper.COLUMN_ID + " <= " + dutyCodeList.get(dutySpinner.getSelectedItemPosition()) +"999 ";
                                text += dutySpinner.getSelectedItem().toString();
                                if(isUp){
                                    adapterUp.onItemInsert(queryString,text);
                                }else
                                    adapterDown.onItemInsert(queryString,text);
                            }
                        }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();
                break;
            case TYPE_ADDRESS:

                AlertDialog.Builder addressDialogBuilder1 = new AlertDialog.Builder(this);
                final View address_content = LayoutInflater.from(this).inflate(R.layout.dialog_content_address,null);
                final Spinner spinnerDist = (Spinner)address_content.findViewById(R.id.dialog_address_spinner1);
                final Spinner spinnerVil = (Spinner)address_content.findViewById(R.id.dialog_address_spinner2);
                final List<Integer> distCodeList = Hydrant.DIST.getAllDist();
                String[] distStringCodeList = new String[distCodeList.size()+1];
                distStringCodeList[0] = "不指定";
                for(int i =1;i<distCodeList.size()+1;i++)
                    distStringCodeList[i] = Hydrant.getChineseDist(distCodeList.get(i-1));
                spinnerDist.setAdapter(new ArrayAdapter<String>(this,R.layout.dialog_list_item_1,distStringCodeList));
                spinnerVil.setAdapter(new ArrayAdapter<String>(this,R.layout.dialog_list_item_1,new String[]{"不指定"}));
                spinnerDist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position ==0 ){
                            spinnerVil.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,new String[]{"不指定"}));
                        }else{
                            List<String> vilStringList = Hydrant.VIL.getVil(distCodeList.get(position-1));
                            vilStringList.add(0,"不指定");
                            spinnerVil.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,vilStringList));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spinnerDist.setSelection(0);
                spinnerVil.setSelection(0);
                addressDialogBuilder1.setView(address_content).setTitle("輸入條件").setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputEditText = (EditText)address_content.findViewById(R.id.dialog_address_editText1);
                        String inputAddress = inputEditText.getText().toString();
                        String text = "地址 = ";
                        String queryString = "( ";
                        String[] inputSections = inputAddress.split(" ");
                        if(spinnerDist.getSelectedItemPosition()!=0 || spinnerVil.getSelectedItemPosition() != 0 || (inputAddress.length()>0 && inputSections.length>0)) {
                            if (spinnerDist.getSelectedItemPosition() != 0) {
                                text += spinnerDist.getSelectedItem().toString() + " ";
                                int dist = distCodeList.get(spinnerDist.getSelectedItemPosition() - 1);
                                queryString += MyDBHelper.COLUMN_DIST + " LIKE \'" + dist + "\' ";

                                if (spinnerVil.getSelectedItemPosition() != 0) {
                                    text += spinnerVil.getSelectedItem().toString();
                                    String vil = spinnerVil.getSelectedItem().toString();
                                    queryString += "AND " + MyDBHelper.COLUMN_VIL + " LIKE  \'%" + vil.substring(0,vil.length()-1) + "%\' ";
                                }
                            }
                            text += " " + inputAddress;

                            if (inputAddress.length() > 0 && inputSections.length > 0) {
                                if (queryString.length() > 3)
                                    queryString += " AND ";
                                queryString += " ( " + MyDBHelper.COLUMN_ADDRESS + " LIKE \'%" + inputSections[0] + "%\' ";
                                for (int i = 1; i < inputSections.length; i++) {
                                    queryString += " AND " + MyDBHelper.COLUMN_ADDRESS + " LIKE \'%" + inputSections[i] + "%\' ";
                                }
                                queryString += " )";
                            }
                            queryString += " )";
                            if (isUp) {
                                adapterUp.onItemInsert(queryString, text);
                            } else
                                adapterDown.onItemInsert(queryString, text);
                        }else{
                            AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                            errorDialog.setTitle("錯誤").setMessage("請至少輸入一個條件").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showConditionDialog(TYPE_ADDRESS);
                                }
                            }).show();
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();


                break;
            case TYPE_TRAFFIC_LEVEL:

                final View traffic_content =LayoutInflater.from(this).inflate(R.layout.dialog_traffic_content,null);
                final Spinner trafficSpinner1 = (Spinner) traffic_content.findViewById(R.id.dialog_traffic_spinner1);
                final Spinner trafficSpinner2 = (Spinner) traffic_content.findViewById(R.id.dialog_traffic_spinner2);

                trafficSpinner1.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,new String[]{"<=","=",">="}));
                trafficSpinner1.setSelection(0);

                String[] levelList = new String[Hydrant.TRAFFIC.LevelName().size()-1];
                for(int i = 1 ;i<Hydrant.TRAFFIC.LevelName().size();i++){
                    levelList[i-1] = i+"級，"+Hydrant.TRAFFIC.LevelName().get(i)+" 可通行";
                }
                trafficSpinner2.setAdapter(new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,levelList));


                final AlertDialog.Builder trafficDialogBuilder = new AlertDialog.Builder(this);
                trafficDialogBuilder.setView(traffic_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = "道路寬度 ";
                                String queryString = MyDBHelper.COLUMN_TRAFFIC_LEVEL;
                                text += trafficSpinner1.getSelectedItem().toString() + " "+trafficSpinner2.getSelectedItem().toString();
                                switch (trafficSpinner1.getSelectedItemPosition()){
                                    case 0:
                                        queryString += " <= ";
                                        break;
                                    case 1:
                                        queryString += " = ";
                                        break;
                                    case 2:
                                        queryString += " >= " ;
                                        break;
                                }
                                queryString += (trafficSpinner2.getSelectedItemPosition()+1) ;

                                if(isUp){
                                    adapterUp.onItemInsert(queryString,text);
                                }else
                                    adapterDown.onItemInsert(queryString,text);
                            }
                        }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();

                break;
            case TYPE_MARKER:
                AlertDialog.Builder markerDialogBuilder1 = new AlertDialog.Builder(this);
                final View marker_content = LayoutInflater.from(this).inflate(R.layout.dialog_content_address,null);
                final Spinner spinnerMarkerExist = (Spinner)marker_content.findViewById(R.id.dialog_address_spinner1);
                final Spinner spinnerMarkerState = (Spinner)marker_content.findViewById(R.id.dialog_address_spinner2);
                final EditText etcStateInput = (EditText)marker_content.findViewById(R.id.dialog_address_editText1);
                ((TextView)marker_content.findViewById(R.id.dialog_address_labelUp)).setText("告示牌 : ");
                ((TextView)marker_content.findViewById(R.id.dialog_address_labelMid)).setText("狀況 : ");
                ((TextView)marker_content.findViewById(R.id.dialog_address_labelDown)).setText("  ");


                spinnerMarkerExist.setAdapter(new ArrayAdapter<String>(this,R.layout.dialog_list_item_1,new String[]{"無設置","有設置"}));
                final List<String> markStateList =Hydrant.MARK_STATES.getAllStates();
                markStateList.add(0,"不指定");

                final ArrayAdapter<String> MarkStateAdapter = new ArrayAdapter<String>(this,R.layout.dialog_list_item_1,markStateList);
                final int etcPosision =markStateList.indexOf(Hydrant.MARK_STATES.ETC);
                spinnerMarkerState.setAdapter(MarkStateAdapter);
                spinnerMarkerExist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position ==0 ){
                            spinnerMarkerState.setAdapter(null);
                            spinnerMarkerState.setEnabled(false);
                            etcStateInput.setHint("");
                            etcStateInput.setEnabled(false);
                            etcStateInput.setVisibility(View.INVISIBLE);
                        }else{
                            spinnerMarkerState.setAdapter(MarkStateAdapter);
                            spinnerMarkerState.setEnabled(true);
                            spinnerMarkerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if(position == etcPosision){
                                        etcStateInput.setHint("輸入其他條件");
                                        etcStateInput.setEnabled(true);
                                        etcStateInput.setVisibility(View.VISIBLE);
                                    }else{
                                        etcStateInput.setHint("");
                                        etcStateInput.setEnabled(false);
                                        etcStateInput.setVisibility(View.INVISIBLE);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinnerMarkerExist.setSelection(1);
                spinnerMarkerState.setSelection(0);
                markerDialogBuilder1.setView(marker_content).setTitle("輸入條件").setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = "告示牌 = ";
                        String queryString = "";
                        if(spinnerMarkerExist.getSelectedItemPosition() == 0){
                            queryString = MyDBHelper.COLUMN_MARKED + " = 0 ";
                            text+="無設置";

                        }else{
                            if(spinnerMarkerState.getSelectedItemPosition()==0){
                                queryString = MyDBHelper.COLUMN_MARKED + " = 1 ";
                                text+="有設置";
                            }else if(spinnerMarkerState.getSelectedItemPosition() == markStateList.indexOf(Hydrant.MARK_STATES.ETC)){
                                String input =etcStateInput.getText().toString();
                                String[] inputs = etcStateInput.getText().toString().split(" ");
                                if(inputs.length<=0 || input.length() <=0 ){
                                    List<String> allStates = Hydrant.MARK_STATES.getAllStates();
                                    allStates.remove(Hydrant.MARK_STATES.ETC);
                                    String queryResult = "( \'"+allStates.get(0)+"\'";
                                    for(int i=1;i<allStates.size();i++){
                                        queryResult+= ",\'"+allStates.get(i)+"\'";
                                    }
                                    queryResult+=" ) ";
                                    queryString =MyDBHelper.COLUMN_MARKED_STATE+" NOT IN "+queryResult;
                                    text+= " 其他";
                                }else{
                                    queryString += " ( " + MyDBHelper.COLUMN_MARKED_STATE + " LIKE \'%" + inputs[0] + "%\' ";
                                    for (int i = 1; i < inputs.length; i++) {
                                        queryString += " AND " + MyDBHelper.COLUMN_MARKED_STATE + " LIKE \'%" + inputs[i] + "%\' ";
                                    }
                                    queryString += " )";
                                    text += etcStateInput.getText();
                                }

                            }else{
                                queryString = MyDBHelper.COLUMN_MARKED_STATE+" = \'"+spinnerMarkerState.getSelectedItem().toString()+"\' ";
                                text+=spinnerMarkerState.getSelectedItem().toString();
                            }
                        }
                        if(isUp){
                            adapterUp.onItemInsert(queryString,text);
                        }else
                            adapterDown.onItemInsert(queryString,text);
                    }
                }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();
                break;
            case TYPE_PS:

                final View ps_content =LayoutInflater.from(this).inflate(R.layout.dialog_content_id,null);
                final Spinner psLogicSpinner = (Spinner) ps_content.findViewById(R.id.dialog_id_spinner);
                ((TextView)ps_content.findViewById(R.id.dialog_id_label)).setText("備註 ");
                final EditText psKeyInput = (EditText)ps_content.findViewById(R.id.dialog_ideditText);
                psKeyInput.setInputType(InputType.TYPE_CLASS_TEXT);
                psLogicSpinner.setAdapter( new ArrayAdapter<String>(SearchActivity.this,R.layout.dialog_list_item_1,new String[]{"不包含","包含"}));
                psLogicSpinner.setSelection(1);
                final AlertDialog.Builder psDialogBuilder = new AlertDialog.Builder(this);
                psDialogBuilder.setView(ps_content).setTitle("輸入條件")
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String input =psKeyInput.getText().toString();
                                String[] inputs = input.split(" ");
                                String queryString ="";

                                if(inputs.length>0 && input.length()>0) {
                                    if(psLogicSpinner.getSelectedItemPosition() == 0)
                                        queryString = "NOT ";
                                    queryString += "( " + MyDBHelper.COLUMN_PS+" LIKE \'%"+inputs[0]+"%\' ";
                                    for(int i = 1; i<inputs.length;i++){
                                        queryString += "AND " + MyDBHelper.COLUMN_PS+" LIKE \'%"+inputs[i]+"%\' ";
                                    }
                                    queryString += " ) ";
                                    String text = "備註  ";
                                    text += psLogicSpinner.getSelectedItem().toString()+" " + input;

                                    if (isUp) {
                                        adapterUp.onItemInsert(queryString, text);
                                    } else
                                        adapterDown.onItemInsert(queryString, text);
                                }else{
                                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(SearchActivity.this);
                                    errorDialog.setTitle("錯誤").setMessage("值不能為空值").setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showConditionDialog(TYPE_PS);
                                        }
                                    }).show();
                                    dialog.dismiss();
                                }
                            }
                        }).setNegativeButton("上一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedType = 0;

                        showAddTypeDialog();
                    }
                }).setNeutralButton("取消",null).show();

                break;
        }
    }

    public void debugShowQueryString(){
        AlertDialog.Builder debugDialog = new AlertDialog.Builder(this);
        debugDialog.setTitle("QueryString");
        String queryString = "Up\n--------------------\n";
        for (int i =0 ;i<adapterUp.getItemCount() ; i++){
            queryString+=adapterUp.getQueryString(i)+"\n";
        }
        queryString += "Down\n--------------------\n";
        for(int i =0 ;i<adapterDown.getItemCount();i++){
            queryString+=adapterDown.getQueryString(i)+"\n";
        }
        debugDialog.setMessage(queryString).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(adapterUp.getItemCount()>0||adapterDown.getItemCount()>0)
                    initailQueryString();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                    builder.setMessage("未輸入任何條件").setTitle("訊息").setPositiveButton("確定",null);
                }
            }
        }).show();

    }



    protected static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;
        private static View mView;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);
            mView = v;

            // Creates a draggable image that will fill the Canvas provided by the system.
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            // Defines local variables
            final View view = mView;
            if (view != null) {
                size.set(view.getWidth(), view.getHeight());
                touch.set(size.x/4, size.y/4);
            }
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            super.onDrawShadow(canvas);
        }
    }


}


class searchConditionAdapter extends RecyclerView.Adapter<searchConditionAdapter.ViewHolder> implements View.OnClickListener , SimpleItemTouchHelperCallback.ItemTouchHelperAdapter , View.OnLongClickListener ,View.OnDragListener{
    private List<String> mDataset;
    private List<String> mTextShow;

    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongClickListener mOnItemLongClickListener = null;
    private OnItemDragListener mOnItemDragListener = null;

    int longClickPosition = -1;

    ArrayList<View> viewList = new ArrayList<>();

    public searchConditionAdapter(){
        mDataset = new ArrayList<>();
        mTextShow = new ArrayList<>();
    }


    @Override
    public void onItemDismiss(int position) {
        mDataset.remove(position);
        mTextShow.remove(position);
        notifyItemRemoved(position);
        viewList.remove(position);
    }

    @Override
    public void onItemMove(int from, int to) {
        Collections.swap(mDataset, from, to);
        Collections.swap(mTextShow, from, to);
        notifyItemMoved(from, to);
        Collections.swap(viewList,from,to);
    }

    public void onItemInsert(int position,String newData,String newText){
        mDataset.add(position,newData);
        mTextShow.add(position,newText);
        notifyItemInserted(position);

    }

    public void onItemInsert(String newData,String newText){
        mDataset.add(newData);
        mTextShow.add(newText);
        notifyItemInserted(mTextShow.size()-1);
    }



    @Override
    public boolean onLongClick(View v) {
        if(mOnItemLongClickListener != null){
            longClickPosition = viewList.indexOf(v);
            return this.mOnItemLongClickListener.onItemLongClick(v,viewList.indexOf(v));
        }
        return false;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if(mOnItemDragListener !=null && viewList.indexOf(v)==longClickPosition){
             return this.mOnItemDragListener.onItemDrag(v,event,viewList.indexOf(v));
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v,viewList.indexOf(v));
        }
    }


    public  class ViewHolder extends RecyclerView.ViewHolder{



        public TextView text;
        public View view;

        public ViewHolder(View v){
            super(v);

            text= (TextView) v.findViewById(R.id.text1);
            view = v;

        }
    }

    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public static interface OnItemLongClickListener {
        boolean onItemLongClick(View view , int position);
    }

    public static interface OnItemDragListener{
        boolean onItemDrag(View v, DragEvent event ,int position) ;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_item_1,parent,false);
        ViewHolder vh = new ViewHolder(v);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        v.setOnDragListener(this);


        return vh;
    }



    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mOnItemLongClickListener = listener;
    }

    public void setmOnItemDragListener(OnItemDragListener listener){
        this.mOnItemDragListener = listener;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.text.setText(mTextShow.get(position));

        viewList.add(position,holder.view);
    }



    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public String getQueryString(int position){
        return mDataset.get(position);
    }

    public String getText(int position){return mTextShow.get(position);}
}

