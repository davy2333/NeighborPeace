package sv.edu.catolica.neighborpeace;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatAdmin extends AppCompatActivity {
    private static final String BASE_URL = "http://192.168.1.125:80/WebServicesphp/";
    private static final String TAG = "ChatAdmin";

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton pollButton;
    private ImageButton addRoleButton;
    private ImageView backButton;
    private TextView chatTitle;

    private AdminChatAdapter adapter;
    private AsyncHttpClient client;
    private Handler handler = new Handler();
    private Runnable messageChecker;

    private int problemId;
    private int userId;
    private String problemTitle;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_admin);

        // Initialize SharedPreferences inside onCreate
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userType = prefs.getString("userType", "");
        userId = prefs.getInt("userId", -1);

        client = new AsyncHttpClient();

        // Obtener datos del problema
        problemId = getIntent().getIntExtra("problem_id", -1);
        problemTitle = getIntent().getStringExtra("problem_title");

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

        // Código de depuración
        Log.d("ChatAdmin", "Usuario tipo: " + userType);
        Log.d("ChatAdmin", "Problem ID: " + problemId);
        Log.d("ChatAdmin", "Problem Title: " + problemTitle);
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        pollButton = findViewById(R.id.poll_button);
        addRoleButton = findViewById(R.id.add_role_button);
        backButton = findViewById(R.id.back_button);
        chatTitle = findViewById(R.id.chatTitle);

        chatTitle.setText(problemTitle);
    }

    private void setupRecyclerView() {
        adapter = new AdminChatAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());
        pollButton.setOnClickListener(v -> showPollDialog());
        addRoleButton.setOnClickListener(v -> showRoleDialog());
    }

    private void assignRole(int targetUserId, String role) {
        try {
            JSONObject params = new JSONObject();
            params.put("userId", targetUserId);
            params.put("problemId", problemId);
            params.put("rol", role);

            Log.d(TAG, "Enviando datos de rol: " + params.toString());

            // Convertir a StringEntity con codificación UTF-8
            StringEntity entity = new StringEntity(params.toString(), "UTF-8");
            entity.setContentType("application/json");

            RequestParams requestParams = new RequestParams();
            requestParams.put("userId", targetUserId);
            requestParams.put("problemId", problemId);
            requestParams.put("rol", role);

            // Usar POST con RequestParams en lugar de StringEntity
            client.post(this, BASE_URL + "assignRole.php", requestParams,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "Respuesta completa de assignRole: " + response.toString());
                            runOnUiThread(() -> {
                                try {
                                    if (response.has("exito") && response.getBoolean("exito")) {
                                        Toast.makeText(ChatAdmin.this,
                                                "Rol asignado correctamente", Toast.LENGTH_SHORT).show();
                                        loadMessages();
                                    } else {
                                        String error = response.optString("error", "Error al asignar rol");
                                        Toast.makeText(ChatAdmin.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error procesando respuesta: " + e.getMessage());
                                    Toast.makeText(ChatAdmin.this,
                                            "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Error en assignRole: " + throwable.getMessage());
                            if (errorResponse != null) {
                                Log.e(TAG, "Error response: " + errorResponse.toString());
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(ChatAdmin.this,
                                        "Error al asignar el rol", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al asignar rol: " + e.getMessage());
            Toast.makeText(this, "Error al asignar el rol", Toast.LENGTH_SHORT).show();
        }
    }

    // También asegúrate que el método showPollDialog esté configurado correctamente:
    private void showPollDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_poll, null);
        EditText questionEdit = dialogView.findViewById(R.id.questionEdit);
        LinearLayout optionsContainer = dialogView.findViewById(R.id.optionsContainer);
        Button addOptionButton = dialogView.findViewById(R.id.addOptionButton);

        // Agregar dos opciones por defecto
        for (int i = 0; i < 2; i++) {
            View optionView = getLayoutInflater().inflate(R.layout.item_poll_option_edit, optionsContainer, false);
            optionsContainer.addView(optionView);
        }

        addOptionButton.setOnClickListener(v -> {
            View optionView = getLayoutInflater().inflate(R.layout.item_poll_option_edit, optionsContainer, false);
            optionView.findViewById(R.id.removeOptionButton).setOnClickListener(v2 -> {
                if (optionsContainer.getChildCount() > 2) {
                    optionsContainer.removeView(optionView);
                } else {
                    Toast.makeText(this, "Se requieren al menos 2 opciones", Toast.LENGTH_SHORT).show();
                }
            });
            optionsContainer.addView(optionView);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Crear Encuesta")
                .setView(dialogView)
                .setPositiveButton("Crear", null) // Se configura después
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String question = questionEdit.getText().toString().trim();
                if (question.isEmpty()) {
                    Toast.makeText(this, "Ingrese una pregunta", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> options = new ArrayList<>();
                for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                    View optionView = optionsContainer.getChildAt(i);
                    EditText optionEdit = optionView.findViewById(R.id.optionEdit);
                    String option = optionEdit.getText().toString().trim();
                    if (!option.isEmpty()) {
                        options.add(option);
                    }
                }

                if (options.size() < 2) {
                    Toast.makeText(this, "Ingrese al menos 2 opciones", Toast.LENGTH_SHORT).show();
                    return;
                }

                createPoll(question, options);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void loadMessages() {
        String url = BASE_URL + "getChatMessages.php?problemId=" + problemId;
        Log.d(TAG, "Cargando mensajes desde: " + url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Respuesta recibida: " + response.toString());
                try {
                    if (response.has("exito") && response.getBoolean("exito")) {
                        JSONArray mensajesArray = response.getJSONArray("mensajes");
                        Log.d(TAG, "Total mensajes: " + mensajesArray.length());

                        runOnUiThread(() -> {
                            adapter.updateMessages(mensajesArray);
                            if (adapter.getItemCount() > 0) {
                                messagesRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
                            }
                        });
                    } else {
                        String error = response.optString("error", "Error desconocido");
                        Log.e(TAG, "Error en la respuesta: " + error);
                        showToastOnUiThread("Error: " + error);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando respuesta", e);
                    showToastOnUiThread("Error procesando los mensajes");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "Error cargando mensajes: " + statusCode + ", " + responseString);
                Log.e(TAG, "Throwable: ", throwable);
                showToastOnUiThread("Error de conexión al cargar mensajes");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Error JSON: " + errorResponse);
                Log.e(TAG, "Throwable: ", throwable);
                showToastOnUiThread("Error en la respuesta del servidor");
            }
        });
    }

    // Agregar este método en la clase ChatAdmin
    private void showToastOnUiThread(final String message) {
        runOnUiThread(() -> {
            Toast.makeText(ChatAdmin.this, message, Toast.LENGTH_LONG).show();
        });
    }

    // Modificar el método sendMessage para usar RequestParams también
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        try {
            RequestParams params = new RequestParams();
            params.put("problemId", problemId);
            params.put("userId", userId);
            params.put("mensaje", messageText);
            params.put("tipo_mensaje", 0);

            Log.d(TAG, "Enviando mensaje con params: " + params.toString());

            client.post(BASE_URL + "sendAdminMessage.php", params,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            runOnUiThread(() -> {
                                if (response.optBoolean("exito", false)) {
                                    messageInput.setText("");
                                    loadMessages();
                                    Toast.makeText(ChatAdmin.this,
                                            "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                } else {
                                    String error = response.optString("error", "Error al enviar el mensaje");
                                    Toast.makeText(ChatAdmin.this, error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Error enviando mensaje: " + throwable.getMessage());
                            runOnUiThread(() -> {
                                Toast.makeText(ChatAdmin.this,
                                        "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error construyendo mensaje: " + e.getMessage());
            Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPoll(String question, List<String> options) {
        try {
            // Convertir la lista de opciones a un JSONArray
            JSONArray optionsArray = new JSONArray();
            for (String option : options) {
                optionsArray.put(option);
            }

            RequestParams params = new RequestParams();
            params.put("problemId", problemId);
            params.put("userId", userId);
            params.put("pregunta", question);
            // Enviar el JSONArray como string
            params.put("opciones", optionsArray.toString());
            params.put("tipo_mensaje", 1);

            Log.d(TAG, "Enviando encuesta: " + params.toString());

            client.post(BASE_URL + "createPoll.php", params,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "Respuesta createPoll: " + response.toString());
                            runOnUiThread(() -> {
                                try {
                                    if (response.has("exito") && response.getBoolean("exito")) {
                                        Toast.makeText(ChatAdmin.this,
                                                "Encuesta creada correctamente", Toast.LENGTH_SHORT).show();
                                        loadMessages();
                                    } else {
                                        String error = response.optString("error", "Error al crear la encuesta");
                                        Log.e(TAG, "Error desde servidor: " + error);
                                        Toast.makeText(ChatAdmin.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error procesando respuesta: " + e.getMessage());
                                    Toast.makeText(ChatAdmin.this,
                                            "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Error en createPoll: " + throwable.getMessage());
                            if (errorResponse != null) {
                                Log.e(TAG, "Error response: " + errorResponse.toString());
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(ChatAdmin.this,
                                        "Error al crear la encuesta", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              String responseString, Throwable throwable) {
                            Log.e(TAG, "Error en createPoll: " + throwable.getMessage());
                            Log.e(TAG, "Response string: " + responseString);
                            runOnUiThread(() -> {
                                Toast.makeText(ChatAdmin.this,
                                        "Error al crear la encuesta", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al crear encuesta: " + e.getMessage());
            Toast.makeText(this, "Error al crear la encuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRoleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_assign_role, null);

        EditText searchUserEdit = dialogView.findViewById(R.id.searchUserEdit);
        final Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);

        // Configurar el Spinner
        String[] roles = {"REPORTADOR", "INGENIERO", "SUPERVISOR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        builder.setView(dialogView)
                .setTitle("Asignar Rol")
                .setPositiveButton("Buscar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();

        // Configurar el botón después de crear el diálogo
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String searchTerm = searchUserEdit.getText().toString();
                String selectedRole = roleSpinner.getSelectedItem().toString();

                if (!searchTerm.isEmpty()) {
                    searchUsers(searchTerm, selectedRole);
                } else {
                    Toast.makeText(ChatAdmin.this,
                            "Ingrese un nombre para buscar", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void searchUsers(String searchTerm, String selectedRole) {
        String url = BASE_URL + "getChatUsers.php?problemId=" + problemId;
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("exito")) {
                        JSONArray users = response.getJSONArray("usuarios");
                        showUserSelectionDialog(users, searchTerm, selectedRole);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error searching users", e);
                    Toast.makeText(ChatAdmin.this,
                            "Error al buscar usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Error en la búsqueda: " + throwable.getMessage());
                Toast.makeText(ChatAdmin.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserSelectionDialog(JSONArray users, String searchTerm, String selectedRole) {
        ArrayList<String> matchingUsers = new ArrayList<>();
        ArrayList<Integer> userIds = new ArrayList<>();

        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userName = user.getString("nombre");
                if (userName.toLowerCase().contains(searchTerm.toLowerCase())) {
                    matchingUsers.add(userName);
                    userIds.add(user.getInt("id"));
                }
            }

            if (matchingUsers.isEmpty()) {
                Toast.makeText(this, "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] userNames = matchingUsers.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Seleccionar Usuario")
                    .setItems(userNames, (dialog, which) -> {
                        int selectedUserId = userIds.get(which);
                        assignRole(selectedUserId, selectedRole);
                        dialog.dismiss();
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing user selection", e);
            Toast.makeText(this, "Error al mostrar usuarios", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMessageChecker() {
        messageChecker = () -> {
            loadMessages();
            handler.postDelayed(messageChecker, 5000);
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

    public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.MessageViewHolder> {
        private List<JSONObject> messages = new ArrayList<>();

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_admin, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            try {
                JSONObject message = messages.get(position);
                holder.bind(message);
            } catch (Exception e) {
                Log.e(TAG, "Error binding message", e);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        void updateMessages(JSONArray newMessages) {
            try {
                messages.clear();
                for (int i = 0; i < newMessages.length(); i++) {
                    JSONObject mensaje = newMessages.getJSONObject(i);
                    // Log para depuración
                    Log.d(TAG, "Procesando mensaje: " + mensaje.toString());
                    messages.add(mensaje);
                }
                Log.d(TAG, "Total mensajes actualizados: " + messages.size());
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error en updateMessages", e);
            }
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView userName, messageText, messageTime, userRole;
            ImageButton messageOptions;

            MessageViewHolder(View itemView) {
                super(itemView);
                userImage = itemView.findViewById(R.id.userImage);
                userName = itemView.findViewById(R.id.userName);
                messageText = itemView.findViewById(R.id.messageText);
                messageTime = itemView.findViewById(R.id.messageTime);
                userRole = itemView.findViewById(R.id.userRole);
                messageOptions = itemView.findViewById(R.id.messageOptions);
            }

            void bind(JSONObject message) {
                try {
                    // Log para depuración
                    Log.d(TAG, "Binding mensaje: " + message.toString());

                    messageText.setVisibility(View.VISIBLE);
                    String messageContent = message.getString("mensaje");
                    int messageType = message.optInt("tipo_mensaje", 0);

                    if (messageType == 0) {
                        // Es un mensaje normal
                        messageText.setText(messageContent);

                        // Limpiar cualquier vista de encuesta anterior
                        ViewGroup parent = (ViewGroup) messageText.getParent();
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            View child = parent.getChildAt(i);
                            if (child.getTag() != null && child.getTag().equals("poll_view")) {
                                parent.removeView(child);
                            }
                        }
                    } else if (messageType == 1) {
                        // Es una encuesta
                        showPollMessage(new JSONObject(messageContent));
                    }

                    messageTime.setText(message.getString("fecha_formateada"));
                    userName.setText(message.getString("nombre"));

                    String role = message.optString("rol", "");
                    if (!role.isEmpty()) {
                        userRole.setVisibility(View.VISIBLE);
                        userRole.setText("(" + role + ")");

                        int colorRes = role.equals("SUPERVISOR") ?
                                android.R.color.holo_orange_light : R.color.Morado;
                        int color = ContextCompat.getColor(ChatAdmin.this, colorRes);
                        userRole.setTextColor(color);
                        userName.setTextColor(color);
                    } else {
                        userRole.setVisibility(View.GONE);
                        userName.setTextColor(ContextCompat.getColor(ChatAdmin.this,
                                android.R.color.darker_gray));
                    }

                    String imgUrl = message.optString("imgPersona", "");
                    if (!imgUrl.isEmpty()) {
                        Glide.with(ChatAdmin.this)
                                .load(BASE_URL + imgUrl)
                                .circleCrop()
                                .error(R.drawable.ic_launcher_profile)
                                .into(userImage);
                    } else {
                        userImage.setImageResource(R.drawable.ic_launcher_profile);
                    }

                    messageOptions.setOnClickListener(v -> {
                        PopupMenu popup = new PopupMenu(ChatAdmin.this, messageOptions);
                        popup.inflate(R.menu.message_options_menu);
                        popup.setOnMenuItemClickListener(item -> {
                            if (item.getItemId() == R.id.action_report) {
                                reportMessage(message);
                                return true;
                            }
                            return false;
                        });
                        popup.show();
                    });

                    boolean isReported = message.optBoolean("reportado", false);
                    if (isReported) {
                        messageText.setAlpha(0.5f);
                        messageText.setPaintFlags(messageText.getPaintFlags() |
                                android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        messageText.setAlpha(1.0f);
                        messageText.setPaintFlags(messageText.getPaintFlags() &
                                ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error binding message", e);
                }
            }

            private void showPollMessage(JSONObject message) {
                try {
                    JSONObject pollData;
                    // Verificar si el mensaje ya es un objeto JSON o necesita ser parseado
                    if (message.has("tipo") && message.getString("tipo").equals("encuesta")) {
                        pollData = message;
                    } else {
                        pollData = new JSONObject(message.getString("mensaje"));
                    }

                    // Inflar layout de encuesta
                    View pollView = LayoutInflater.from(ChatAdmin.this)
                            .inflate(R.layout.item_poll_message, null);

                    TextView questionText = pollView.findViewById(R.id.pollQuestion);
                    LinearLayout optionsContainer = pollView.findViewById(R.id.pollOptionsContainer);
                    TextView resultsText = pollView.findViewById(R.id.pollResults);

                    // Mostrar la pregunta
                    questionText.setText(pollData.getString("pregunta"));

                    // Limpiar contenedor actual
                    messageText.setVisibility(View.GONE);
                    ViewGroup parent = (ViewGroup) messageText.getParent();

                    // Remover vista anterior de encuesta si existe
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        if (child.getTag() != null && child.getTag().equals("poll_view")) {
                            parent.removeView(child);
                            break;
                        }
                    }

                    // Agregar la nueva vista de encuesta
                    pollView.setTag("poll_view");
                    parent.addView(pollView, parent.indexOfChild(messageText) + 1);

                    // Cargar las opciones
                    JSONArray options = new JSONArray(pollData.getString("opciones"));
                    loadPollOptions(pollData.getInt("encuestaId"), options, optionsContainer, resultsText);

                } catch (Exception e) {
                    Log.e(TAG, "Error showing poll message: " + e.getMessage());
                }
            }

            private void loadPollOptions(int pollId, JSONArray options, LinearLayout container, TextView resultsText) {
                container.removeAllViews();
                try {
                    int totalVotes = 0;

                    for (int i = 0; i < options.length(); i++) {
                        String option = options.getString(i);
                        View optionView = LayoutInflater.from(ChatAdmin.this)
                                .inflate(R.layout.item_poll_option, container, false);

                        TextView optionText = optionView.findViewById(R.id.optionText);
                        TextView voteCount = optionView.findViewById(R.id.voteCount);

                        optionText.setText(option);

                        // Hacer la opción clickeable
                        optionView.setOnClickListener(v -> votePoll(pollId, option));

                        container.addView(optionView);
                    }

                    // Actualizar resultados desde el servidor
                    updatePollResults(pollId, resultsText);

                } catch (Exception e) {
                    Log.e(TAG, "Error loading poll options", e);
                }
            }

            private void addPollOption(LinearLayout container, JSONObject option) {
                try {
                    View optionView = LayoutInflater.from(ChatAdmin.this)
                            .inflate(R.layout.item_poll_option, container, false);

                    TextView optionText = optionView.findViewById(R.id.optionText);
                    TextView voteCount = optionView.findViewById(R.id.voteCount);

                    optionText.setText(option.getString("opcion"));
                    voteCount.setText(option.getInt("votos") + " votos");

                    // Marcar opción si el usuario ya votó
                    if (option.optBoolean("userVoted", false)) {
                        optionView.setBackgroundResource(R.drawable.poll_option_selected_background);
                    }

                    container.addView(optionView);
                } catch (Exception e) {
                    Log.e(TAG, "Error adding poll option", e);
                }
            }

            private void updatePollResults(int pollId, TextView resultsText) {
                String url = BASE_URL + "getPollResults.php?pollId=" + pollId;
                client.get(url, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if (response.has("mensajes")) {
                                JSONArray mensajes = response.getJSONArray("mensajes");
                                for (int i = 0; i < mensajes.length(); i++) {
                                    JSONObject mensaje = mensajes.getJSONObject(i);
                                    String nombreUsuario = mensaje.getString("nombre");
                                    String mensajeTexto = mensaje.getString("mensaje");
                                    String rol = mensaje.getString("rol");
                                    String imgPersona = mensaje.getString("imgPersona");
                                    String fechaFormateada = mensaje.getString("fecha_formateada");
                                    // Aquí puedes actualizar la interfaz de usuario con los datos del mensaje
                                }
                            } else {
                                Log.e(TAG, "Respuesta JSON inesperada");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error procesando respuesta JSON", e);
                        }
                    }
                });
            }

            private void votePoll(int pollId, String option) {
                try {
                    RequestParams params = new RequestParams();
                    params.put("pollId", pollId);
                    params.put("userId", userId);
                    params.put("option", option);

                    client.post(BASE_URL + "votePoll.php", params,
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    if (response.optBoolean("exito", false)) {
                                        Toast.makeText(ChatAdmin.this,
                                                "Voto registrado", Toast.LENGTH_SHORT).show();
                                        loadMessages();
                                    } else {
                                        Toast.makeText(ChatAdmin.this,
                                                "Error al votar", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers,
                                                      Throwable throwable, JSONObject errorResponse) {
                                    Toast.makeText(ChatAdmin.this,
                                            "Error al votar", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Error voting poll", e);
                }
            }
        }
    }

    private void reportMessage(JSONObject message) {
        try {
            JSONObject params = new JSONObject();
            params.put("messageId", message.getInt("id"));
            params.put("userId", userId);

            StringEntity entity = new StringEntity(params.toString());
            client.post(this, BASE_URL + "reportMessage.php", entity, "application/json",
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            if (response.optBoolean("exito", false)) {
                                Toast.makeText(ChatAdmin.this,
                                        "Mensaje reportado correctamente", Toast.LENGTH_SHORT).show();
                                loadMessages();
                            } else {
                                Toast.makeText(ChatAdmin.this,
                                        "Error al reportar el mensaje", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(ChatAdmin.this,
                                    "Error de conexión al reportar", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error reporting message", e);
            Toast.makeText(this, "Error al reportar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }
}