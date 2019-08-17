package com.example.assigment3;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button btn_strat, btn_stop, btn_reset, btn_event, capvideo_btn, contact_btn, search_btn;
    TextView txt_name, txt_ph;
    EditText search_ed;
    VideoView videoView;
    Chronometer cmTimer;

    Uri videoUri;

    Boolean resume = false;
    long elapsedTime;

    private static final int VIDEO_CAPTURE = 101;
    private static final int VIEW_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cmTimer = findViewById(R.id.cmtime);
        btn_strat = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_reset = findViewById(R.id.btn_reset);
        btn_event=findViewById(R.id.event);
        capvideo_btn=findViewById(R.id.capt_vd);
        contact_btn=findViewById(R.id.contact);
        search_btn=findViewById(R.id.search_btn);
        txt_ph = findViewById(R.id.ph_tv);
        search_ed = findViewById(R.id.sr_ed);


        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (!resume)
                {
                    elapsedTime= SystemClock.elapsedRealtime();
                }
                else
                {
                    elapsedTime = elapsedTime + 1000;
                }
            }
        });
        btn_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendarEvent = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", calendarEvent.getTimeInMillis());
                intent.putExtra("endTime", calendarEvent.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra("title", "Sample Event");
                intent.putExtra("allDay", true);
                intent.putExtra("rule", "FREQ=YEARLY");
                startActivity(intent);
            }
        });

        capvideo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takeVideoIntent, VIDEO_CAPTURE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "There is no camera on device", Toast.LENGTH_LONG).show();
                }
            }
        });

        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, VIEW_CONTACT);
            }
        });


        txt_ph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_ph = findViewById(R.id.ph_tv);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + txt_ph.getText().toString()));
                startActivity(intent);
            }
        });



        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
                intent.putExtra(SearchManager.QUERY, search_ed.getText().toString());
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_CAPTURE) {

            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
                playbackRecordedVideo();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show();
            }
        }

        else if (requestCode == VIEW_CONTACT && resultCode == RESULT_OK) {

            Uri contactData = data.getData();
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();

            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

            txt_name = findViewById(R.id.name_tv);
            txt_name.setText(name);

            txt_ph = findViewById(R.id.ph_tv);
            txt_ph.setText(number);
        }
    }

    //timer
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_start:
                btn_strat.setEnabled(false);
                btn_stop.setEnabled(true);

                if (!resume) {
                    cmTimer.setBase(SystemClock.elapsedRealtime());
                    cmTimer.start();
                } else {
                    cmTimer.start();
                }
                break;

            case R.id.btn_stop:
                btn_strat.setEnabled(true);
                btn_stop.setEnabled(false);
                cmTimer.stop();
                resume = true;
                btn_strat.setText("RESUME");
                break;

            case R.id.btn_reset:
                cmTimer.stop();
                cmTimer.setText("00:00");
                resume = false;
                btn_strat.setEnabled(true);
                btn_strat.setText("START");
                btn_stop.setEnabled(false);
                break;
        }
    }

    public void playbackRecordedVideo() {
        videoView = findViewById(R.id.videoview);
        videoView.setVideoURI(videoUri);
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.start();
    }
}
