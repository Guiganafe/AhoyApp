package com.guiganafe.ahoy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessage;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private String currentGroupName, currentUserId, currentUserName, currentTime, currentDate;
    private DatabaseReference usersReference, groupNameReference, groupMessageKeyReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Inicializa o Firebase e instancia o mesmo*/
        mAuth = FirebaseAuth.getInstance();
        /*Retorna o id do usuário*/
        currentUserId = mAuth.getCurrentUser().getUid();
        /*Retorna o nome do grupo*/
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        /*Retorna a referencia do usuário da tabela users*/
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        /*Retorna a referência do grupo a partir da tabela groups*/
        groupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        setContentView(R.layout.activity_group_chat);
        /*Inicializa os campos*/
        InitializeMetods();
        /*Busca as informações do usuário no Firebase*/
        getUserInfo();
        /*Ação de clicar no ImageButton de enviar a mensagem*/
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName + " :\n" + chatMessage + " \n" + chatTime + "   " + chatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    /**
     * Salva os dados da mensagem no banco de dados
     */
    private void SaveMessageInDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameReference.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Insira uma mensagem válida", Toast.LENGTH_SHORT);
        }else{
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);

            groupMessageKeyReference = groupNameReference.child(messageKey);

            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("name", currentUserName);
            messageMap.put("message", message);
            messageMap.put("date", currentDate);
            messageMap.put("time", currentTime);

            groupMessageKeyReference.updateChildren(messageMap);
        }
    }

    /**
     * Inicializa os campos
     */
    private void InitializeMetods() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessage = (ImageButton) findViewById(R.id.send_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    /**
     * Busca os dados do usuário na base de dados do Firebase
     */
    public void getUserInfo() {
        usersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
