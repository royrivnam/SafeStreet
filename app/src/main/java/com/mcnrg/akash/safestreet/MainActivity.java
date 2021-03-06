package com.mcnrg.akash.safestreet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.support.v4.content.ContextCompat;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener {
    TextView vehicle1, vehicle2, vehicle3, vehicle4, position1, position2, position3, update1, update2, update3, update4, update5, update6;
    Button button, settings;
    LinearLayout vehicleL1, vehicleL2, vehicleL3, vehicleL4;
    CheckBox soundbox, wifibox;

    SensorManager sm;
    LocationManager lm;
    WaveRecorder wave;
    WifiDetector wifi;
    WifiManager wifiCheck;

    boolean updating = false;
    long prev = 0, prev2 = 0, prev3 = 0;
    long traveltime = 0;
    int level, scale;
    int adjustment;
    float battery;
    double bearing;
    double latitude, longitude, lastlatitude, lastlongitude, accuracy;
    double distance = 0;
    double bump_thres = 10.6, pot_thres = 4, lowlimit = 5, scaling = 5, baselimit = 5, finb, finp, speed;

    int events;

    private StorageReference storageReference;
    public static SharedPreferences shpref;
    public final static String ss = "fname";
    public final static String k2 = "b";

    RandomAccessFile raf;
    File root;
    Calendar calendar;
    SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-hh_mm_ss");



    Button m1,m2,m3,m4,m5;
    RandomAccessFile mantag;
    long manstamp;
    File root2;

    public static SharedPreferences shpref2;
    public final static String ss2 = "fname2";
    public final static String k3 = "b2";
    String register="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shpref2 = MainActivity.this.getSharedPreferences(ss2, Context.MODE_PRIVATE);
        register=shpref2.getString(k3,"");
        Toast.makeText(MainActivity.this,"Welcome "+register+"!",Toast.LENGTH_SHORT).show();
        final Dialog phone=new Dialog(MainActivity.this);
        phone.setContentView(R.layout.identity);
        phone.setTitle("Enter Phone Number!!");
        phone.setCancelable(false);
        final EditText phnno=(EditText)phone.findViewById(R.id.phnEntry);
        final Button entry=(Button)phone.findViewById(R.id.entrybutton);
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phnno.getText().toString().length()==10)
                {
                    register=phnno.getText().toString();
                    SharedPreferences.Editor editor2=shpref2.edit();
                    editor2.putString(k3,phnno.getText().toString());
                    editor2.commit();
                    phone.dismiss();
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Enter valid phone number!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        if(register.equals(""))
        phone.show();

        wifiCheck=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        vehicleL1 = (LinearLayout) findViewById(R.id.twoL);
        vehicleL2 = (LinearLayout) findViewById(R.id.threeL);
        vehicleL3 = (LinearLayout) findViewById(R.id.fourL);
        vehicleL4 = (LinearLayout) findViewById(R.id.sixL);
        vehicle1 = (TextView) findViewById(R.id.two);
        vehicle2 = (TextView) findViewById(R.id.three);
        vehicle3 = (TextView) findViewById(R.id.four);
        vehicle4 = (TextView) findViewById(R.id.six);
        position1 = (TextView) findViewById(R.id.pocket);
        position2 = (TextView) findViewById(R.id.mounter);
        position3 = (TextView) findViewById(R.id.dashboard);
        button = (Button) findViewById(R.id.button);
        update1 = (TextView) findViewById(R.id.text1);
        update2 = (TextView) findViewById(R.id.text2);
        update3 = (TextView) findViewById(R.id.text3);
        update4 = (TextView) findViewById(R.id.text4);
        update5 = (TextView) findViewById(R.id.text5);
        update6 = (TextView) findViewById(R.id.text6);
        settings = (Button) findViewById(R.id.setting);
        soundbox = (CheckBox) findViewById(R.id.sound);
        wifibox = (CheckBox) findViewById(R.id.wifi);


        m1=(Button)findViewById(R.id.mb1);
        m2=(Button)findViewById(R.id.mp1);
        m3=(Button)findViewById(R.id.mj1);
        m4=(Button)findViewById(R.id.mn1);
        m5=(Button)findViewById(R.id.mc1);

        m1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(button.getText().toString().equals("STOP")) {
                    String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                    root2 = new File(path2);
                    if (!root2.exists()) {
                        root2.mkdirs();
                    }
                    try {
                        mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        mantag.seek(mantag.length());
                        mantag.write(("B " + manstamp + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Cannot tag now!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(button.getText().toString().equals("STOP")) {
                    String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                    root2 = new File(path2);
                    if (!root2.exists()) {
                        root2.mkdirs();
                    }
                    try {
                        mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        mantag.seek(mantag.length());
                        mantag.write(("P " + manstamp + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Cannot tag now!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(button.getText().toString().equals("STOP")) {
                    String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                    root2 = new File(path2);
                    if (!root2.exists()) {
                        root2.mkdirs();
                    }
                    try {
                        mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        mantag.seek(mantag.length());
                        mantag.write(("J " + manstamp + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Cannot tag now!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        m4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(button.getText().toString().equals("STOP")) {
                    String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                    root2 = new File(path2);
                    if (!root2.exists()) {
                        root2.mkdirs();
                    }
                    try {
                        mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        mantag.seek(mantag.length());
                        mantag.write(("N " + manstamp + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Cannot tag now!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        m5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(button.getText().toString().equals("STOP")) {
                    String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                    root2 = new File(path2);
                    if (!root2.exists()) {
                        root2.mkdirs();
                    }
                    try {
                        mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        mantag.seek(mantag.length());
                        mantag.write(("C " + manstamp + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Cannot tag now!!",Toast.LENGTH_SHORT).show();
                }
            }
        });


        //soundbox.setChecked(true);
        soundbox.setEnabled(false);
        soundbox.setVisibility(View.GONE);
        wifibox.setChecked(true);
        Constants.vehiclestoreL = vehicleL1;
        Constants.vehiclestore = vehicle1;
        Constants.vehicle = "TWO_WHEELER";
        vehicle1.setClickable(false);
        vehicle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.vehiclestoreL.setBackgroundResource(R.drawable.border_blue);
                Constants.vehiclestore.setClickable(true);
                vehicleL1.setBackgroundResource(R.drawable.border_green);
                vehicle1.setClickable(false);
                Constants.vehiclestoreL = vehicleL1;
                Constants.vehiclestore = vehicle1;
                Constants.vehicle = "TWO_WHEELER";

            }
        });
        vehicle2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.vehiclestoreL.setBackgroundResource(R.drawable.border_blue);
                Constants.vehiclestore.setClickable(true);
                vehicleL2.setBackgroundResource(R.drawable.border_green);
                vehicle2.setClickable(false);
                Constants.vehiclestoreL = vehicleL2;
                Constants.vehiclestore = vehicle2;
                Constants.vehicle = "THREE_WHEELER";
            }
        });
        vehicle3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.vehiclestoreL.setBackgroundResource(R.drawable.border_blue);
                Constants.vehiclestore.setClickable(true);
                vehicleL3.setBackgroundResource(R.drawable.border_green);
                vehicle3.setClickable(false);
                Constants.vehiclestoreL = vehicleL3;
                Constants.vehiclestore = vehicle3;
                Constants.vehicle = "FOUR_WHEELER";
            }
        });
        vehicle4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.vehiclestoreL.setBackgroundResource(R.drawable.border_blue);
                Constants.vehiclestore.setClickable(true);
                vehicleL4.setBackgroundResource(R.drawable.border_green);
                vehicle4.setClickable(false);
                Constants.vehiclestoreL = vehicleL4;
                Constants.vehiclestore = vehicle4;
                Constants.vehicle = "SIX_WHEELER";
            }
        });
        position1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position1.setBackgroundResource(R.drawable.button_green);
                position1.setClickable(false);
                position2.setBackgroundResource(R.drawable.border_blue);
                position2.setClickable(true);
                position3.setBackgroundResource(R.drawable.border_blue);
                position3.setClickable(true);
                Constants.position = "POCKET";
            }
        });
        position2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position2.setBackgroundResource(R.drawable.button_green);
                position2.setClickable(false);
                position1.setBackgroundResource(R.drawable.border_blue);
                position1.setClickable(true);
                position3.setBackgroundResource(R.drawable.border_blue);
                position3.setClickable(true);
                Constants.position = "MOUNTER";
            }
        });
        position3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position3.setBackgroundResource(R.drawable.button_green);
                position3.setClickable(false);
                position1.setBackgroundResource(R.drawable.border_blue);
                position1.setClickable(true);
                position2.setBackgroundResource(R.drawable.border_blue);
                position2.setClickable(true);
                Constants.position = "DASHBOARD";
            }
        });
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        storageReference = FirebaseStorage.getInstance().getReference();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0x1);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0x1);
                /*if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                    askForPermission(Manifest.permission.RECORD_AUDIO, 0x1);*/
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
                    askForPermission(Manifest.permission.CHANGE_WIFI_STATE, 0x1);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (!(Constants.position.equals(""))) {
                            updating = !updating;
                            if (updating) {
                                Intent batteryStatus = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                                scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                battery = level / (float) scale;
                                sm.registerListener((SensorEventListener) MainActivity.this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
                                sm.registerListener((SensorEventListener) MainActivity.this, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
                                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
                                calendar = Calendar.getInstance();
                                Constants.starttime = String.valueOf(formatter.format(calendar.getTime()));
                                String path = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                                root = new File(path);
                                if (!root.exists()) {
                                    root.mkdirs();
                                }
                                try {
                                    raf = new RandomAccessFile(root + "/All_Details.txt", "rw");
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                String whole = "bump:" + String.valueOf((float) bump_thres) + "," + "pothole:" + String.valueOf((float) pot_thres) + "," + "basespeed:" + String.valueOf((float) baselimit) + "," + "lowerspeed:" + String.valueOf((float) lowlimit) + "," + "scaling:" + String.valueOf((float) scaling) + "\n" + "x,y,z,latitude,longitude,bearing,accuracy,light,timestamp" + "\n";
                                try {
                                    raf.seek(raf.length());
                                    raf.write(whole.getBytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                traveltime = SystemClock.elapsedRealtime();
                                vehicle1.setClickable(false);
                                vehicle2.setClickable(false);
                                vehicle3.setClickable(false);
                                vehicle4.setClickable(false);
                                position1.setClickable(false);
                                position2.setClickable(false);
                                position3.setClickable(false);
                                settings.setClickable(false);
                                settings.setVisibility(View.INVISIBLE);
                                button.setText("STOP");
                                button.setTextColor(Color.parseColor("#FFFFFFFF"));
                                button.setBackgroundResource(R.drawable.button_red);

                                /*if (soundbox.isChecked() == true && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    wave = new WaveRecorder(path + "/SoundRecord.wav");
                                    wave.startRecording();
                                } else {
                                    soundbox.setChecked(false);
                                    Toast.makeText(MainActivity.this, "Permission is required to use \"SOUND\" feature. Go to Settings->Apps->SafeStreet->Permissions->Microphone->On", Toast.LENGTH_SHORT).show();
                                }
                                soundbox.setEnabled(false);*/

                                if (wifibox.isChecked() == true && wifiCheck.isWifiEnabled())
                                {
                                    wifi=new WifiDetector(path+"/WifiRecord.txt",getApplicationContext());
                                    wifi.startDetecting();
                                }
                                else
                                {
                                    wifibox.setChecked(false);
                                    Toast.makeText(MainActivity.this,"Turn on wifi to use the wifi \"WIFI\" feature",Toast.LENGTH_SHORT).show();
                                }
                                wifibox.setEnabled(false);

                            } else {
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setTitle("Alert!!");
                                alert.setMessage("Do you really want to stop?");
                                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sm.unregisterListener((SensorEventListener) MainActivity.this);
                                        try {
                                            raf.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        /*if (soundbox.isChecked() == true && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            wave.stopRecording();
                                        }
                                        soundbox.setEnabled(true);*/

                                        if (wifibox.isChecked() == true && wifiCheck.isWifiEnabled()) {
                                            wifi.stopDetecting();
                                        }
                                        wifibox.setEnabled(true);
                                        TotalDistance td=new TotalDistance(Environment.getExternalStorageDirectory() + "/SafeStreet/Total_distance_in_kms.txt",Double.parseDouble(getDistance(distance)));
                                        td.calculate();

                                        adjustment = 0;
                                        prev = -900000;
                                        prev2 = -900000;
                                        prev3 = -900000;
                                        latitude = 0;
                                        longitude = 0;
                                        lastlatitude = 0;
                                        lastlongitude = 0;
                                        distance = 0;
                                        accuracy = 10000;
                                        speed = 0;
                                        events = 0;
                                        update3.setText(String.valueOf(events));
                                        vehicle1.setClickable(true);
                                        vehicle2.setClickable(true);
                                        vehicle3.setClickable(true);
                                        vehicle4.setClickable(true);
                                        position1.setClickable(true);
                                        position2.setClickable(true);
                                        position3.setClickable(true);
                                        settings.setClickable(true);
                                        settings.setVisibility(View.VISIBLE);
                                        button.setText("START");
                                        button.setTextColor(Color.parseColor("#FF000000"));
                                        button.setBackgroundResource(R.drawable.button_green);

                                        final Dialog rating=new Dialog(MainActivity.this);
                                        rating.setContentView(R.layout.rating);
                                        rating.setTitle("RATE THE DRIVER!!");
                                        rating.setCancelable(false);
                                        final EditText ratingValue=(EditText)rating.findViewById(R.id.ratingEntry);
                                        final Button submit=(Button)rating.findViewById(R.id.submitbutton);
                                        submit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if(!(ratingValue.getText().toString().trim().equals(""))) {
                                                    if (Integer.parseInt(ratingValue.getText().toString()) <= 5 && Integer.parseInt(ratingValue.getText().toString()) > 0) {
                                                        String path2 = Environment.getExternalStorageDirectory() + "/SafeStreet/" + Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/";
                                                        root2 = new File(path2);
                                                        if (!root2.exists()) {
                                                            root2.mkdirs();
                                                        }
                                                        try {
                                                            mantag = new RandomAccessFile(root + "/ManualTagging.txt", "rw");
                                                        } catch (FileNotFoundException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            mantag.seek(mantag.length());
                                                            mantag.write(("Driver Rating " + ratingValue.getText().toString() + " out of 5").getBytes());
                                                            mantag.close();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        rating.dismiss();
                                                        uploadFile("All_Details.txt", "first");
                                                        uploadFile("WifiRecord.txt", "second");
                                                        uploadFile("ManualTagging.txt", "third");
                                                        //uploadFile("SoundRecord.wav","fourth");
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Wrong Value Entered", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                else {
                                                    Toast.makeText(MainActivity.this, "Please enter a value between 0 to 5", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        rating.show();
                                    }
                                });
                                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        updating = true;
                                    }
                                });
                                alert.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Position of the phone required", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Location should be turned On.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Storage permission required. Go to Settings->Apps->SafeStreet->Permissions->Storage->On", Toast.LENGTH_SHORT).show();
                }

            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("ALERT!!");
                adb.setMessage("This section is for advanced users for calibrating. Do you still want to continue?");
                adb.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        shpref = MainActivity.this.getSharedPreferences(ss, Context.MODE_PRIVATE);
                        final Dialog thres = new Dialog(MainActivity.this);
                        thres.setContentView(R.layout.threshold);
                        String mount[] = (shpref.getString(k2, " # # # # ")).split("#");
                        final EditText bump = (EditText) thres.findViewById(R.id.bump_thres);
                        if (!mount[0].equals(" ")) {
                            bump.setText(mount[0]);
                        }
                        final EditText pot = (EditText) thres.findViewById(R.id.pot_thres);
                        if (!mount[1].equals(" ")) {
                            pot.setText(mount[1]);
                        }
                        final EditText low1 = (EditText) thres.findViewById(R.id.low);
                        if (!mount[2].equals(" ")) {
                            low1.setText(mount[2]);
                        }
                        final EditText scale1 = (EditText) thres.findViewById(R.id.scale);
                        if (!mount[3].equals(" ")) {
                            scale1.setText(mount[3]);
                        }
                        final EditText base1 = (EditText) thres.findViewById(R.id.base);
                        if (!mount[4].equals(" ")) {
                            base1.setText(mount[4]);
                        }
                        final Button confirmm = (Button) thres.findViewById(R.id.okk);
                        final Button done = (Button) thres.findViewById(R.id.done);
                        confirmm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!(bump.getText().toString().trim().equals("")) && !(pot.getText().toString().trim().equals("")) && !(low1.getText().toString().trim().equals("")) && !(scale1.getText().toString().trim().equals("")) && !(base1.getText().toString().trim().equals(""))) {
                                    bump_thres = Double.parseDouble(bump.getText().toString().trim());
                                    pot_thres = Double.parseDouble(pot.getText().toString().trim());
                                    lowlimit = Double.parseDouble(low1.getText().toString().trim());
                                    scaling = Double.parseDouble(scale1.getText().toString().trim());
                                    baselimit = Double.parseDouble(base1.getText().toString().trim());
                                    Toast.makeText(MainActivity.this, "bumper: " + String.valueOf(bump_thres) + "/pothole: " + String.valueOf(pot_thres), Toast.LENGTH_SHORT).show();
                                    thres.dismiss();
                                } else {
                                    Toast.makeText(MainActivity.this, "Fields cannot be null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!(bump.getText().toString().trim().equals("")) && !(pot.getText().toString().trim().equals("")) && !(low1.getText().toString().trim().equals("")) && !(scale1.getText().toString().trim().equals("")) && !(base1.getText().toString().trim().equals(""))) {
                                    SharedPreferences.Editor editor = shpref.edit();
                                    bump_thres = Double.parseDouble(bump.getText().toString().trim());
                                    pot_thres = Double.parseDouble(pot.getText().toString().trim());
                                    lowlimit = Double.parseDouble(low1.getText().toString().trim());
                                    scaling = Double.parseDouble(scale1.getText().toString().trim());
                                    baselimit = Double.parseDouble(base1.getText().toString().trim());
                                    editor.putString(k2, String.valueOf(bump_thres) + "#" + String.valueOf(pot_thres) + "#" + String.valueOf(lowlimit) + "#" + String.valueOf(scaling) + "#" + String.valueOf(baselimit));
                                    editor.commit();
                                    Toast.makeText(MainActivity.this, "bumper: " + String.valueOf(bump_thres) + "/pothole: " + String.valueOf(pot_thres), Toast.LENGTH_SHORT).show();
                                    thres.dismiss();
                                } else {
                                    Toast.makeText(MainActivity.this, "Fields cannot be null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        thres.show();
                    }
                });
                adb.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                adb.show();
            }
        });
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        }
    }

    float[] acceleration = new float[3];
    float light;
    double[] ori = new double[2];
    double acc_final[] = new double[3];
    double cosa, sina, cosb, sinb;
    String filesize;
    char flag;

    @Override
    public void onSensorChanged(SensorEvent event) {
        flag = '0';
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, acceleration,
                        0, acceleration.length);
                break;

            case Sensor.TYPE_LIGHT:
                light = event.values[0];
                break;
        }
        long timestamp = SystemClock.elapsedRealtime();
        manstamp=timestamp;


        float sum = (float) Math.sqrt((acceleration[0] * acceleration[0]) + (acceleration[1] * acceleration[1]) + (acceleration[2] * acceleration[2]));
        if ((timestamp - prev) > 850000) {
            adjustment = 0;
            prev = timestamp;
        }
        if (Math.abs(sum - 9.80) <= 0.4 && adjustment <= 10 && (timestamp - prev) <= 1000) {
            adjustment++;
            ori[0] = Math.atan2(acceleration[1], acceleration[2]);
            ori[1] = Math.atan2(-1 * acceleration[0], Math.sqrt((acceleration[1] * acceleration[1]) + (acceleration[2] * acceleration[2])));
            cosa = Math.cos(ori[0]);
            sina = Math.sin(ori[0]);
            cosb = Math.cos(ori[1]);
            sinb = Math.sin(ori[1]);
        }
        acc_final[0] = (cosb * acceleration[0]) + (sinb * sina * acceleration[1]) + (cosa * sinb * acceleration[2]);
        acc_final[1] = (cosa * acceleration[1]) - (sina * acceleration[2]);
        acc_final[2] = (-1 * sinb * acceleration[0]) + (cosb * sina * acceleration[1]) + (cosb * cosa * acceleration[2]);

        if (lastlatitude != 0 && lastlongitude != 0 && latitude != 0 && longitude != 0) {
            Location location1 = new Location(""), location2 = new Location("");
            location1.setLatitude(latitude);
            location1.setLongitude(longitude);
            location2.setLatitude(lastlatitude);
            location2.setLongitude(lastlongitude);
            if (location2.distanceTo(location1) > 1) {
                bearing = location2.bearingTo(location1);
                if (latitude != lastlatitude || longitude != lastlongitude) {
                    distance = distance + location2.distanceTo(location1);
                }
                lastlatitude = latitude;
                lastlongitude = longitude;
            }

        } else {
            lastlatitude = latitude;
            lastlongitude = longitude;
        }
        if ((speed * 3.6) < baselimit) {
            finb = (bump_thres);
            finp = -1 * (pot_thres);
        } else {
            finb = bump_thres + (((speed * 3.6) - lowlimit) * (scaling / 10));
            finp = -1 * (pot_thres + (((speed * 3.6) - lowlimit) * (scaling / 10)));
        }
        if ((acc_final[2] > finb || acc_final[2] < finp) && (timestamp - prev3) > 2000) {
            events++;
            update3.setText(String.valueOf(events));
            prev3 = timestamp;
            flag = '1';
        }
        String whole = String.valueOf((float) acc_final[0]) + "," + String.valueOf((float) acc_final[1]) + "," + String.valueOf((float) acc_final[2]) + "," + String.valueOf(latitude) + "," + String.valueOf(longitude) + "," + String.valueOf(bearing) + "," + String.valueOf((float) accuracy) + "," + String.valueOf(light) + "," + String.valueOf(timestamp) + "," + String.valueOf(flag) + "\n";
        try {
            raf.seek(raf.length());
            raf.write(whole.getBytes());
            filesize = getfilesize(raf.length());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ((timestamp - prev2) > 10) {
            update1.setText(getDistance(distance) + " km.");
            update2.setText(getTraveltime(timestamp));
            update4.setText(filesize + " Mb");
            update5.setText(getBatteryconsumed(battery) + " %");
            update6.setText(getSpeed(speed) + " km/hr");
            prev2 = timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        speed = location.getSpeed();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    String getTraveltime(long timestamp) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        if (((timestamp - traveltime) / (1000.0 * 60.0)) > 100) {
            return df.format((timestamp - traveltime) / (1000.0 * 60.0 * 60)) + " hours";
        }
        return df.format((timestamp - traveltime) / (1000.0 * 60.0)) + " minutes";
    }

    String getfilesize(long size) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        return df.format((double) size / (1024.0 * 1024.0));
    }

    String getBatteryconsumed(double battery) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return df.format((battery - (level / (float) scale)) * 100);
    }

    String getDistance(double distance) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        return df.format(distance / 1000.0);
    }

    String getSpeed(double speed) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        return df.format(speed * 3.6);
    }

    private void uploadFile(String filename, String index) {
        final String path = Constants.vehicle + "_" + Constants.position + "_" + Constants.starttime + "_" + Constants.model + "/" + filename;
        Uri filePath = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/SafeStreet/" + path));
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading "+index+" file...");
            progressDialog.show();
            String path2=register+"_"+path;
            StorageReference riversRef = storageReference.child(path2);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File uploaded to the server successfully", Toast.LENGTH_LONG).show();
                            File file=new File(Environment.getExternalStorageDirectory() + "/SafeStreet/" + path);
                            try{
                                file.delete();
                            }
                            catch(Exception e){e.printStackTrace();}
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            String linebar = "";
                            int i = 0;
                            for (; i < (int) progress / 10; i++)
                                linebar = linebar + "-";
                            for (; i < 10; i++)
                                linebar = linebar + " ";
                            progressDialog.setMessage("Uploaded" + linebar + ((int) progress) + "%");
                        }
                    });
        } else {
        }
    }
}