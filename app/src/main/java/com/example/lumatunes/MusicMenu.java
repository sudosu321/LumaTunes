package com.example.lumatunes;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicMenu extends AppCompatActivity {
    FloatingActionButton button;
    ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        button = findViewById(R.id.button);
        listView=findViewById(R.id.listView);
        startApp();
    }
    void refreshList(){
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,
                list,R.layout.item_layout,new String[]{"name","uri"},
                new int[]{R.id.name, R.id.icon});
        listView.setAdapter(simpleAdapter);
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.icon) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageResource(R.drawable.music_foreground);
                    return true;
                }
                return false;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Extract clicked video URI
            Toast.makeText(MusicMenu.this, "" + list.get(position).get("name"), Toast.LENGTH_LONG).show();
            ArrayList<String> newList = new ArrayList<>();
            for (HashMap<String, Object> item : list) {
                newList.add((String) item.get("uri"));
            }
            // Start PlayerActivity
            Intent intent = new Intent(MusicMenu.this, playerActivity.class);
            intent.putStringArrayListExtra("list", newList); // full list
            intent.putExtra("position", position);                 // clicked position
            startActivity(intent);
        });
    }
    void startApp(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, 1001);//THIS WILL OPEN THE SAF
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri folderUri = data.getData();

                getContentResolver().takePersistableUriPermission(folderUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                createList(folderUri);
            }
        }
    }
    void createList(Uri folderUri) {
        list = new ArrayList<>();
        //FOR LISTING THE CONTENTS
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
        );
        //CURSOR IS USED TO ITERATE OVER COLLECTION OF OBJECTS , HERE OBJECT IS THE STRING STORING DISPLAY NAME IF TYPE
        Cursor cursor = getContentResolver().query(
                childrenUri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME,//NAME
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,//ID
                        DocumentsContract.Document.COLUMN_MIME_TYPE},//TYPE
                null,
                null,
                null
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String docId = cursor.getString(1);
                String mime = cursor.getString(2);
                if (mime != null && mime.startsWith("audio/")) {//USING VIDEO ONLY
                    Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId);//GENERATES USRI OF FILE
                    addItemToList(name, fileUri.toString());  // save URI instead of file path
                }
            }
            refreshList();
            cursor.close();
        }
    }
    void addItemToList(String name, String uriString) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uri", uriString);
        map.put("name", name);
        list.add(map);
    }
}