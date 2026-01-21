package com.example.emergencyalertapp;

import android.content.Intent;
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

public class ContactsFragment extends Fragment {
    private ListView listView;
    private ArrayList<String> contactList = new ArrayList<>();
    private String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = v.findViewById(R.id.contactListView);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch contacts from Firebase
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("emergencyContacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contactList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String number = ds.getValue(String.class);
                            contactList.add(number);
                        }

                        if (getActivity() != null) {
                            // Use the CUSTOM layout list_item_black_text
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                    R.layout.list_item_black_text, contactList);
                            listView.setAdapter(adapter);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Plus button to add more contacts
        v.findViewById(R.id.btnAddContacts).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), EmergencyContactActivity.class)));

        return v;
    }
}