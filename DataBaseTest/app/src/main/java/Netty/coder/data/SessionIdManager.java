package Netty.coder.data;

public class SessionIdManager {
    private static int SESSION_ID=0;

    public void setSessionId(int sessionID) {
        SessionIdManager.SESSION_ID = sessionID;
    }
    public int getSessionId() {
        return SessionIdManager.SESSION_ID;
    }

}