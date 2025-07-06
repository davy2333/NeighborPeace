package sv.edu.catolica.neighborpeace;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private int id;
    private int userId;
    private String userName;
    private String userRole;
    private String message;
    private String timeStamp;
    private String userImage;
    private int messageType;
    private boolean isReported;
    private PollData pollData;

    // Constructor
    public ChatMessage(int id, int userId, String userName, String userRole, String message,
                       String timeStamp, String userImage) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.message = message;
        this.timeStamp = timeStamp;
        this.userImage = userImage;
        this.messageType = 0;
        this.isReported = false;
    }

    // Getters y setters
    // ... (agregar todos los getters y setters necesarios)

    public static class PollData {
        private int pollId;
        private String question;
        private List<PollOption> options;

        public static class PollOption {
            private int id;
            private String text;
            private int votes;
            private boolean userVoted;

            public PollOption(int id, String text, int votes) {
                this.id = id;
                this.text = text;
                this.votes = votes;
                this.userVoted = false;
            }

            // Getters y setters
            // ... (agregar los getters y setters necesarios)
        }

        // Constructor
        public PollData(int pollId, String question) {
            this.pollId = pollId;
            this.question = question;
            this.options = new ArrayList<>();
        }

        // Getters y setters
        // ... (agregar los getters y setters necesarios)
    }
}