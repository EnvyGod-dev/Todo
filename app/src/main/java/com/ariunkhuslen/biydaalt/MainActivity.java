package com.ariunkhuslen.biydaalt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<DataClass> dataList;
    private List<DataClass> originalList; // Store the original list separately
    private MyAdapter adapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        searchView.clearFocus();

        // Set up RecyclerView
        dataList = new ArrayList<>();
        originalList = new ArrayList<>();
        adapter = new MyAdapter(this, dataList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Android_tut");

        // Fetch data from Firebase
        fetchDataFromFirebase();

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchDataFromFirebase();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Set up SearchView listener
        setupSearchView();

        // Floating Action Button click listener
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });
    }

    private void fetchDataFromFirebase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                originalList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = itemSnapshot.getValue(DataClass.class);
                    if (dataClass != null) {
                        dataList.add(dataClass);
                        originalList.add(dataClass); // Keep a copy in the original list
                    }
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    private void filterData(String query) {
        if (query.isEmpty()) {
            // If the search query is empty, show the original list
            adapter.updateList(originalList);
        } else {
            ArrayList<DataClass> filteredList = new ArrayList<>();
            for (DataClass dataClass : originalList) {
                if (dataClass.getDataTitle() != null && dataClass.getDataTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(dataClass);
                }
            }
            // Update the adapter with the filtered list
            adapter.updateList(filteredList);
        }
    }
}
