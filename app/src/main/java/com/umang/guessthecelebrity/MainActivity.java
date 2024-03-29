package com.umang.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb =0;
    ImageView imageView;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];
    int[] answersint = new int[4];
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void generateQuestion(){
        Random random = new Random();

        int tempchoosen = random.nextInt(celebUrls.size());

        while (chosenCeleb == tempchoosen){
            tempchoosen = random.nextInt(celebUrls.size());
        }
        chosenCeleb = tempchoosen;

        ImageDownloader imageTask = new ImageDownloader();

        Bitmap celebImage;

        try {
            celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for(int i=0;i<4;i++){

                if(i==locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }
                else {

                    incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    while (chosenCeleb == incorrectAnswerLocation  || Arrays.asList(answersint).contains(incorrectAnswerLocation)){

                        incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    }

                    answersint[i] = incorrectAnswerLocation;
                    answers[i] = celebNames.get(incorrectAnswerLocation);

                }

            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void celebChosen(View view){

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct Answer", Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(getApplicationContext(), "Wrong! It Was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }
        generateQuestion();
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }

                return result;

            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        DownloadTask task = new DownloadTask();
        String result = "";

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();

//            Log.i("Contents : ", result);

            String[] splitResult = result.split("<div class\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"http://cdn.posh24.se/images/:profile(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()){
                celebUrls.add("http://cdn.posh24.se/images/:profile" + m.group(1));
               // Log.i("Urls = ", m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while(m.find()){
                celebNames.add(m.group(1));
              //  Log.i("Names = ", m.group(1));
            }

//            Log.i("Size of Names = ", Integer.toString(celebNames.size()));
//            Log.i("Size of Urls = ", Integer.toString(celebUrls.size()));

            generateQuestion();


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
