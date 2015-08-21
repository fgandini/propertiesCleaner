package pops.cleaner.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;


public class Main {

    public static void main(String[] args) {

        if (args == null) {
            System.out.println("You must specify your .properties path and project path.");
            System.exit(0);
        }
        if (args[0].isEmpty()) {
            System.out.println("You must specify your .properties path.");
            System.exit(0);
        }
        if (args[1].isEmpty()) {
            System.out.println("You must specify your project path.");
            System.exit(0);
        }     

        Path propertiesFilePath = Paths.get(args[0]);
        Path projectDirectoryPath = Paths.get(args[1]);   

        Properties propertiesFromFile = new Properties();
        Properties propertiesFoundInDirectory = new Properties();

        try {
            propertiesFromFile = getKeysFromProperties(propertiesFilePath);
            propertiesFoundInDirectory.putAll(searchInDirectory(propertiesFromFile, projectDirectoryPath));
        } catch (IOException e) {
            System.out.println("ohh crap...error when trying to read properties file!");
            e.printStackTrace();
            System.exit(0);
        }

        Properties notFoundKeys = getNotFoundKeys(propertiesFromFile, propertiesFoundInDirectory);

        if (!notFoundKeys.isEmpty()) {
            System.out.println("####-- Keys not found --####");
            notFoundKeys.forEach((k,v) -> {
                System.out.println(k.toString());
            });
        }   

        saveNewFileProperties(propertiesFoundInDirectory, propertiesFilePath);
    }

    public static Properties searchInFile(Properties properties, Path filePath) throws FileNotFoundException {

        BufferedReader br = null;
        Properties propertiesWithFoundKeysFile = new Properties();
        String line = "";
        int lineNumber = 0;

        br = new BufferedReader(new FileReader(filePath.toString()));

        try {
            while((line = br.readLine()) != null)
            {
                lineNumber++;

                for (Entry<Object, Object> prop : properties.entrySet()) {
                    if(line.matches(".*\"" + prop.getKey().toString() + "\".*")){
                        propertiesWithFoundKeysFile.setProperty(prop.getKey().toString(), prop.getValue().toString());
                        System.out.println("Found key - " + prop.getKey().toString() + " - on line " + lineNumber + " - on file - " + filePath.getFileName());     
                    }
                }
            }

        } catch (IOException e) {} 

        try {
            br.close();
        } catch (IOException e) {}       

        return propertiesWithFoundKeysFile;
    }

    private static void saveNewFileProperties(Properties foundKeys, Path propertiesPath){

        FileOutputStream fOS;

        try {
            fOS = new FileOutputStream(propertiesPath.toString() + "_CLEAN");
            foundKeys.store(fOS, "");
            fOS.flush();
            fOS.close();        
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties getNotFoundKeys(Properties propertiesFromFile, Properties propertiesFoundDirectory) {

        Properties notFoundKey = new Properties();

        for (Entry<Object, Object> prop : propertiesFromFile.entrySet()) {
            if(!propertiesFoundDirectory.containsKey(prop.getKey())) {
                notFoundKey.put(prop.getKey(), prop.getValue());
            }            
        }

        return notFoundKey;
    }

    private static Properties searchInDirectory(Properties properties, Path directoryPath) {

        Properties propertiesWithFoundKeysInDirectory = new Properties();

        try {
            Files.walk(directoryPath)
                .filter(c -> Files.isRegularFile(c))
                .forEach(file -> {
                try {
                    propertiesWithFoundKeysInDirectory.putAll(searchInFile(properties, file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {            
            e.printStackTrace();
        }

        return propertiesWithFoundKeysInDirectory;
    }

    private static Properties getKeysFromProperties(Path propertiesPath) throws FileNotFoundException, IOException  {

        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesPath.toString()));

        return properties;
    }
}
