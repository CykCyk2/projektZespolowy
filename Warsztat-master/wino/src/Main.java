import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String args[]) {

        try {
            new JRC();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
