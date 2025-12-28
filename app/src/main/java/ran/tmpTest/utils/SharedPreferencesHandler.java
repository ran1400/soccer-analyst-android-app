package ran.tmpTest.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import ran.tmpTest.sharedData.EventsFragmentData;
import ran.tmpTest.sharedData.GameFragmentData;

public class SharedPreferencesHandler
{
    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor editor;

    public SharedPreferencesHandler(Context context)
    {
        sharedPreferences = context.getSharedPreferences("appData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void initValues()
    {
        GameFragmentData.initValues(sharedPreferences);
        EventsFragmentData.initValues(sharedPreferences);
    }

    public void saveDataInMemory()
    {
        EventsFragmentData.saveInMemory(editor,false);
        GameFragmentData.saveInMemory(editor,false);
        editor.apply();
    }

    public <T> T getData(String key, Type type, T defaultValue)
    {
        T data = null;
        String serializedObject = sharedPreferences.getString(key, null);
        if (serializedObject != null)
        {
            Gson gson = new Gson();
            data = gson.fromJson(serializedObject, type);
        }
        if (data == null)
            return defaultValue;
        return data;
    }

    public <T> void saveData(String key,T data,boolean toApply)
    {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        editor.putString(key, json);
        if (toApply)
            editor.apply();
    }
}
