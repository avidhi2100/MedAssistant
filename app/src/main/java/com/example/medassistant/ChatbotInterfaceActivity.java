package com.example.medassistant;

import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotInterfaceActivity extends AppCompatActivity {

    RecyclerView recycleView;
    TextView welcomeView;
    EditText messageText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    String threadId="";
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

//    OkHttpClient client = new OkHttpClient();
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot_interface);
        messageList = new ArrayList<>();
        recycleView=findViewById(R.id.recycler_view);
        welcomeView=findViewById(R.id.welcome_text);
        messageText=findViewById(R.id.message_text);
        sendButton=findViewById(R.id.send_button);

        messageAdapter = new MessageAdapter(messageList);
        recycleView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recycleView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = messageText.getText().toString().trim();
            addToChat(question,Message.SENT_BY_ME);
            messageText.setText("");
            callAPI(question);
            welcomeView.setVisibility(View.GONE);


        });
    }
    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recycleView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }
    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }

    void callAPI(String question) {
        messageList.add(new Message("Typing... ",Message.SENT_BY_BOT));
//        String escapedQuestion = question.replace("\"", "\\\"");
        String jsonPayload = String.format("{\"prompt\": \"%s\", \"thread_id\": \"%s\"}", question, threadId);

//call a function to get threadId, if threadId is null send empty string
        Log.d("JSON", String.valueOf(jsonPayload));
        RequestBody body = RequestBody.create(jsonPayload.toString(),JSON);
        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/ask")
                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer sk-proj-8Zkx72lu29NIZce3kVRUT3BlbkFJN5ZE1UMobDQsiKcrK9vz")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
                Log.e("OnFail", Objects.requireNonNull(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
//                    JSONObject  jsonObject = null;
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String output = jsonObject.getString("response");
                        threadId=jsonObject.getString("thread_id");



                        addResponse(output.trim());
                    }catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("Test:"+ e.getMessage());
                        Log.e("Respose Test error:", Objects.requireNonNull(e.getMessage()));
                    }


                }else{
                    Log.e("Failed", "no working: " + response);
                    addResponse("Failed to load response due to "+response.body().toString());
                }
            }
        });



    }
    }

