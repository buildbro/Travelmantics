package com.example.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private static final int PICTURE_REQUEST_CODE = 42;
    FirebaseDatabase mFirebaseDb;
    DatabaseReference mDbReference;

    EditText titleEditText, descriptionEditText, priceEditText;
    ImageView dealImageView;
    Button imageBtn;
    TravelDeal deal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDb = FirebaseUtil.mFirebaseDatabase;
        mDbReference = FirebaseUtil.mDatabaseReference;

        Intent intent = getIntent();
        deal = (TravelDeal) intent.getSerializableExtra("deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        initializeUi();

        showImage(deal.getImageUrl());
    }

    //helper method that setup views for this activity
    private void initializeUi() {
        titleEditText = findViewById(R.id.title_text_view);
        descriptionEditText = findViewById(R.id.description_text_view);
        priceEditText = findViewById(R.id.price_text_view);
        dealImageView = findViewById(R.id.deal_image_view);
        imageBtn = findViewById(R.id.btn_image);

        titleEditText.setText(deal.getTitle());
        descriptionEditText.setText(deal.getDescription());
        priceEditText.setText(deal.getPrice());

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Photo"), PICTURE_REQUEST_CODE);
            }
        });
    }

    private void saveDeal() {
        deal.setTitle(titleEditText.getText().toString());
        deal.setDescription(descriptionEditText.getText().toString());
        deal.setPrice(priceEditText.getText().toString());

        if (deal.getId() == null) {
            mDbReference.push().setValue(deal);
        } else {
            mDbReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();

            if (deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
                StorageReference ref = FirebaseUtil.mStorageRef.child(deal.getImageName());
                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete image", "Image deleted!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Delete image", e.getMessage());
                    }
                });
            }
            return;
        }

        mDbReference.child(deal.getId()).removeValue();
    }

    private void backToList() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void clean() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        priceEditText.setText("");
        titleEditText.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        titleEditText.setEnabled(isEnabled);
        descriptionEditText.setEnabled(isEnabled);
        priceEditText.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(dealImageView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUrl;
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri url = uri.getResult();

                    Log.d("Upload URL", "Upload Success, download URL ");
                    downloadUrl = url.toString();
                    deal.setImageUrl(downloadUrl);
                    showImage(downloadUrl);

                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageName(pictureName);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deal_activity_menu, menu);

        if (FirebaseUtil.isAdmin == true) {
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableEditTexts(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
