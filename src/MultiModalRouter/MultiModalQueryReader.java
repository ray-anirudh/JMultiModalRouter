package src.MultiModalRouter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

public class MultiModalQueryReader {
    LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

    public void readMultiModalQueries(String multiModalQueriesFilePath) {
        try {
            // Reader for multi-modal queries file todo finish this bitch
            BufferedReader multiModalQueriesReader = new BufferedReader(new FileReader(multiModalQueriesFilePath));

        } catch (IOException iOE) {

        } catch (FileNotFoundException fNFE) {

        }
    }

    private int void findIndexInArray() {

    }

    public LinkedHashMap<Integer, MultiModalQuery> getMultiModalQueries() {
        return this.multiModalQueries;
    }
}
