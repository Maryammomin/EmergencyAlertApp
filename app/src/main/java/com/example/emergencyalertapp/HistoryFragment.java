package com.example.emergencyalertapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {
    private ListView listView;
    private ArrayList<String> historyList = new ArrayList<>();
    private String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        listView = v.findViewById(R.id.historyListView);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch History from Firebase
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("history")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String type = ds.child("type").getValue(String.class);
                            String time = ds.child("time").getValue(String.class);
                            if (type != null && time != null) {
                                historyList.add(type + " Alert sent at " + time);
                            }
                        }

                        // Show newest history at the top
                        Collections.reverse(historyList);

                        if (getActivity() != null) {
                            // Use the CUSTOM layout list_item_black_text
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                    R.layout.list_item_black_text, historyList);
                            listView.setAdapter(adapter);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        return v;
    }
}