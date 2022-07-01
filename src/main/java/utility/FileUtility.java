package utility;

import alert.JdkFuncAlert;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class FileUtility {

    public static Map<String, Object> readYamlFile(String filePathStr){
        Map<String, Object> yamlMap = new HashMap<>();
        Yaml yaml = new Yaml();
        try{
            yamlMap = yaml.load(new String(readFile(filePathStr)));

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return yamlMap;
    }

    public static byte[] readFile(String filePathStr){
        if(filePathStr.startsWith("jar:") || filePathStr.startsWith("file:")){
            return readFileByURL(filePathStr);
        }else{
            return readFileByLocal(filePathStr);
        }
    }

    public static byte[] readFileByURL(String filePathStr){
        // jar:file:/Users/lufei/psi_demo-2.0.0.jar!/sink.yaml
        byte[] result = new byte[]{};
        URL url = null;
        try {
            url = new URL(filePathStr);
            InputStream is = url.openStream();
            result = is.readAllBytes();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public static byte[] readFileByLocal(String filePathStr) {
        byte[] result = new byte[]{};
        File file = new File(filePathStr);
        try {
            InputStream in = new FileInputStream(file);
            result = in.readAllBytes();
            in.close();
        }catch (Exception e){

        }
        return result;
    }

    public static void saveFile(String filePathStr,byte[] data) {
        File file  = new File(filePathStr);
        File directory = new File(file.getParent());
        if(!directory.exists()){
            directory.mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data,0,data.length);
            fos.flush();
            fos.close();
        }catch (Exception e){}
    }

    public static void main(String[] args) {


    }
}

