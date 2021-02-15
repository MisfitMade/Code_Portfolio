package main;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class Loader {

    public static <T> T loadFxmlFile(String fileName){
        FXMLLoader loader = new FXMLLoader(Loader.class.getResource(fileName));

        T root = null;

        try {
            root = loader.load();
        }
        catch (IOException e){
            System.out.println(fileName);
            //e.printStackTrace();
        }

        return root;
    }
}
