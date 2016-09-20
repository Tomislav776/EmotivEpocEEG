package model;

import javafx.scene.image.Image;


import java.io.File;
import java.util.ArrayList;


public class ImageHandler {

    private String path = System.getProperty("user.dir") + File.separator + "Documents" + File.separator + "Photos" + File.separator;
    private String[] extens;

    ArrayList<Image> photos = new ArrayList<Image>();



    public File[] photo () {
        String a="", b="";
        String[] value;
        int counter = 0;


        File repo = new File(path);

        File[] fileList = repo.listFiles();

        return fileList;
    }


}
