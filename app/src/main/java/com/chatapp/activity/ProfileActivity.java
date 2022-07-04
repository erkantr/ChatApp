package com.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatapp.R;
import com.chatapp.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    GoogleMap mGoogleMap;
    FusedLocationProviderClient client;
    SupportMapFragment mapFragment;
    Location location;
    double lat;
    double lng;

    Button guncelle;

    ImageView profile_image;

    TextView fullname, tw;

    TextInputEditText kullanici_adi_edit, email, sifre_edit2, sifre_edit, sifre_edit1;

    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        kullanici_adi_edit = findViewById(R.id.username_et);
        sifre_edit2 = findViewById(R.id.newpassword2);
        sifre_edit = findViewById(R.id.password);
        sifre_edit1 = findViewById(R.id.newpassword);
        fullname = findViewById(R.id.username);
        tw = findViewById(R.id.adress);
        profile_image = findViewById(R.id.profile_image);
        guncelle = findViewById(R.id.guncelle);
        email = findViewById(R.id.email);

        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(this);
            //getLocation();
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        try {
        getLocation();
        Geocoder geo = new Geocoder(ProfileActivity.this.getApplicationContext(), Locale.getDefault());
        List<Address> addresses = geo.getFromLocation(lat, lng, 1);

        System.out.println(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubLocality() + " Mahallesi, " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());

        String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
        tw.setText(newtext);
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);


        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance(ChatActivity.url).getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ProfileActivity.class == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                if (user.getId().equals(fuser.getUid())) {
                    fullname.setText(user.getUsername());
                    email.setText(fuser.getEmail());
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.drawable.ic_outline_account_circle_24);
                    } else {
                        if (ChatActivity.isValidContextForGlide(ProfileActivity.this)) {
                            Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profile_image);
                        }
                    }
                    kullanici_adi_edit.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(ProfileActivity.this);
            }
        });

        guncelle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_name = kullanici_adi_edit.getText().toString();
                String txt_sifre2 = sifre_edit2.getText().toString();
                String txt_sifre = sifre_edit.getText().toString();
                String txt_sifre1 = sifre_edit1.getText().toString();
                position = 1;

                if (TextUtils.isEmpty(txt_name)) {
                    Toast.makeText(ProfileActivity.this, "Tüm alanlar zorunludur!", Toast.LENGTH_SHORT).show();
                } else {
                    reference = FirebaseDatabase.getInstance(ChatActivity.url).getReference("Users").child(fuser.getUid());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String newPassword = "";

                    if (TextUtils.isEmpty(txt_sifre1) && TextUtils.isEmpty(txt_sifre) && TextUtils.isEmpty(txt_sifre2)) {
                        System.out.println("Şifre yenilenemedi");
                    } else {
                        if (TextUtils.isEmpty(txt_sifre1) || TextUtils.isEmpty(txt_sifre) || TextUtils.isEmpty(txt_sifre2)) {
                            Toast.makeText(ProfileActivity.this, "Şifrenizi değiştirebilmeniz için tüm alanları doldurmanız gerekiyor", Toast.LENGTH_SHORT).show();
                            position = 0;
                        } else {
                            if (txt_sifre2.equals(txt_sifre1)) {
                                newPassword = txt_sifre1;
                                if (user != null) {
                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(user.getEmail(), txt_sifre);

                                    String finalNewPassword = newPassword;
                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        System.out.println("Kullanıcı yeniden giriş yaptı");
                                                        if (!finalNewPassword.equals("") && finalNewPassword.length() >= 6 && position != 3) {
                                                            user.updatePassword(finalNewPassword)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                System.out.println("Şifre yenileme başarılı");
                                                                            } else {
                                                                                Toast.makeText(ProfileActivity.this, "Bu şifre ile kayıt olamazsınız", Toast.LENGTH_SHORT).show();
                                                                                position = 0;
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            position = 0;
                                                            Toast.makeText(ProfileActivity.this, "şifre en az 6 karakterden oluşmalıdır", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        position = 0;
                                                        Toast.makeText(ProfileActivity.this, "Lütfen şifrenizi doğru girin", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Çok fazla güncelleme yaptınız lütfen çıkış yapıp tekrar deneyin", Toast.LENGTH_SHORT).show();
                                    position = 3;
                                }
                            } else {
                                position = 0;
                                Toast.makeText(ProfileActivity.this, "Şifreler birbirleri ile uyuşmuyor!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();;
                    map.put("username", txt_name);
                    reference.updateChildren(map);
                    if (position != 0 || position != 3) {
                        Toast.makeText(ProfileActivity.this, "Profil bilgileri güncellendi", Toast.LENGTH_SHORT).show();
                    }
                    sifre_edit.setText("");
                    sifre_edit1.setText("");
                    sifre_edit2.setText("");
                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance(ChatActivity.url).getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", "" + mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Upload in preogress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(this);
            mGoogleMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Buradayım!");
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    googleMap.addMarker(markerOptions);
                } else {
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        TextView tw = findViewById(R.id.adress);
                        tw.setText(newtext);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    tw.setText("Konum bilgisi alınamadı");
                    Toast.makeText(ProfileActivity.this, "Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(ProfileActivity.this, "Konum bilgisi kapalı", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}