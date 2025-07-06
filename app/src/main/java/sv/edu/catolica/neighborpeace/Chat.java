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
import android.widget.LinearLayout;
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
    private static final String BASE_URL = "http://192.168.0.12:80/WebServicesphp/";
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

        // Inicializar y configurar el cliente HTTP
        setupHttpClient();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadMessages();
        startMessageChecker();
    }

    private void setupHttpClient() {
        client = new AsyncHttpClient();
        client.setTimeout(15000); // 15 segundos
        client.setConnectTimeout(15000);
        client.setResponseTimeout(15000);
        client.setMaxRetriesAndTimeout(3, 5000);

        // Configurar headers por defecto
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Accept", "application/json");
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
        Log.d(TAG, "Cargando mensajes desde: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONArray mensajesArray = response.getJSONArray("mensajes");
                        int previousCount = messages.size();
                        messages.clear();

                        for (int i = 0; i < mensajesArray.length(); i++) {
                            JSONObject mensaje = mensajesArray.getJSONObject(i);
                            // Asegurarse de que el tipo de mensaje sea explícitamente 1 para encuestas
                            int messageType = mensaje.has("tipo_mensaje") ? mensaje.getInt("tipo_mensaje") : 0;

                            if (messageType == 1 && mensaje.getString("mensaje").startsWith("{")) {
                                try {
                                    JSONObject pollData = new JSONObject(mensaje.getString("mensaje"));
                                    // Verificar que sea una encuesta válida
                                    if (pollData.has("encuestaId") && pollData.has("pregunta")) {
                                        messages.add(new Message(
                                                mensaje.getInt("idUsuario"),
                                                mensaje.getString("nombre"),
                                                pollData.getString("pregunta"),
                                                mensaje.getString("fecha_formateada"),
                                                mensaje.optString("imgPersona"),
                                                1,
                                                pollData
                                        ));
                                    } else {
                                        // Si no tiene la estructura de encuesta, tratarlo como mensaje normal
                                        messages.add(new Message(
                                                mensaje.getInt("idUsuario"),
                                                mensaje.getString("nombre"),
                                                mensaje.getString("mensaje"),
                                                mensaje.getString("fecha_formateada"),
                                                mensaje.optString("imgPersona")
                                        ));
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing poll data", e);
                                    // Si hay error al parsear, tratar como mensaje normal
                                    messages.add(new Message(
                                            mensaje.getInt("idUsuario"),
                                            mensaje.getString("nombre"),
                                            mensaje.getString("mensaje"),
                                            mensaje.getString("fecha_formateada"),
                                            mensaje.optString("imgPersona")
                                    ));
                                }
                            } else {
                                // Mensaje normal
                                messages.add(new Message(
                                        mensaje.getInt("idUsuario"),
                                        mensaje.getString("nombre"),
                                        mensaje.getString("mensaje"),
                                        mensaje.getString("fecha_formateada"),
                                        mensaje.optString("imgPersona")
                                ));
                            }
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            if (messages.size() > previousCount) {
                                messagesRecyclerView.scrollToPosition(messages.size() - 1);
                            }
                        });
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

            StringEntity entity = new StringEntity(params.toString(), "UTF-8");
            entity.setContentType("application/json");

            client.post(this, BASE_URL + "sendMessage.php", entity, "application/json",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "Respuesta del servidor: " + response.toString());
                            if (response.optBoolean("exito", false)) {
                                runOnUiThread(() -> {
                                    messageInput.setText("");
                                    loadMessages();
                                });
                            } else {
                                String error = response.optString("error", "Error al enviar el mensaje");
                                showError(error);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "Error enviando mensaje: " + responseString, throwable);
                            showError("Error de conexión al enviar el mensaje");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Error enviando mensaje: " + errorResponse, throwable);
                            showError("Error al procesar la respuesta del servidor");
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preparando mensaje", e);
            showError("Error al preparar el mensaje");
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(Chat.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void startMessageChecker() {
        if (messageChecker != null) {
            handler.removeCallbacks(messageChecker);
        }

        messageChecker = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    loadMessages();
                    handler.postDelayed(this, 5000);
                }
            }
        };
        handler.post(messageChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && messageChecker != null) {
            handler.removeCallbacks(messageChecker);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMessageChecker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && messageChecker != null) {
            handler.removeCallbacks(messageChecker);
        }
    }

    private static class Message {
        final int userId;
        final String userName;
        final String messageText;
        final String timeStamp;
        final String userImage;
        final int messageType; // 0: normal, 1: encuesta
        JSONObject pollData; // Para almacenar datos de la encuesta
        JSONObject pollVotes; // Para almacenar los votos de cada usuario

        Message(int userId, String userName, String messageText, String timeStamp, String userImage) {
            this(userId, userName, messageText, timeStamp, userImage, 0, null);
        }

        Message(int userId, String userName, String messageText, String timeStamp, String userImage, int messageType, JSONObject pollData) {
            this.userId = userId;
            this.userName = userName;
            this.messageText = messageText;
            this.timeStamp = timeStamp;
            this.userImage = userImage;
            this.messageType = messageType;
            this.pollData = pollData;

            try {
                if (pollData != null) {
                    this.pollVotes = pollData.optJSONObject("votes");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing poll votes", e);
            }
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_MESSAGE = 0;
        private static final int VIEW_TYPE_POLL = 1;

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).messageType;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_POLL) {
                View view = inflater.inflate(R.layout.poll_message_item, parent, false);
                return new PollViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.message_item, parent, false);
                return new MessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);
            if (holder instanceof PollViewHolder) {
                ((PollViewHolder) holder).bind(message);
            } else if (holder instanceof MessageViewHolder) {
                ((MessageViewHolder) holder).bind(message);
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

            void bind(Message message) {
                userName.setText(message.userName);
                messageText.setText(message.messageText);
                messageTime.setText(message.timeStamp);

                if (message.userImage != null && !message.userImage.isEmpty()) {
                    Glide.with(Chat.this)
                            .load(BASE_URL + message.userImage)
                            .circleCrop()
                            .error(R.drawable.ic_launcher_profile)
                            .into(userImage);
                } else {
                    userImage.setImageResource(R.drawable.ic_launcher_profile);
                }
            }
        }

        class PollViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView userName;
            TextView question;
            LinearLayout optionsContainer;
            TextView messageTime;
            Message currentMessage;

            PollViewHolder(@NonNull View itemView) {
                super(itemView);
                userImage = itemView.findViewById(R.id.userImage);
                userName = itemView.findViewById(R.id.userName);
                question = itemView.findViewById(R.id.pollQuestion);
                optionsContainer = itemView.findViewById(R.id.pollOptionsContainer);
                messageTime = itemView.findViewById(R.id.messageTime);
            }

            void bind(Message message) {
                this.currentMessage = message;
                userName.setText(message.userName);
                question.setText(message.messageText);
                messageTime.setText(message.timeStamp);

                if (message.userImage != null && !message.userImage.isEmpty()) {
                    Glide.with(Chat.this)
                            .load(BASE_URL + message.userImage)
                            .circleCrop()
                            .error(R.drawable.ic_launcher_profile)
                            .into(userImage);
                }

                optionsContainer.removeAllViews();
                try {
                    JSONArray options = message.pollData.getJSONArray("opciones");
                    for (int i = 0; i < options.length(); i++) {
                        String option = options.getString(i);
                        addPollOption(optionsContainer, option, message.pollData.getString("encuestaId"), i);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading poll options", e);
                }
            }

            private void addPollOption(LinearLayout container, String optionText, String pollId, int optionIndex) {
                View optionView = getLayoutInflater().inflate(R.layout.poll_option_item, container, false);
                TextView optionTextView = optionView.findViewById(R.id.optionText);
                optionTextView.setText(optionText);

                // Crear un objeto contenedor para los estados
                class VoteState {
                    boolean userVoted = false;
                    boolean isUserCurrentVote = false;
                }
                VoteState voteState = new VoteState();

                try {
                    if (currentMessage.pollVotes != null) {
                        JSONArray votes = currentMessage.pollVotes.optJSONArray(String.valueOf(optionIndex));
                        if (votes != null) {
                            for (int i = 0; i < votes.length(); i++) {
                                if (votes.getInt(i) == userId) {
                                    voteState.userVoted = true;
                                    voteState.isUserCurrentVote = true;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking votes", e);
                }

                // Aplicar estilo basado en el voto
                if (voteState.isUserCurrentVote) {
                    optionView.setBackgroundResource(R.drawable.poll_option_selected);
                    optionTextView.setTextColor(getResources().getColor(R.color.white));
                } else if (voteState.userVoted) {
                    optionView.setEnabled(false);
                    optionView.setAlpha(0.5f);
                }

                final View finalOptionView = optionView;
                final TextView finalOptionTextView = optionTextView;

                optionView.setOnClickListener(v -> {
                    if (!voteState.userVoted) {
                        votePollOption(pollId, optionIndex, finalOptionView, finalOptionTextView);
                    }
                });
                container.addView(optionView);
            }

            private void votePollOption(String pollId, int optionIndex, View optionView, TextView optionTextView) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("pollId", pollId);
                    params.put("userId", userId);
                    params.put("optionIndex", optionIndex);

                    StringEntity entity = new StringEntity(params.toString());
                    client.post(Chat.this, BASE_URL + "votePoll.php", entity, "application/json",
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    if (response.has("error")) {
                                        try {
                                            String errorMsg = response.getString("error");
                                            // Manejar el caso de voto duplicado
                                            if (errorMsg.contains("Duplicate entry")) {
                                                Toast.makeText(Chat.this, "Ya has votado en esta encuesta", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Chat.this, errorMsg, Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            Toast.makeText(Chat.this, "Error al votar", Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (response.optBoolean("exito", false)) {
                                        runOnUiThread(() -> {
                                            // Restablecer todas las opciones a su estado normal
                                            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                                                View child = optionsContainer.getChildAt(i);
                                                child.setBackgroundResource(R.drawable.poll_option_background);
                                                ((TextView) child.findViewById(R.id.optionText))
                                                        .setTextColor(getResources().getColor(R.color.poll_text_color));
                                                child.setAlpha(1.0f); // Mantener opacidad normal
                                                child.setEnabled(false); // Deshabilitar todas las opciones
                                            }

                                            // Resaltar la opción seleccionada
                                            optionView.setBackgroundResource(R.drawable.poll_option_selected);
                                            optionTextView.setTextColor(getResources().getColor(R.color.white));
                                            optionView.setAlpha(1.0f);
                                        });
                                        Toast.makeText(Chat.this, "Voto registrado", Toast.LENGTH_SHORT).show();
                                        loadMessages();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    Toast.makeText(Chat.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Error voting", e);
                    Toast.makeText(Chat.this, "Error al procesar el voto", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}