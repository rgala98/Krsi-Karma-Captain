package com.krsikarma.captain.Activities;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.DefaultTextConfig;
import com.krsikarma.captain.Utility.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EarningsActivity extends AppCompatActivity {

    public static final String TAG = "EarningsActivity";
    ImageView img_back;

    TabLayout earnings_tab_layout;
    TextView tv_earnings;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    Double total_earnings, today_earnings,week_earnings, month_earnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(EarningsActivity.this);
        setContentView(R.layout.activity_earnings);

        init();

        img_back.setOnClickListener(view -> onBackPressed());


        earnings_tab_layout.addTab(earnings_tab_layout.newTab().setText(getString(R.string.today)));
        earnings_tab_layout.addTab(earnings_tab_layout.newTab().setText(getString(R.string.this_week)));
        earnings_tab_layout.addTab(earnings_tab_layout.newTab().setText(getString(R.string.this_month)));
        earnings_tab_layout.addTab(earnings_tab_layout.newTab().setText(getString(R.string.overall)));
        getData();


        startCountAnimation(tv_earnings, 0);




        // There are 4 status in total - "Received", "Cancelled", "Rejected", "Pending"
        // For images there are three ic_received, ic_cancelled, ic_pending
        // Change the color of the status_text and amount accroding to status
        // User cannot request for more if one req is pending
        // Cancel button shows only if status is pending



        earnings_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "onTabSelected: " + tab.getPosition());

                switch (tab.getPosition()){
                    case 0: // TODAY
                        String today = String.valueOf(today_earnings).substring(0,String.valueOf(today_earnings).length() -2 );
                        startCountAnimation(tv_earnings, Integer.parseInt(today));
                            break;
                    case 1: // THIS WEEK
                        String week = String.valueOf(week_earnings).substring(0,String.valueOf(week_earnings).length() -2 );
                        startCountAnimation(tv_earnings, Integer.parseInt(week));
                        break;
                    case 2: // THIS MONTH
                        String month = String.valueOf(month_earnings).substring(0,String.valueOf(month_earnings).length() -2 );
                        startCountAnimation(tv_earnings, Integer.parseInt(month));
                        break;
                    case 3: // OVERALL
                        String overall = String.valueOf(total_earnings).substring(0,String.valueOf(total_earnings).length() -2 );
                        startCountAnimation(tv_earnings, Integer.parseInt(overall));
                        break;


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        earnings_tab_layout = (TabLayout) findViewById(R.id.earnings_tab_layout);
        tv_earnings = (TextView) findViewById(R.id.tv_earnings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

    }

    private void getData(){
        listenerRegistration = db.collection(getString(R.string.partners))
                .document(firebaseUser.getUid())
                .collection(getString(R.string.accounts))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            Log.e(TAG, "error : ", error);
                            return;
                        }

                        total_earnings = 0.0;
                        today_earnings = 0.0;
                        week_earnings = 0.0;
                        month_earnings = 0.0;

                        Timestamp ts_date;
                        Date dt_fb_date, dt_current_date;
                        String str_date, str_current_date;
                        for (QueryDocumentSnapshot doc : value) {


                            //Calculate overall
                            if(doc.get(getString(R.string.order_amount))!=null){
                                String str_total_earnings = doc.getString(getString(R.string.order_amount));
                                Double order_earnings = Double.parseDouble(str_total_earnings);
                                total_earnings = total_earnings + order_earnings;
                            }


                            if (doc.get(getString(R.string.date_created)) != null) {
                                ts_date = doc.getTimestamp(getString(R.string.date_created));
                                SimpleDateFormat sfd_format = new SimpleDateFormat("d MMMM, yyyy");

                                str_date = sfd_format.format(ts_date.toDate());
                                str_current_date = sfd_format.format(new Date());
                                try {
                                    dt_fb_date = sfd_format.parse(str_date);
                                    dt_current_date = sfd_format.parse(str_current_date);

                                    int time_value = dt_fb_date.compareTo(dt_current_date);

                                    if(time_value == 0){
                                        //today's earnings should be calculated here

                                        if(doc.get(getString(R.string.order_amount))!=null){
                                            String str_total_earnings = doc.getString(getString(R.string.order_amount));
                                            Double earnings = Double.parseDouble(str_total_earnings);
                                            today_earnings = today_earnings + earnings;
                                        }

                                    }

                                    if(isDateInCurrentWeek(dt_fb_date)){
                                        //Calculate current weeks earnings here

                                        if(doc.get(getString(R.string.order_amount))!=null){
                                            String str_total_earnings = doc.getString(getString(R.string.order_amount));
                                            Double earnings = Double.parseDouble(str_total_earnings);
                                            week_earnings = week_earnings + earnings;
                                        }
                                    }

                                    if(isDateInCurrentMonth(dt_fb_date)){
                                        //Calculate current month earnings here

                                        if(doc.get(getString(R.string.order_amount))!=null){
                                            String str_total_earnings = doc.getString(getString(R.string.order_amount));
                                            Double earnings = Double.parseDouble(str_total_earnings);
                                            month_earnings = month_earnings + earnings;
                                        }
                                    }



                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }


                        }

                        Log.i(TAG, "total earnings are " +total_earnings);

                    }
                });
    }

    public static boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return week == targetWeek && year == targetYear;
    }

    public static boolean isDateInCurrentMonth(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int month = currentCalendar.get(Calendar.MONTH);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetMonth = targetCalendar.get(Calendar.MONTH);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return month == targetMonth && year == targetYear;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startCountAnimation(TextView textView, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(0, end);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText("₹ " + Utils.getFormattedNumber(animation.getAnimatedValue().toString()));
            }
        });
        animator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration = null;
        }
    }
}