package com.example.emergencyalertapp;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView rv;
    private List<HistoryModel> historyList = new ArrayList<>();
    private final String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        rv = v.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHistory();
        return v;
    }

    private void loadHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("history")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            historyList.add(new HistoryModel(
                                    String.valueOf(ds.child("type").getValue()),
                                    String.valueOf(ds.child("time").getValue()),
                                    String.valueOf(ds.child("recipients").getValue())
                            ));
                        }
                        Collections.reverse(historyList);
                        rv.setAdapter(new HistoryAdapter(historyList));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        List<HistoryModel> mList;
        HistoryAdapter(List<HistoryModel> list) { this.mList = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(getContext()).inflate(R.layout.item_history_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            h.type.setText(mList.get(p).type); h.time.setText(mList.get(p).time); h.rec.setText("Sent to: " + mList.get(p).recipients);
        }
        @Override public int getItemCount() { return mList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView type, time, rec;
            VH(View v) { super(v); type = v.findViewById(R.id.tvHistoryType); time = v.findViewById(R.id.tvHistoryTime); rec = v.findViewById(R.id.tvHistoryRecipients); }
        }
    }
    public static class HistoryModel { public String type, time, recipients; public HistoryModel(String t, String tm, String r) { this.type = t; this.time = tm; this.recipients = r; } }
}