import java.io.BufferedReader;
import java.io.PrintWriter;

public class ConnectionPair {

    private PrintWriter value;
    private BufferedReader key;

    public ConnectionPair(BufferedReader key, PrintWriter value){
        this.key = key;
        this.value = value;
    }

    public PrintWriter getValue() {
        return value;
    }

    public void setValue(PrintWriter value) {
        this.value = value;
    }

    public BufferedReader getKey() {
        return key;
    }

    public void setKey(BufferedReader key) {
        this.key = key;
    }
}
