package com.renai.firedepartment.keelong.hydrantmap;

/*import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.api.services.sheets.v4.*;

public class testView extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view);

    }
}*/

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.*;

import com.google.api.services.sheets.v4.model.*;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.IndicatorSeekBarType;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class testView extends AppCompatActivity
        {
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView messege;
    private Button buttonRead;
    private Button buttonWrite;
    private Button buttonBRead;
    private Button buttonBWrite;
    private EditText editTextID;
    private EditText editTextStates;
    private EditText editTextType;
    private EditText editTextMarked;
    private EditText editTextMarkedStates;
    private EditText editTextLat;
    private EditText editTextAddress;
    private EditText editTextArea;
    private EditText editTextLng;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int ACTION_READ = 1;
    static final int ACTION_WRITE = 2;
    static final int ACTION_BATCH_READ = 3;
    static final int ACTION_BATCH_WRITE = 4;

    private SQLiteDatabase db;
    private int mAction;


    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    IndicatorSeekBar test;
    DiscreteSeekBar test2;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_test2);


        test = (IndicatorSeekBar)findViewById(R.id.indicatorSeekBar);
        test2=(DiscreteSeekBar)findViewById(R.id.discreteseekbar);


        View testContent = LayoutInflater.from(this).inflate(R.layout.nav_header_main2,null);
        final ImageView imageView = testContent.findViewById(R.id.imageView);
        test.setCustomIndicator(testContent);
        test.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                switch (progress){
                    case 1:
                        imageView.setImageResource(R.drawable.btn_query);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.button_cancel);
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.button_concern);
                        break;
                }
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String tickBelowText, boolean fromUserTouch) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        

        //messege = (TextView)findViewById(R.id.textViewMessge);

        /*buttonBRead = (Button)findViewById(R.id.buttonBatchRead);
        buttonBWrite = (Button)findViewById(R.id.buttonBatchWrite);
        buttonRead = (Button)findViewById(R.id.buttonRead);
        buttonWrite = (Button)findViewById(R.id.buttonWrite);
        editTextID= (EditText)findViewById(R.id.editTextID);
        editTextStates = (EditText)findViewById(R.id.editTextStates);
        editTextType = (EditText)findViewById(R.id.editTextType);
        editTextMarked = (EditText)findViewById(R.id.editTextMarked);
        editTextMarkedStates = (EditText)findViewById(R.id.editTextMarkedStates);
        editTextLat = (EditText)findViewById(R.id.editTextLat);
        editTextAddress = (EditText)findViewById(R.id.editTextAddress);
        editTextArea = (EditText)findViewById(R.id.editTextArea);
        editTextLng = (EditText)findViewById(R.id.editTextLng);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        db = new MyDBHelper(this).getWritableDatabase();


        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonWrite.setEnabled(false);
                messege.setText("");
                getResultsFromApi(mAction =ACTION_WRITE);
                buttonWrite.setEnabled(true);
            }
        });

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRead.setEnabled(false);
                messege.setText("");
                getResultsFromApi(mAction = ACTION_READ);
                buttonRead.setEnabled(true);
            }
        });

        buttonBWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBWrite.setEnabled(false);
                messege.setText("");
                buttonBWrite.setEnabled(true);
            }
        });

        buttonBRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBRead.setEnabled(false);
                messege.setText("");

                String[] ss = {"*"};
                Cursor c =db.query(MyDBHelper.TABLE_HYDRANT,ss,null,null,null,null,null);
                c.moveToFirst();
                int length =c.getColumnNames().length;
                String s = "";
                do{

                    for(int i =0;i<length;i++){
                        s = s + c.getString(i) + " ";
                    }
                    s = s+"\n";

                }while (c.moveToNext());

                messege.setText(s);
                buttonBRead.setEnabled(true);
            }
        });

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

*/
    }



    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */

}
