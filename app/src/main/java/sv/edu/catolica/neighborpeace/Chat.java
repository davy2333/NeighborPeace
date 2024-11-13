package sv.edu.catolica.neighborpeace;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Chat extends AppCompatActivity {
    private static final String BASE_URL = "http://172.20.10.5:80/WebServicesphp/";
    private static final String TAG = "ChatActivity";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageView backButton;
    private TextView chatTitle;

    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private AsyncHttpClient client;
    private Handler handler = new Handler();
    private Runnable messageChecker;

    private int problemId;
    private int userId;
    private String problemTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        client = new AsyncHttpClient();

        // Obtener datos del problema
        problemId = getIntent().getIntExtra("problem_id", -1);
        problemTitle = getIntent().getStringExtra("problem_title");

        // Obtener ID del usuario actual
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        if (problemId == -1 || userId == -1) {
            Toast.makeText(this, "Error al cargar el chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadMessages();
        startMessageChecker();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);
        chatTitle = findViewById(R.id.chatTitle);

        chatTitle.setText(problemTitle);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadMessages() {
        String url = BASE_URL + "getMessages.php?idProblema=" + problemId;

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONArray mensajesArray = response.getJSONArray("mensajes");
                        messages.clear();

                        for (int i = 0; i < mensajesArray.length(); i++) {
                            JSONObject mensaje = mensajesArray.getJSONObject(i);
                            messages.add(new Message(
                                    mensaje.getInt("idUsuario"),
                                    mensaje.getString("nombre"),
                                    mensaje.getString("mensaje"),
                                    mensaje.getString("fecha_formateada"),
                                    mensaje.optString("imgPersona")
                            ));
                        }

                        adapter.notifyDataSetChanged();
                        messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading messages", e);
                }
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        try {
            JSONObject params = new JSONObject();
            params.put("idProblema", problemId);
            params.put("idUsuario", userId);
            params.put("mensaje", messageText);

            StringEntity entity = new StringEntity(params.toString());

            client.post(this, BASE_URL + "sendMessage.php", entity, "application/json",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            messageInput.setText("");
                            loadMessages();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(Chat.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    private void startMessageChecker() {
        messageChecker = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, 5000); // Actualizar cada 5 segundos
            }
        };
        handler.post(messageChecker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && messageChecker != null) {
            handler.removeCallbacks(messageChecker);
        }
    }

    // Clase interna Message
    private static class Message {
        final int userId;
        final String userName;
        final String messageText;
        final String timeStamp;
        final String userImage;

        Message(int userId, String userName, String messageText, String timeStamp, String userImage) {
            this.userId = userId;
            this.userName = userName;
            this.messageText = messageText;
            this.timeStamp = timeStamp;
            this.userImage = userImage;
        }
    }

    // Adapter para el RecyclerView
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messages.get(position);

            holder.userName.setText(message.userName);
            holder.messageText.setText(message.messageText);
            holder.messageTime.setText(message.timeStamp);

            // Cargar imagen de perfil
            if (message.userImage != null && !message.userImage.isEmpty()) {
                Glide.with(Chat.this)
                        .load(BASE_URL + message.userImage)
                        .circleCrop()
                        .error(R.drawable.ic_launcher_profile)
                        .into(holder.userImage);
            } else {
                holder.userImage.setImageResource(R.drawable.ic_launcher_profile);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView userName;
            TextView messageText;
            TextView messageTime;

            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                userImage = itemView.findViewById(R.id.userImage);
                userName = itemView.findViewById(R.id.userName);
                messageText = itemView.findViewById(R.id.messageText);
                messageTime = itemView.findViewById(R.id.messageTime);
            }
        }
    }
}