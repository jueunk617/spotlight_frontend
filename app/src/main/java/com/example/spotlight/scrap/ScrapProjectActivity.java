package com.example.spotlight.scrap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotlight.R;
import com.example.spotlight.network.API.ApiClient;
import com.example.spotlight.network.API.ApiService;
import com.example.spotlight.network.DTO.FeedDTO;
import com.example.spotlight.network.Util.FeedDTOConverter;
import com.example.spotlight.posting.Post;
import com.example.spotlight.posting.PostAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScrapProjectActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FeedDTOConverter feedDTOConverter = new FeedDTOConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrap_project);

        recyclerView = findViewById(R.id.recyclerView_scrap_project);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<FeedDTO>> call = apiService.getScrappedFeeds();

        call.enqueue(new Callback<List<FeedDTO>>() {
            @Override
            public void onResponse(Call<List<FeedDTO>> call, Response<List<FeedDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FeedDTO> scrappedFeeds = response.body();

                    List<Post> posts = feedDTOConverter.convertToPostList(scrappedFeeds);

                    postAdapter = new PostAdapter(ScrapProjectActivity.this, posts);
                    recyclerView.setAdapter(postAdapter);
                } else {
                    showError("스크랩 목록을 불러오는 데 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<FeedDTO>> call, Throwable t) {
                Log.e("API Error", "Error: " + t.getMessage(), t);
                showError("네트워크 오류가 발생했습니다. 인터넷 연결을 확인해 주세요.");
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(ScrapProjectActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onScrapStageClicked(View view) {
        Intent intent = new Intent(this, ScrapStageActivity.class);
        startActivity(intent);
    }
}
