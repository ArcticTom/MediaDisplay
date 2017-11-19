package com.oneupsquad.mediadisplay;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.oneupsquad.mediadisplay.db.Contract;
import com.oneupsquad.mediadisplay.db.TaskDbHelper;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends AppCompatActivity {
    private boolean undo = false;
    private CaldroidFragment caldroidFragment;
    private CaldroidFragment dialogCaldroidFragment;
    private TaskDbHelper dbHelper;
    private ListView taskListView;
    private ArrayAdapter<Task> taskAdapter;
    private ArrayList<Boolean> checkboxes = null;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

		 caldroidFragment = new CustomCalendarFragment();

        // Setup arguments
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, false);
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, true);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
        args.putBoolean(CaldroidFragment.DISABLE_DATES, false);
        args.putBoolean(CaldroidFragment.SELECTED_DATES, false);
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.Calendar);

        caldroidFragment.setArguments(args);

        setCustomResourceForDates();

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        final WeatherFragment wf = new WeatherFragment();

        FragmentTransaction w = getSupportFragmentManager().beginTransaction();
        w.replace(R.id.weather_fragment, wf);
        w.commit();

        img = (ImageView) findViewById(R.id.img);

        File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/");
        File files[] = rootDir.listFiles();
        File image = files[new Random().nextInt(files.length)];

        if (image.toString() != null && image.toString().length() > 0) {
            Picasso.with(MainActivity.this).load(image).fit().into(img);
        } else {
            Toast.makeText(MainActivity.this, "Empty URI", Toast.LENGTH_SHORT).show();
        }

        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:'00:00Z'");
                String start = format.format(currentTime);
                String end = format.format(currentTime);
                wf.updateWeatherData(start, end);

                img = (ImageView) findViewById(R.id.img);

                File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/");
                File files[] = rootDir.listFiles();
                File image = files[new Random().nextInt(files.length)];

                if (image.toString() != null && image.toString().length() > 0) {
                    Picasso.with(MainActivity.this).load(image).fit().into(img);
                } else {
                    Toast.makeText(MainActivity.this, "Empty URI", Toast.LENGTH_SHORT).show();
                }
            }
        };

        timer.schedule(hourlyTask, 1000 * 60 * 60, 1000 * 60 * 60);

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                Toast.makeText(getApplicationContext(), formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
                    Toast.makeText(getApplicationContext(),
                            "Caldroid view is created", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);

        dbHelper = new TaskDbHelper(this);
        taskListView = (ListView) findViewById(R.id.task_list);

        updateTasks();

        final ImageButton addTask = (ImageButton) findViewById(R.id.task_add);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout dialogLayout = new LinearLayout(v.getContext());
                dialogLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams dialogLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                dialogLayout.setLayoutParams(dialogLayoutParam);

                final TextView taskText = new TextView(v.getContext());
                taskText.setText("Task");
                taskText.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary1));
                LinearLayout.LayoutParams TextLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                TextLayoutParam.setMargins(25, 10, 25, 0);
                taskText.setLayoutParams(TextLayoutParam);

                final EditText taskEditText = new EditText(v.getContext());
                LinearLayout.LayoutParams editTextLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                editTextLayoutParam.setMargins(25, 0, 25, 20);
                taskEditText.setLayoutParams(editTextLayoutParam);

                final TextView taskText2 = new TextView(v.getContext());
                taskText2.setText("Recurring task?");
                taskText2.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary1));
                LinearLayout.LayoutParams TextLayoutParam2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                TextLayoutParam2.setMargins(25, 0, 25, 10);
                taskText2.setLayoutParams(TextLayoutParam2);

                final Switch switchRecurring = new Switch(v.getContext());
                LinearLayout.LayoutParams switchLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                switchLayoutParam.setMargins(25, 0, 25, 0);
                switchLayoutParam.gravity = Gravity.LEFT;
                switchRecurring.setLayoutParams(switchLayoutParam);
                switchRecurring.setShowText(true);
                switchRecurring.setTextOn("Yes");
                switchRecurring.setTextOff("No");
                switchRecurring.setSwitchPadding(20);


                dialogLayout.addView(taskText);
                dialogLayout.addView(taskEditText);
                dialogLayout.addView(taskText2);
                dialogLayout.addView(switchRecurring);

                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("New task")
                        .setMessage("Please fill in the following fields.")
                        .setView(dialogLayout)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());

                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(Contract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(Contract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateTasks();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

       // final Button customizeButton = (Button) findViewById(R.id.customize_button);
/*
        // Customize the calendar
        customizeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (undo) {
                    customizeButton.setText("Customize");

                    // Reset calendar
                    caldroidFragment.clearDisableDates();
                    caldroidFragment.clearSelectedDates();
                    caldroidFragment.setMinDate(null);
                    caldroidFragment.setMaxDate(null);
                    caldroidFragment.setShowNavigationArrows(true);
                    caldroidFragment.setEnableSwipe(true);
                    caldroidFragment.refreshView();
                    undo = false;
                    return;
                }

                // Else
                undo = true;
                customizeButton.setText("Undo");
                Calendar cal = Calendar.getInstance();

                // Min date is last 7 days
                cal.add(Calendar.DATE, -7);
                Date minDate = cal.getTime();

                // Max date is next 7 days
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 14);
                Date maxDate = cal.getTime();

                // Set selected dates
                // From Date
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 2);
                Date fromDate = cal.getTime();

                // To Date
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 3);
                Date toDate = cal.getTime();

                // Set disabled dates
                ArrayList<Date> disabledDates = new ArrayList<Date>();
                for (int i = 5; i < 8; i++) {
                    cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, i);
                    disabledDates.add(cal.getTime());
                }

                // Customize
                caldroidFragment.setMinDate(minDate);
                caldroidFragment.setMaxDate(maxDate);
                caldroidFragment.setDisableDates(disabledDates);
                caldroidFragment.setSelectedDates(fromDate, toDate);
                caldroidFragment.setShowNavigationArrows(false);
                caldroidFragment.setEnableSwipe(false);

                caldroidFragment.refreshView();

                // Move to date
                // cal = Calendar.getInstance();
                // cal.add(Calendar.MONTH, 12);
                // caldroidFragment.moveToDate(cal.getTime());

                String text = "Today: " + formatter.format(new Date()) + "\n";
                text += "Min Date: " + formatter.format(minDate) + "\n";
                text += "Max Date: " + formatter.format(maxDate) + "\n";
                text += "Select From Date: " + formatter.format(fromDate)
                        + "\n";
                text += "Select To Date: " + formatter.format(toDate) + "\n";
                for (Date date : disabledDates) {
                    text += "Disabled Date: " + formatter.format(date) + "\n";
                }

            }
        });

        Button showDialogButton = (Button) findViewById(R.id.show_dialog_button);

        final Bundle state = savedInstanceState;
        showDialogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Setup caldroid to use as dialog
                dialogCaldroidFragment = new CaldroidFragment();
                dialogCaldroidFragment.setCaldroidListener(listener);

                // If activity is recovered from rotation
                final String dialogTag = "CALDROID_DIALOG_FRAGMENT";
                if (state != null) {
                    dialogCaldroidFragment.restoreDialogStatesFromKey(
                            getSupportFragmentManager(), state,
                            "DIALOG_CALDROID_SAVED_STATE", dialogTag);
                    Bundle args = dialogCaldroidFragment.getArguments();
                    if (args == null) {
                        args = new Bundle();
                        dialogCaldroidFragment.setArguments(args);
                    }
                } else {
                    // Setup arguments
                    Bundle bundle = new Bundle();
                    // Setup dialogTitle
                    dialogCaldroidFragment.setArguments(bundle);
                }

                dialogCaldroidFragment.show(getSupportFragmentManager(),
                        dialogTag);
            }
        });
        */

    }


    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();

        // Min date is last 7 days
        cal.add(Calendar.DATE, -3);
        Date blueDate = cal.getTime();

        // Max date is next 7 days
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        Date greenDate = cal.getTime();

        if (caldroidFragment != null) {
            caldroidFragment.setTextColorForDate(R.color.colorAccent, blueDate);
            caldroidFragment.setTextColorForDate(R.color.colorAccent, greenDate);
        }
    }

    private void updateTasks() {
        ArrayList<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Contract.TaskEntry.TABLE,
                new String[]{Contract.TaskEntry._ID, Contract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(Contract.TaskEntry.COL_TASK_TITLE);
            Task newtask = new Task(cursor.getString(idx), false);
            taskList.add(newtask);
        }

        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(this,
                    taskList);
            taskListView.setAdapter(taskAdapter);
        } else {
            taskAdapter.clear();
            taskAdapter.addAll(taskList);
            taskAdapter.notifyDataSetChanged();

        }

        cursor.close();
        db.close();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Contract.TaskEntry.TABLE,
                Contract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();


        Animation anim = AnimationUtils.loadAnimation(
                this, android.R.anim.slide_out_right
        );
        anim.setDuration(800);
        parent.startAnimation(anim);

        new Handler().postDelayed(new Runnable() {

            public void run() {

                updateTasks();
                taskAdapter.notifyDataSetChanged();

            }

        }, anim.getDuration());

        //updateTasks();

    }

}
