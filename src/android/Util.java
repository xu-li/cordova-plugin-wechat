package xu.li.cordova.wechat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Util {

    public static File getCacheFolder(Context context) {
        File cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "cache");
            if (!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
        }

        if(!cacheDir.isDirectory()) {
            cacheDir = context.getCacheDir(); //get system cache folder
        }

        return cacheDir;
    }

    public static File downloadAndCacheFile(Context context, String url) {
        URL fileURL = null;
        try {
            fileURL = new URL(url);

            Log.d(Wechat.TAG, String.format("Start downloading file at %s.", url));

            HttpURLConnection connection = (HttpURLConnection) fileURL.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(Wechat.TAG, String.format("Failed to download file from %s, response code: %d.", url, connection.getResponseCode()));
                return null;
            }

            InputStream inputStream = connection.getInputStream();

            File cacheDir = getCacheFolder(context);
            File cacheFile = new File(cacheDir, url.substring(url.lastIndexOf("/") + 1));
            FileOutputStream outputStream = new FileOutputStream(cacheFile);

            byte buffer[] = new byte[4096];
            int dataSize;
            while ((dataSize = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, dataSize);
            }
            outputStream.close();

            Log.d(Wechat.TAG, String.format("File was downloaded and saved at %s.", cacheFile.getAbsolutePath()));

            return cacheFile;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
