package com.b2b.sampleb2b.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.b2b.sampleb2b.AppExecutors;
import com.b2b.sampleb2b.DataRepository;
import com.b2b.sampleb2b.MyTaskApp;
import com.b2b.sampleb2b.R;
import com.b2b.sampleb2b.adapters.FilterBottomDialogAdapter;
import com.b2b.sampleb2b.constants.AllConstants;
import com.b2b.sampleb2b.databinding.ActivityAddTaskBinding;
import com.b2b.sampleb2b.db.MyTaskDatabase;
import com.b2b.sampleb2b.db.entities.FolderEntity;
import com.b2b.sampleb2b.models.AddTaskDetails;
import com.b2b.sampleb2b.utils.ApplicationUtils;
import com.b2b.sampleb2b.receiver.TaskAlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 20/4/18.
 */

public class AddTaskActivity extends AppCompatActivity implements AllConstants, View.OnClickListener {

    private static final String TAG =      "AddTaskActivity";
    private BottomSheetBehavior             mBottomSheetBehavior;
    private List<String>                    listFilter;
    private int                             prorityType;
    private ActivityAddTaskBinding          taskBinding;
    private FilterBottomDialogAdapter       filterBottomDialogAdapter ;
    private FolderEntity   folderEntity   = null;
    private AddTaskDetails addTaskDetails = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskBinding = DataBindingUtil.setContentView(this,R.layout.activity_add_task);
        taskBinding.setLifecycleOwner(this);
        initializeResources();
    }

    private void initializeResources() {

        Bundle bundle = this.getIntent().getExtras();
        String title = "";
        if(bundle.containsKey(TITLE)){
            title = bundle.getString(TITLE);
            taskBinding.setTaskName(title);
        }
        if(bundle.containsKey(FOLDER_OBJ)){
            folderEntity = bundle.getParcelable(FOLDER_OBJ);
            if(folderEntity.getTaskDetails() != null && folderEntity.getTaskDetails().size() > 0){
                addTaskDetails = folderEntity.getTaskDetails().get(0);
                taskBinding.setTaskDetails(addTaskDetails);
            }
        }

        taskBinding.tvCancel.setOnClickListener(this);
        taskBinding.tvSave.setOnClickListener(this);
        taskBinding.txtMeetingDate.setOnClickListener(this);
        taskBinding.txtMeetingTime.setOnClickListener(this);
        taskBinding.imgClearTime.setOnClickListener(this);
        taskBinding.imgClearDate.setOnClickListener(this);
        taskBinding.priorityNone.setOnClickListener(this);
        taskBinding.priorityOne.setOnClickListener(this);
        taskBinding.priorityTwo.setOnClickListener(this);
        taskBinding.priorityThree.setOnClickListener(this);
        taskBinding.layoutRepeat.setOnClickListener(this);
        taskBinding.layoutMoveto.setOnClickListener(this);

        //Find bottom Sheet ID
       // View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(taskBinding.bottomSheet);

       //By default set BottomSheet Behavior as Collapsed and Height 0
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(0);

      //If you want to handle callback of Sheet Behavior you can use below code
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "State Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "State Dragging");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "State Expanded");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "State Hidden");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "State Settling");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        listFilter = new ArrayList<>();
        listFilter.add("Filter 1");
        listFilter.add("Filter 2");
        listFilter.add("Filter 3");
        listFilter.add("Filter 4");
        listFilter.add("Filter 5");
        listFilter.add("Filter 6");
        listFilter.add("Filter 1");
        listFilter.add("Filter 2");
        listFilter.add("Filter 3");
        listFilter.add("Filter 4");
        listFilter.add("Filter 5");
        listFilter.add("Filter 6");
        filterBottomDialogAdapter = new FilterBottomDialogAdapter(this, listFilter);
        taskBinding.incFilter.cmnRecView.recyclerview.setAdapter(filterBottomDialogAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_meeting_date:
                ApplicationUtils.showDatePicker(AddTaskActivity.this, taskBinding.txtMeetingDate);
                break;
            case R.id.txt_meeting_time:
                ApplicationUtils.showTimePicker(AddTaskActivity.this, taskBinding.txtMeetingTime, taskBinding.txtRepeatalarmDate);
                break;
            case R.id.img_clear_date:
                taskBinding.txtMeetingDate.setText(R.string.select_date);
                taskBinding.txtRepeatalarmDate.setVisibility(View.GONE);
                break;
            case R.id.img_clear_time:
                taskBinding.txtMeetingTime.setText(R.string.select_time);
                taskBinding.txtRepeatalarmDate.setVisibility(View.GONE);
                break;
            case R.id.priority_none:
                prorityType = 0;
                taskBinding.priorityNone.setTextColor(ContextCompat.getColor(this, R.color.white));
                taskBinding.priorityNone.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_left_rounded_corners_click));
                taskBinding.priorityOne.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityOne.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                taskBinding.priorityTwo.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityTwo.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                taskBinding.priorityThree.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityThree.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_right_rounded_corners));
                break;
            case R.id.priority_one:
                prorityType = 1;
                taskBinding.priorityOne.setTextColor(ContextCompat.getColor(this, R.color.white));
                taskBinding.priorityOne.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square_click));
                taskBinding.priorityNone.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityNone.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_left_rounded_corners));
                taskBinding.priorityTwo.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityTwo.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                taskBinding.priorityThree.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityThree.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_right_rounded_corners));
                break;
            case R.id.priority_two:
                prorityType = 2;
                taskBinding.priorityTwo.setTextColor(ContextCompat.getColor(this, R.color.white));
                taskBinding.priorityTwo.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square_click));
                taskBinding.priorityNone.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityNone.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_left_rounded_corners));
                taskBinding.priorityOne.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityOne.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                taskBinding.priorityThree.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityThree.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_right_rounded_corners));
                break;
            case R.id.priority_three:
                prorityType = 3;
                taskBinding.priorityThree.setTextColor(ContextCompat.getColor(this, R.color.white));
                taskBinding.priorityThree.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_right_rounded_corners_click));
                taskBinding.priorityNone.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityNone.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_left_rounded_corners));
                taskBinding.priorityOne.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityOne.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                taskBinding.priorityTwo.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
                taskBinding.priorityTwo.setBackground(ContextCompat.getDrawable(this, R.drawable.segment_square));
                break;
            case R.id.layout_repeat:
               taskBinding.incFilter.titleDialog.setText(R.string.select_repeat);
               taskBinding.incFilter.titleDialog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat, 0, 0, 0);
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    filterBottomDialogAdapter.notifyDataSetChanged();
                    //If state is in collapse mode expand it
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    //else if state is expanded collapse it
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
            case R.id.layout_moveto:
                taskBinding.incFilter.titleDialog.setText(R.string.select_move_to);
                taskBinding.incFilter.titleDialog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_folder_stroke, 0, 0, 0);
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    filterBottomDialogAdapter.notifyDataSetChanged();
                    //If state is in collapse mode expand it
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    //else if state is expanded collapse it
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;
            case R.id.tv_save:
                if (taskBinding.txtMeetingDate.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.select_date))) {
                    Toast.makeText(this, "Please Select Task Date", Toast.LENGTH_SHORT).show();
                } else if (taskBinding.txtMeetingTime.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.select_time))) {
                    Toast.makeText(this, "Enter Select Task Reminder Time", Toast.LENGTH_SHORT).show();
                }/* else if (taskBinding.txtRepeatMode.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.select_repeat_mode))) {
                    Toast.makeText(this, "Select task repeat mode", Toast.LENGTH_SHORT).show();
                }*/ else if (TextUtils.isEmpty(taskBinding.edtTaskNote.getText().toString().trim())) {
                    Toast.makeText(this, "Enter task note", Toast.LENGTH_SHORT).show();
                } else if (prorityType == 0) {
                    Toast.makeText(this, "Select prority type", Toast.LENGTH_SHORT).show();
                }else{
                    //start DB Transaction
                    addTaskDetails.setTaskDate(taskBinding.txtMeetingDate.getText().toString().trim());
                    addTaskDetails.setTaskTime(taskBinding.txtMeetingTime.getText().toString().trim());
                   // addTaskDetails.setTaskRepeatMode(taskBinding.txtRepeatMode.getText().toString().trim());
                    addTaskDetails.setTaskNote(taskBinding.edtTaskNote.getText().toString().trim()   );
                    addTaskDetails.setTaskPriority(String.valueOf(prorityType));
                    List<AddTaskDetails> taskDetailsList = new ArrayList<>();
                    taskDetailsList.add(addTaskDetails);
                    folderEntity.setTaskDetails(taskDetailsList);
                    AppExecutors appExecutors = new AppExecutors();
                    appExecutors.getExeDiskIO().execute(()->{
                        MyTaskDatabase database = ((MyTaskApp)getApplicationContext()).getDatabase();
                        int update = database.getFolderDao().updateFolderTask(folderEntity);
                        finish();
                    });
                }
                break;
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    private void setTaskAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(AddTaskActivity.this, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        Calendar alarmStartTime = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 2);
        alarmStartTime.set(Calendar.MINUTE, 13);
        alarmStartTime.set(Calendar.SECOND, 00);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(), 0, pendingIntent);
    }

    /**
     * Generate UUID for every alarmtask-->Save in DB --> get id from DB pass it while Notification generation-->
     * onClick notification get that ID and pass in RequestCode.
     */
    private void cancelTaskAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(AddTaskActivity.this, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }


}
