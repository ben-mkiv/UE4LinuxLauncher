package launcher.objects;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCache {
    public static FileCache instance = new FileCache();

    private File cacheDirectory = new File(System.getProperty("user.home") + "/.cache/UE4LinuxLauncher");

    //todo: replace by something that fixes all special chars
    private String fixURL(String urlIn){
        return urlIn.replaceAll(" ", "%20");    // fix spaces in URL
    }

    public String readFile(String category, String identifier, String urlOrigin) throws Exception {
         String cacheURL = getFile(category, identifier, urlOrigin);

        try {
            return new String(Files.readAllBytes(new File(cacheURL).toPath()));
        } catch (IOException ex){
            System.out.println("failed to read cached file, ");
            ex.printStackTrace();
            throw new Exception("couldn't read cached file for URL " + urlOrigin);
        }
    }

    public String getFile(String category, String identifier, String urlOrigin){
        try {
            // strip of url parameters and domain, etc.
            URL url = new URL(fixURL(urlOrigin));
            String filename = Paths.get(new URL(urlOrigin).getPath()).getFileName().toString();

            File directory = new File(cacheDirectory.getAbsolutePath() + "/" + category + "/" + identifier);
            File file = new File(directory.getAbsolutePath() + "/" + filename);

            if(file.exists()){
                return "file://"+file.getAbsolutePath();
            } else {
                directory.mkdirs();
                return downloadFile(url, file.getAbsolutePath()) ? "file://"+file.getAbsolutePath() : urlOrigin;
            }
        }
        catch(MalformedURLException exception){
            //fallback to the original URL if something went wrong
            System.out.println("malformed URL: " + urlOrigin);
            exception.printStackTrace();
            return urlOrigin;
        }
    }

    private boolean downloadFile(URL url, String filename){
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return true;
        } catch (IOException exception) {
            System.out.println("IO Exception, failed to cache resource from URL: " + url.toString());
            exception.printStackTrace();
        }

        return false;
    }

}
