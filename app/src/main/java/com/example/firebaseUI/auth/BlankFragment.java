package com.example.firebaseUI.auth;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.firebaseUI.R;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment implements View.OnClickListener {

    private ListView listView;

    private ArrayAdapter<String> adapter;
    private EditText text;
    private Button button;
    private String test1 = "test";


    private DatabaseReference myRef;


    public BlankFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        // Create a new Adapter
        adapter = new ArrayAdapter<>(BlankFragment.this.getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(BlankFragment.this.getActivity());

        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child(test1).addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null){
                    adapter.add( (String)dataSnapshot.child("text").getValue());
                }

            }

            @Override
            public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {
                adapter.remove((String)dataSnapshot.child("text").getValue());
            }

            @Override
            public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       
        text = (EditText) view.findViewById(R.id.todoText);
        button = (Button)  view.findViewById(R.id.addButton);
        button.setOnClickListener(this);

        return view;
    }
    @Override
    public void onClick(View view) {
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(test1).push().child("text").setValue(text.getText().toString());
    }
}
