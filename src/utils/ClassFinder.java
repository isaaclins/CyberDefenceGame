package src.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassFinder {

    public static List<Class<?>> findSubclasses(String packageName, Class<?> superclass) {
        List<Class<?>> subclasses = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                if (directory.exists()) {
                    for (File file : directory.listFiles()) {
                        if (file.getName().endsWith(".class")) {
                            String className = packageName + '.'
                                    + file.getName().substring(0, file.getName().length() - 6);
                            Class<?> cls = Class.forName(className);
                            if (superclass.isAssignableFrom(cls) && !cls.equals(superclass)) {
                                subclasses.add(cls);
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return subclasses;
    }
}
