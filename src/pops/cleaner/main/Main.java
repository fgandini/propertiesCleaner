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
        Properties propertiesFoundedDirectory = new Properties();

        try {
            propertiesFromFile = getKeysFromProperties(propertiesFilePath);
            propertiesFoundedDirectory.putAll(searchInDirectory(propertiesFromFile, projectDirectoryPath));
        } catch (IOException e) {
            System.out.println("ohh crap...error when trying to read properties file!");
            e.printStackTrace();
            System.exit(0);
        }

        Properties notFoundedKeys = getNotFoundedKeys(propertiesFromFile, propertiesFoundedDirectory);

        if (!notFoundedKeys.isEmpty()) {
            System.out.println("####-- Keys not found --####");
            notFoundedKeys.forEach((k,v) -> {
                System.out.println(k.toString());
            });
        }   

        saveNewFileProperties(propertiesFoundedDirectory, propertiesFilePath);
    }

    public static Properties searchInFile(Properties properties, Path filePath) throws FileNotFoundException {

        BufferedReader br = null;
        Properties propertiesWithFoundedKeysFile = new Properties();
        String line = "";
        int lineNumber = 0;

        br = new BufferedReader(new FileReader(filePath.toString()));

        try {
            while((line = br.readLine()) != null)
            {
                lineNumber++;

                for (Entry<Object, Object> prop : properties.entrySet()) {
                    if(line.matches(".*\"" + prop.getKey().toString() + "\".*")){
                        propertiesWithFoundedKeysFile.setProperty(prop.getKey().toString(), prop.getValue().toString());
                        System.out.println("Found key - " + prop.getKey().toString() + " - on line " + lineNumber + " - on file - " + filePath.getFileName());     
                    }
                }
            }

        } catch (IOException e) {} 

        try {
            br.close();
        } catch (IOException e) {}       

        return propertiesWithFoundedKeysFile;
    }

    private static void saveNewFileProperties(Properties foundedKeys, Path propertiesPath){

        FileOutputStream fOS;

        try {
            fOS = new FileOutputStream(propertiesPath.toString() + "_CLEAN");
            foundedKeys.store(fOS, "");
            fOS.flush();
            fOS.close();        
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties getNotFoundedKeys(Properties propertiesFromFile, Properties propertiesFoundedDirectory) {

        Properties notFoundedKey = new Properties();

        for (Entry<Object, Object> prop : propertiesFromFile.entrySet()) {
            if(!propertiesFoundedDirectory.containsKey(prop.getKey())) {
                notFoundedKey.put(prop.getKey(), prop.getValue());
            }            
        }

        return notFoundedKey;
    }

    private static Properties searchInDirectory(Properties properties, Path directoryPath) {

        Properties propertiesWithFoundedKeysDirectory = new Properties();

        try {
            Files.walk(directoryPath)
                .filter(c -> Files.isRegularFile(c))
                .forEach(file -> {
                try {
                    propertiesWithFoundedKeysDirectory.putAll(searchInFile(properties, file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {            
            e.printStackTrace();
        }

        return propertiesWithFoundedKeysDirectory;
    }

    private static Properties getKeysFromProperties(Path propertiesPath) throws FileNotFoundException, IOException  {

        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesPath.toString()));

        return properties;
    }
}