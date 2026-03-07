package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends Fragment {
    private RecyclerView rv;
    private List<ContactModel> list = new ArrayList<>();
    private final String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        rv = v.findViewById(R.id.rvContacts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        loadContacts();

        v.findViewById(R.id.btnAddContacts).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), EmergencyContactActivity.class)));
        return v;
    }

    private void loadContacts() {
        if (FirebaseAuth.getInstance().getUid() == null) return;
        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("emergencyContacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "Emergency Contact", number = "";
                            if (ds.getValue() instanceof Map) {
                                Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                name = String.valueOf(map.get("name"));
                                number = String.valueOf(map.get("number"));
                            } else {
                                number = ds.getValue(String.class);
                            }
                            String phoneName = getContactNameFromPhone(number);
                            list.add(new ContactModel(phoneName != null ? phoneName : name, number));
                        }
                        rv.setAdapter(new ContactAdapter(list));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private String getContactNameFromPhone(String num) {
        if (num == null || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        try (Cursor c = requireContext().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception e) {} return null;
    }

    class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {
        List<ContactModel> mList;
        ContactAdapter(List<ContactModel> list) { this.mList = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(getContext()).inflate(R.layout.item_contact_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            h.name.setText(mList.get(p).name);
            h.num.setText(mList.get(p).number);
        }
        @Override public int getItemCount() { return mList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView name, num;
            VH(View v) { super(v); name = v.findViewById(R.id.tvContactName); num = v.findViewById(R.id.tvContactNumber); }
        }
    }

    public static class ContactModel {
        public String name, number;
        public ContactModel(String n, String num) { this.name = n; this.number = num; }
    }
}