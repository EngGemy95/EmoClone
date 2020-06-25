package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivitySettingsBinding;
import com.fci_zu_eng_gemy_95.emoclone.model.Contacts;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    private static final int REQUEST_LOAD_IMAGES = 100;
    Intent intent;
    Uri imageUri;
    StorageReference userProfileImageRef;
    DatabaseReference userRef;
    String userID;
    ProgressDialog dialog;
    HashMap<String, Object> profileData;
    String savedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages/");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingProfileData(userID);

        binding.setMyHandler(new MyHandler(getApplicationContext()));
    }

    private void loadingProfileData(final String userid) {
        if (userid != null) {
            userRef.child(userid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Contacts contactsData = dataSnapshot.getValue(Contacts.class);

                        binding.usernameSettings.setText(contactsData.getUsername());
                        binding.bioSettings.setText(contactsData.getStatus());
                        Log.d("userDatagetImageUri", contactsData.getImgUri() + "  oo");
                        Picasso.get().load(contactsData.getImgUri())
                                .placeholder(R.drawable.profile_image).into(binding.profileImageSettings);

                    } else {
                        binding.usernameSettings.setText("");
                        binding.bioSettings.setText("");
                        binding.profileImageSettings.setImageResource(R.drawable.profile_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGES && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.profileImageSettings.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void open(int REQUEST_LOAD_IMAGES) {
        openGallery();
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_LOAD_IMAGES);
    }

    public void openGallery() {
        intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
    }

    public class MyHandler {
        Context context;

        public MyHandler(Context context) {
            this.context = context;
        }

        public void showImage(View view) {
            open(REQUEST_LOAD_IMAGES);
        }

        public void saveUserData(View view) {
            if (imageUri == null) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("imgUri")) {
                            saveInfoOnlyWithoutImage();
                        } else {
                            Toast.makeText(context, "Please Select Image !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else if (binding.usernameSettings.getText().equals("") || binding.usernameSettings.getText().length() < 1) {
                Toast.makeText(SettingsActivity.this, "Enter User Name", Toast.LENGTH_SHORT).show();

            } else if (binding.bioSettings.getText().equals("") || binding.bioSettings.getText().length() < 1) {
                Toast.makeText(SettingsActivity.this, "Enter Bio", Toast.LENGTH_SHORT).show();

            } else {

                dialog = new ProgressDialog(SettingsActivity.this);
                dialog.setTitle("Account Settings");
                dialog.setMessage("Please Wait....");
                dialog.show();

                final StorageReference filePath = userProfileImageRef.child(userID);
                final UploadTask uploadTask = filePath.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            task.getException();
                        }
                        savedImageUri = filePath.getDownloadUrl().toString();

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            savedImageUri = task.getResult().toString();

                            final String username = binding.usernameSettings.getText().toString();
                            final String userStatus = binding.bioSettings.getText().toString();
                            HashMap<String, Object> profileData = new HashMap<>();
                            profileData.put("uid", userID);
                            // ( username , status , imgUri ) this attribute must equal the name of the ( User Class )
                            profileData.put("username", username);
                            profileData.put("status", userStatus);
                            profileData.put("imgUri", savedImageUri);

                            userRef.child(userID).updateChildren(profileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Profile data updated", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingsActivity.this, ContactsActivity.class));
                                    }
                                }
                            });
                        }
                    }
                });

            }

        }

        private void saveInfoOnlyWithoutImage() {

            final String username = binding.usernameSettings.getText().toString();
            final String userStatus = binding.bioSettings.getText().toString();

            if (binding.usernameSettings.getText().equals("") || binding.usernameSettings.getText().length() < 1) {
                Toast.makeText(SettingsActivity.this, "Enter User Name", Toast.LENGTH_SHORT).show();

            } else if (binding.bioSettings.getText().equals("") || binding.bioSettings.getText().length() < 1) {
                Toast.makeText(SettingsActivity.this, "Enter Bio", Toast.LENGTH_SHORT).show();

            } else {
                dialog = new ProgressDialog(SettingsActivity.this);
                dialog.setTitle("Account Settings");
                dialog.setMessage("Please Wait....");
                dialog.show();

                HashMap<String, Object> profileData = new HashMap<>();
                profileData.put("uid", userID);
                profileData.put("username", username);
                profileData.put("status", userStatus);

                userRef.child(userID).updateChildren(profileData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Profile data updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SettingsActivity.this, ContactsActivity.class));
                        }
                    }
                });

            }
        }

    }
}
