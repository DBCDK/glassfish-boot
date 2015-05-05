package dk.dbc.glassfishboot

import groovy.transform.ToString


@ToString
public class GlassfishApp {
    String path;
    File file;

    boolean hasPath() {
        path != null
    }
}