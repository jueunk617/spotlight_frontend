package com.example.spotlight.alarm;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotlight.graduates.GraduatesProposeActivity;
import com.example.spotlight.R;
import com.example.spotlight.network.API.*;
import com.example.spotlight.network.Response.NotificationListResponse;
import com.example.spotlight.network.Response.NotificationResponse;
import com.example.spotlight.network.Util.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmActivity extends AppCompatActivity {
    private ApiService apiService;
    private Typeface typeface1;
    private Typeface typeface2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_graduates);

        // 폰트 설정을 위해 assets 폴더에 있는 폰트 파일의 경로 설정
        String fontPath1 = "font/pretendard_bold.ttf";
        String fontPath2 = "font/pretendard_regular.ttf";

        // 폰트 파일을 로드하여 설정
        typeface1 = ResourcesCompat.getFont(this, R.font.pretendard_bold);
        typeface2 = ResourcesCompat.getFont(this, R.font.pretendard_regular);

        apiService = ApiClient.getClient().create(ApiService.class);

        Call<NotificationListResponse> call = apiService.getNotifications();
        call.enqueue(new Callback<NotificationListResponse>() {
            @Override
            public void onResponse(Call<NotificationListResponse> call, Response<NotificationListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationResponse> notificationList = response.body().getNotifications();
                    for (NotificationResponse notificationResponse : notificationList) {
                        addNotification(notificationResponse);
                    }
                } else {
                    Toast.makeText(AlarmActivity.this, "토큰: " + TokenManager.getToken(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(AlarmActivity.this, "알림 목록을 가져오지 못했습니다. 응답 코드: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<NotificationListResponse> call, Throwable t) {
            }
        });
    }

    public void addNotification(NotificationResponse notificationResponse) {
        Long notificationId = notificationResponse.getNotificationId();
        String type = notificationResponse.getType();
        String message = notificationResponse.getMessage();
        String date = notificationResponse.getDate();
        String status = notificationResponse.getStatus();
        LinearLayout parentLayout = findViewById(R.id.notification_layout);

        LinearLayout dynamicLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(5), dpToPx(10), dpToPx(5), 0);
        dynamicLayout.setLayoutParams(layoutParams);
        dynamicLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(35), dpToPx(35)); // 이미지 크기 설정
        imageParams.setMargins(dpToPx(16), dpToPx(5), 0, 0); // 이미지 마진 설정
        imageView.setLayoutParams(imageParams);
        imageView.setImageResource(R.drawable.alarm_detail); // 이미지 설정

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView1 = new TextView(this);
        LinearLayout.LayoutParams textParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams1.setMargins(dpToPx(10), 0, 0, 0); // 텍스트 마진 설정
        textView1.setLayoutParams(textParams1);
        textView1.setText(message);
        textView1.setTypeface(typeface1);
        textView1.setTextColor(Color.BLACK);
        textView1.setTextSize(18); // 글자 크기 설정

        TextView textView2 = new TextView(this);
        LinearLayout.LayoutParams textParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams2.setMargins(dpToPx(10), dpToPx(2), 0, 0); // 텍스트 마진 설정
        textView2.setLayoutParams(textParams2);
        textView2.setText(date);
        textView2.setTextColor(Color.parseColor("#a6a6a6"));
        textView2.setTypeface(typeface2);
        textView2.setTextSize(15); // 글자 크기 설정

        textLayout.addView(textView1);
        textLayout.addView(textView2);

        dynamicLayout.addView(imageView);
        dynamicLayout.addView(textLayout);

        parentLayout.addView(dynamicLayout, 0);

        dynamicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationClick(notificationResponse);
            }
        });
    }

    private void handleNotificationClick(NotificationResponse notificationResponse) {
        String type = notificationResponse.getType();
        Intent intent;
        switch (type) {
            case "invited":
                showAcceptDialog(notificationResponse);
                break;
            case "recruit":
                intent = new Intent(AlarmActivity.this, GraduatesProposeActivity.class);
                intent.putExtra("notification_id", notificationResponse.getNotificationId());
                intent.putExtra("message", notificationResponse.getMessage());
                startActivity(intent);
                break;
            case "invite_result":
                break;
            default:
                Toast.makeText(AlarmActivity.this, "알 수 없는 알림 유형입니다.", Toast.LENGTH_SHORT).show();
                return;
        }
    }

    private void acceptInvitation(Long projectRoleId, Long userId, AlertDialog dialog) {
        Call<Void> call = apiService.acceptInvitation(projectRoleId, userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공 메시지 표시
                    Toast.makeText(AlarmActivity.this, "초대가 수락되었습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // 다이얼로그 닫기
                } else {
                    // 실패 메시지 표시
                    Toast.makeText(AlarmActivity.this, "초대 수락에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 서버 오류 메시지 표시
                Toast.makeText(AlarmActivity.this, "서버 요청에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAcceptDialog(NotificationResponse notificationResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ok, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageButton confirmButton = dialogView.findViewById(R.id.dialog_invite_ok);
        ImageButton cancelButton = dialogView.findViewById(R.id.dialog_invite_no);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 초대 수락 API 호출
                acceptInvitation(notificationResponse.getNotificationId(), notificationResponse.getUserId(), dialog);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });

        dialog.show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onInviteNoClicked(View view) {
        showNoDialog();
    }

    public void onInviteOkClicked(View view) {
        showAcceptDialog();
    }

    private void showNoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_no, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageButton confirmButton = dialogView.findViewById(R.id.dialog_no_ok);
        ImageButton cancelButton = dialogView.findViewById(R.id.dialog_no_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기서 거절 동작을 처리
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showAcceptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ok, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageButton confirmButton = dialogView.findViewById(R.id.dialog_invite_ok);
        ImageButton cancelButton = dialogView.findViewById(R.id.dialog_invite_no);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기서 수락 동작을 처리
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}