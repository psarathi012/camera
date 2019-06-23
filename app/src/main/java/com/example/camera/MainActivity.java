package com.example.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button button;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView= findViewById(R.id.image);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private int matchWords(String wrd, String medicine){
        wrd = wrd.toLowerCase();
        medicine = medicine.toLowerCase();
        return FuzzySearch.ratio(medicine,wrd);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if(!textRecognizer.isOperational()){
                System.out.println("Non operational *******************************");
            }
            else {
                System.out.println("Operational ******************************");
                assert imageBitmap != null;
                Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for (int i =0; i<items.size(); i++){
                    TextBlock myitems = items.valueAt(i);
                    sb.append(myitems.getValue());
                    sb.append(" ");
                }
                System.out.println("STRING ****************************************");
                System.out.println(sb.toString());
                String text = sb.toString();
                if(items.size()==0){
                    System.out.println("Sorry, No text detected");
                    text = "No text found";
                }
                ArrayList<String> medicines= new ArrayList<>();
                try {
                    Scanner sc = new Scanner(getAssets().open("medicines.txt"));
                    while (sc.hasNextLine()) {
                        String ln = sc.nextLine();
                        System.out.println(ln);
                        System.out.println("***************************************************");
                        medicines.add(ln);
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("***************************************************");
                }
                String[] foundwrds = text.split(" ");
                int max = -1;
                for (String foundwrd : foundwrds) {
                    for (int j = 0; j < medicines.size(); j++) {
                        int score = matchWords(foundwrd, medicines.get(j));
                        if (score > max) {
                            max = score;
                            text = medicines.get(j);
                        }
                    }
                }
                System.out.println(max);
                System.out.println(text);
                System.out.println("************************************************************");
                textView.setText(text);
            }
        }
    }
}
