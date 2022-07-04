package com.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.chatapp.R;
import com.chatapp.adapter.ChatAdapter;
import com.chatapp.adapter.UserAdapter;
import com.chatapp.model.Chat;
import com.chatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    ImageView profile_image;
    TextView username;
    ImageView back;

    public static UserAdapter userAdapter;
    private List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = findViewById(R.id.recyclerview);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        back = findViewById(R.id.back);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();

        readUsers();

        Window window = this.getWindow();

        window.setStatusBarColor(this.getResources().getColor(R.color.color1));

        profile_image.setOnClickListener(this::menu);

        back.setOnClickListener(view -> {
            NavUtils.navigateUpFromSameTask(UsersActivity.this);

        });

    }

    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance(ChatActivity.url).getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (user.getId() != null && !user.getId().equals(firebaseUser.getUid())) {
                        mUsers.add(user);
                    }

                }

                userAdapter = new UserAdapter(UsersActivity.this, mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void menu(View v){
        PopupMenu popupMenu = new PopupMenu(UsersActivity.this,v);
        popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.profile:
                        startActivity(new Intent(UsersActivity.this,ProfileActivity.class));
                        break;
                    case R.id.logout:
                        startActivity(new Intent(UsersActivity.this,LoginActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }
}