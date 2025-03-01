package com.example.chattingapplication.Activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.chattingapplication.Fragment.ChatsFragment;
import com.example.chattingapplication.Fragment.ProfileFragment;
import com.example.chattingapplication.Fragment.StatusFragment;
import com.example.chattingapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    ChatsFragment ChatsFragment=new ChatsFragment();
    StatusFragment StatusFragment=new StatusFragment();
    ProfileFragment ProfileFragment=new ProfileFragment();

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView=findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            navigateToFragment(ChatsFragment, R.id.chats);
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatsFragment())
                    .commit();
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int menuItemId = item.getItemId();

                if (menuItemId == R.id.chats) {
                    navigateToFragment(ChatsFragment, menuItemId);
                }
                else if (menuItemId == R.id.status) {
                    navigateToFragment(StatusFragment, menuItemId);
                }
                else if (menuItemId == R.id.profile) {
                    navigateToFragment(ProfileFragment, menuItemId);
                }
                else{
                    return false;
                }
                return true;
            }
        });
    }
    private void navigateToFragment(Fragment fragment, int menuItemId) {

        if (currentFragment != fragment) {
            currentFragment = fragment;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Check if the fragment is already added
            if (fragment.isAdded()) {
                // If the fragment is already added, just show it
                transaction.show(fragment);
            } else {
                // If it's not added, replace it
                transaction.replace(R.id.fragment_container, fragment);

                // Only add non-home fragments to the back stack
                if (!(fragment instanceof ChatsFragment)) {
                    transaction.addToBackStack(null);
                }
            }

            transaction.commit();

            // Immediately set the selected item
            bottomNavigationView.setSelectedItemId(menuItemId);
        }
    }

}