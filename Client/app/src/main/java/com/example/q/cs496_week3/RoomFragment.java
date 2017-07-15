package com.example.q.cs496_week3;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class RoomFragment extends Fragment {

    final String S_NEW_MESSAGE = "new-message", S_USER_JOINED = "user-joined", S_USER_LEFT = "user-left";

    String NICKNAME = UserInfo.getNickname();
    String ROOM = RoomActivity.ROOM;

    private Boolean isConnected = true;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private Socket mSocket;

    public RoomFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapter(context, mMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(S_NEW_MESSAGE, onNewMessage);
        mSocket.on(S_USER_JOINED, onUserJoined);
        mSocket.on(S_USER_LEFT, onUserLeft);
        mSocket.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(S_NEW_MESSAGE, onNewMessage);
        mSocket.off(S_USER_JOINED, onUserJoined);
        mSocket.off(S_USER_LEFT, onUserLeft);
    }

    private void attemptSend() {
        if (!mSocket.connected()) return;

        String message = mInputMessageView.getText().toString().trim();

        mInputMessageView.setText("");

        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        JSONObject data = createData(new String[]{"message"}, new String[]{message});
        mSocket.emit("new-message", data);
    }

    private void addMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE).username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private JSONObject createData(String[] keys, String[] values) {
        JSONObject data = new JSONObject();

        if (keys.length != values.length) {
            Log.d("createData", "ERROR: KEYS DO NOT MATCH VALUES");
            return data;
        }

        try {
            data.put("room", ROOM);
            data.put("nickname", NICKNAME);
            for (int i = 0; i < keys.length; i++) {
                data.put(keys[i], values[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
                        Toast.makeText(getActivity().getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();
                        Log.d("onConnect", "Disconnected");
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Log.d("onDisconnect", "disconnected");
                    Toast.makeText(getActivity().getApplicationContext(), R.string.disconnect, Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
                        isConnected = false;
                    }

                    Log.d("onConnectError", "Error connecting");

                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username, message;
                    try {
                        username = data.getString("nickname");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e("onNewMessage", e.getMessage());
                        return;
                    }

                    addMessage(username, message);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nickname;
                    int numUsers;
                    try {
                        nickname = data.getString("nickname");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e("onUserJoined", e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, nickname));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e("onUserLeft", e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                }
            });
        }
    };
}
