import org.mindrot.jbcrypt.BCrypt;

public class Temp {
    public static void main(String[] args) {
        String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());
        System.out.println("HASH=" + hashed);
    }
}
