package DatabaseVersion;

public class DatabaseVersionManager {
    public int VERSION = 13;
    private String LoginUserName;
    public int getVERSION(){
        return VERSION;
    }
    public boolean setUserNow(String UserName){
        if(UserName!=null){
            LoginUserName = UserName;
            return true;
        }
        return false;
    }
}
