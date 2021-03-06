package com.baldwin.clockpuncher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity implements OnClickListener {

    Button punchIn, punchOut, viewDb;
    ListView lvEntries;
    int iAmountToDisplay = 7;
    Calendar calHelperIn, calHelperOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        punchIn = (Button) findViewById(R.id.bPunchIn);
        punchOut = (Button) findViewById(R.id.bPunchOut);
        viewDb = (Button) findViewById(R.id.bViewDb);
        lvEntries = (ListView) findViewById(R.id.lvMainList);

        populateListView();

        punchIn.setOnClickListener(this);
        punchOut.setOnClickListener(this);

    }

    private void populateListView(){
        //TODO Does not display correct amount with a new database

        String data;
        long lTimeIn;
        long lTimeOut;
        String sTimeIn;
        String sTimeOut;
        long lTotalTime;
        long lTotalHours;

        ArrayList<String> alId = new ArrayList<String>();
        ArrayList<String> alTimeIn = new ArrayList<String>();
        ArrayList<String> alTimeOut = new ArrayList<String>();
        ArrayList<String> alTotal = new ArrayList<String>();

        Calendar calHelptimeIn = Calendar.getInstance();
        Calendar calHelptimeOut = Calendar.getInstance();

        ClockDbAdapter dbAdapter = new ClockDbAdapter(this);

        //Retrieve all the data in the database
        dbAdapter.open();
        data = dbAdapter.getAllData();
        dbAdapter.close();

        //Split by new line into Entries array as Strings.
        String[] Entries = data.split("\n");

        //If length of the array is less then the amount to display.
        if(Entries.length < iAmountToDisplay){
            iAmountToDisplay = Entries.length;
        }


        //Extract the data convert to a date format and set the Views.
        for (int i = Entries.length - iAmountToDisplay; i < Entries.length; i++) {

            String[] sRow = Entries[i].split(" ");
            if (sRow.length == 3) {

                alId.add(sRow[0]);
                lTimeIn = Long.parseLong(sRow[1]);
                lTimeOut = Long.parseLong(sRow[2]);
                lTotalTime = lTimeOut - lTimeIn;

                calHelptimeIn.setTimeInMillis(lTimeIn);
                calHelptimeOut.setTimeInMillis(lTimeOut);
                sTimeIn = calHelptimeIn.getTime().toString().substring(4, 19);
                sTimeOut = calHelptimeOut.getTime().toString().substring(4, 19);

                //If time out has the same month and day as sTimeIn then remove duplicate info
                if(sTimeIn.substring(0,5).equals(sTimeOut.substring(0,5))){
                    sTimeOut = sTimeOut.substring(6);
                }else if(sTimeIn.substring(0,3).equals(sTimeOut.substring(0,3))) {
                    sTimeOut = sTimeOut.substring(4);
                }
                alTimeIn.add(sTimeIn);

                if(lTimeOut > 0) {
                    alTimeOut.add(sTimeOut);

                    lTotalTime = lTotalTime / 1000 / 60;
                    if (lTotalTime > 60) {

                        lTotalHours = lTotalTime / 60;
                        lTotalTime = lTotalTime % 60;

                        alTotal.add( String.valueOf(lTotalHours) + ":"
                                + String.valueOf(lTotalTime));

                    } else {

                        alTotal.add(String.valueOf(lTotalTime) + " Mins");
                    }
                }else{
                    //There is no time out.
                    alTotal.add("");
                    alTimeOut.add("Still Clocked in");

                }

            }//end if
            Log.d("populate alTimein.length=", ""+ alTimeIn.size());

        }//end for

        Collections.reverse(alId);
        Collections.reverse(alTimeIn);
        Collections.reverse(alTimeOut);
        Collections.reverse(alTotal);
        //Convert all the array lists to arrays
        String aId[];
        String aTimeIn[];
        String aTimeOut[];
        String aTotal[];

        if(alId.isEmpty()){
            aId = new String[0];
            aTimeIn = new String[0];
            aTimeOut = new String[0];
            aTotal = new String[0];
        }else{
            aId = new String[alId.size()];
            alId.toArray(aId);

            aTimeIn = new String[alTimeIn.size()];
            alTimeIn.toArray(aTimeIn);

            aTimeOut = new String[alTimeOut.size()];
            alTimeOut.toArray(aTimeOut);

            aTotal = new String[alTotal.size()];
            alTotal.toArray(aTotal);
        }

        //Pass the arrays to ListViewAdapter
        ListViewAdapter lvAdapter = new ListViewAdapter(this, aId, aTimeIn,aTimeOut,aTotal);
        lvEntries.setAdapter(lvAdapter);

    }
    @Override
    public void onClick(View v) {

        ClockDbAdapter db = new ClockDbAdapter(MainActivity.this);

        db.open();
        long lTimeOut = db.lastTimeOut();
        long lTimeIn = db.lastTimeIn();
        db.close();

        switch (v.getId()) {

            case R.id.bPunchIn:

                if (lTimeOut > 0 || lTimeIn == -1) {
                    calHelperIn = Calendar.getInstance();

                    db.open();
                    db.createEntryTimeIn(Long.toString(calHelperIn.getTimeInMillis()));
                    db.close();

                } else {
                    Toast.makeText(this, "You have not punched out", 2).show();

                }

                break;

            case R.id.bPunchOut:

                calHelperOut = Calendar.getInstance();

                Log.d("punchOut lTimeIn", "" + lTimeIn);
                Log.d("punchOut lTimeOut", "" + lTimeOut);

                db.close();
                if (lTimeIn < 0 || lTimeOut > 0) {
                    Toast.makeText(this, "You have not punched in", 2).show();
                } else {

                    //Set strings up to be sent to database.
                    lTimeOut = calHelperOut.getTimeInMillis();
                    String strTimeOut = Long.toString(lTimeOut);

                    //create database db
                    db.open();
                    db.createEntryTimeOut(strTimeOut);
                    db.close();

                }

                break;
        }
        populateListView();

    }

    //Open the activity ViewDb
    public void viewDb(View v) {
        /*
        Intent i = new Intent(this, ViewDb.class);
        startActivity(i);
        */

        //Switch between showing 7 rows too all rows.
        if (iAmountToDisplay == 7) {
            iAmountToDisplay = 255;
            viewDb.setText("View Less");
        } else {
            iAmountToDisplay = 7;
            viewDb.setText("View More");
        }lvEntries.setFadingEdgeLength(15);

        populateListView();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        populateListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


}
