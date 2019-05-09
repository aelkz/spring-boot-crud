package com.aelkz.springboot.skeleton.util;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;

public class MavenUtils {

    private static MavenUtils INSTANCE = null;

    private MavenUtils() { }

    public static MavenUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MavenUtils();
        }
        return INSTANCE;
    }

    public String artifactVersion() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileReader("pom.xml"));
            return model.getVersion();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

}
