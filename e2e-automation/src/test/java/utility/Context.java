package test.java.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

// Singleton that keeps test context variables across different test sessions
public class Context {

    private static Context ourInstance = new Context();
    public String recoveryPhrase, backupFileName;

    public static Context getInstance() {
        return ourInstance;
    }

    private Context() {}

    public void getContext() {
        try {
            Reader reader = new FileReader("ctx.json");
            Gson gson = new GsonBuilder().create();
            Context temp = gson.fromJson(reader, Context.class);
            reader.close();
            ourInstance.recoveryPhrase = temp.recoveryPhrase;
            ourInstance.backupFileName = temp.backupFileName;
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    public void dumpContext() {
        try {
            Writer writer = new FileWriter("ctx.json");
            Gson gson = new GsonBuilder().create();
            gson.toJson(Context.getInstance(), writer);
            writer.flush();
            writer.close();
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

}
