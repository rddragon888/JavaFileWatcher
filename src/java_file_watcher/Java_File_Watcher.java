
package java_file_watcher;

/**
 *
 * @author gbeard
 */
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Java_File_Watcher {

    /**
     * @param args the command line arguments
     */
    static String watcherLocation = "", backupFolder = "";

    public static void main(String[] args) {
        readSetupFile();
        displayMenu();
    }

    public static void displayMenu() {
        Scanner mInput = new Scanner(System.in);
        String sInput = "";

        System.out.println("File Watcher");
        System.out.println("******************************");
        System.out.println("1 - Start File Watcher Process");
        System.out.println("2 - Reports");
        System.out.println("Q - Exit Program");
        System.out.println("******************************");

        System.out.print(">");
        getInput(sInput = mInput.next());

    }

    public static void readSetupFile() {
        try {
            File setupFile = new File("setup.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(setupFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("filelocation");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    watcherLocation = eElement.getElementsByTagName("driveLetter").item(0).getTextContent();
                    backupFolder = eElement.getElementsByTagName("backupfolder").item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void getInput(String input) {
        switch (input) {
            case "1": //Start filewatcher process
                startFileWatcher();
                break;
            case "2": //Display reports menu
                break;
            case "Q": //Exit program
                break;            
            default:
                System.out.println("Please enter 1, 2, or Q.");
                displayMenu();
                break;

        }
    }

    public static void startFileWatcher() {

        try {

            //Path dir = Paths.get(URI.create("file:/D:/HDW"));
            Path dir = Paths.get(URI.create("file:/" + watcherLocation));
            
            if (Files.exists(dir)) {
                System.out.println("Path Exist");
                WatchService watcher = FileSystems.getDefault().newWatchService();
                try {
                    WatchKey key = dir.register(watcher, ENTRY_CREATE);
                } catch (IOException x) {
                    System.out.println(x.toString());
                }

                for (;;) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        System.out.println("File Created: " + filename.toString());
                        Path filePath = Paths.get(URI.create("file:/" + watcherLocation + "/" + filename));
                        Path dirBackup = Paths.get(URI.create("file:/" + backupFolder + "/" + filename));
                        
                        Path temp = Files.move(filePath, dirBackup, REPLACE_EXISTING);
                        
                        if(temp != null)
                        {
                            System.out.println(filename + " moved to backup folder");
                        }
                        else
                        {
                            System.out.println(filename + " file move error.");
                        }
                    }
                    boolean vaild = key.reset();
                    if (!vaild) {
                        break;
                    }
                }
            } else {
                System.out.println("Path Does Not Exist");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
