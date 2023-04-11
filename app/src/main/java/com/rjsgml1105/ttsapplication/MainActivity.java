package com.rjsgml1105.ttsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rjsgml1105.ttsapplication.api.ClovaNetworkClient;
import com.rjsgml1105.ttsapplication.api.ClovaVoiceService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;

    Button btnSound1 ,btnSound2;
    EditText editMemo;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editMemo=findViewById(R.id.editMemo);
        btnSound1=findViewById(R.id.btnSound1);
        btnSound2=findViewById(R.id.btnSound2);


        btnSound1 .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = editMemo.getText().toString();
                clova();
        

            }
        });

        btnSound2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = editMemo.getText().toString();
                googletts();
            }
        });
        
        
        
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.KOREA);

                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.e("TTS", "Language not supported.");
                    }
                } else {
                    Log.e("TTS", "Initialization failed.");
                }
            }
        });


        
        

    }

    private void clova() {

        Retrofit retrofit = ClovaNetworkClient.getRetrofitClient(MainActivity.this);
        ClovaVoiceService api = retrofit.create(ClovaVoiceService.class);


        Call<ResponseBody> call = api.synthesize("nminyoung", -1,5 ,1,0,0,0,0,text);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    try {
                        File tempMp3 = File.createTempFile("temp","mp3",getCacheDir());
                        tempMp3.deleteOnExit();;

                        InputStream inputStream = response.body().byteStream();
                        FileOutputStream fos = new FileOutputStream(tempMp3);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buffer))!=-1){
                            fos.write(buffer,0,read);
                        }
                        fos.flush();
                        fos.close();
                        inputStream.close();

                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(tempMp3.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });


    }

//    구글 tts
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            // Interrupts the current utterance (whether played or rendered to file) and
            // discards other utterances in the queue.
            tts.shutdown();
            // Releases the resources used by the TextToSpeech engine.
        }
        super.onDestroy();
    }

//    구글tts실행시 사용설정
    public void googletts() {
        tts.setPitch((float)0.5); // 톤조절
        tts.setSpeechRate((float)0.8); // 속도

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "uid");
        // 첫 번째 매개변수: 음성 출력을 할 텍스트
        // 두 번째 매개변수: 1. TextToSpeech.QUEUE_FLUSH - 진행중인 음성 출력을 끊고 이번 TTS의 음성 출력
        //                 2. TextToSpeech.QUEUE_ADD - 진행중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력
    }




}