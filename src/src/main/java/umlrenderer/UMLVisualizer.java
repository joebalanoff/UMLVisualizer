package umlrenderer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UMLVisualizer {
    private final List<Path> allFiles;
    private final List<UMLClass> compiledClasses;

    private final UMLRenderer renderer;

    private Map<String, Color> classColors = new HashMap<>();
    private List<Color> colorPalette = Arrays.asList(Color.BLUE, Color.GREEN, Color.ORANGE);

    private UMLVisualizer(List<Path> allFiles) {
        this.allFiles = allFiles;
        this.compiledClasses = new ArrayList<>();

        this.renderer = new UMLRenderer(this);
    }

    public static void init() {
        try {
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path mainFileDirectory = Files.walk(projectRoot)
                    .parallel()
                    .filter(path -> path.toString().endsWith(".java") && containsMainMethod(path))
                    .map(Path::getParent)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Main class directory not found"));

            new UMLVisualizer(getAllJavaFiles(mainFileDirectory)).parseAllFiles();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing UML renderer", e);
        }
    }

    public static boolean containsMainMethod(Path path) {
        try {
            return Files.lines(path).anyMatch(line -> line.contains("public static void main"));
        } catch (IOException e) {
            return false;
        }
    }

    private static List<Path> getAllJavaFiles(Path startPath) throws IOException {
        List<Path> fileList = new ArrayList<>();

        Files.walk(startPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(fileList::add);

        return fileList;
    }

    private void parseAllFiles() {
        for (Path file : allFiles) {
            try {
                String content = new String(Files.readAllBytes(file));
                parseJavaFile(content);
            } catch (IOException e) {
                System.err.println("Error reading file: " + file);
                e.printStackTrace();
            }
        }
    }

    private void parseJavaFile(String content) {
        String className = extractClassName(content);
        UMLClass newClass = new UMLClass(className, extractMethods(content), extractFields(content), false);

        String parentClassName = extractParentClassName(content);
        if (parentClassName != null) {
            UMLClass parentClass = findOrCreateClass(parentClassName);
            newClass.setParentClass(parentClass);
        }

        compiledClasses.add(newClass);
    }

    private String extractParentClassName(String content) {
        Pattern pattern = Pattern.compile("class\\s+\\w+\\s+extends\\s+(\\w+)");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    private UMLClass findOrCreateClass(String className) {
        return compiledClasses.stream()
                .filter(umlClass -> umlClass.getClassName().equals(className))
                .findFirst()
                .orElseGet(() -> new UMLClass(className, new ArrayList<>(), new ArrayList<>(), false));
    }

    private String extractClassName(String content) {
        String classPattern = "(class|interface)\\s+(\\w+)";
        Pattern pattern = Pattern.compile(classPattern);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private List<String> extractMethods(String content) {
        List<String> methods = new ArrayList<>();
        String methodPattern = "(public|protected|private)\\s+([\\w<>]+)\\s+(\\w+)\\s*\\(([^)]*)\\)";
        Pattern pattern = Pattern.compile(methodPattern);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String accessModifier = convertAccessModifier(matcher.group(1) != null ? matcher.group(1) : "default");
            String returnType = matcher.group(2);
            String methodName = matcher.group(3);
            String params = matcher.group(4);

            String[] paramList = params.isEmpty() ? new String[0] : params.split("\\s*,\\s*");
            StringBuilder paramTypesOnly = new StringBuilder();
            for (String param : paramList) {
                String[] typeParts = param.trim().split("\\s+");
                paramTypesOnly.append(typeParts[0]).append(" ");
                if (typeParts.length > 1 && typeParts[1].contains("<")) {
                    paramTypesOnly.append(typeParts[1]);
                }
                paramTypesOnly.append(", ");
            }
            if (!paramTypesOnly.isEmpty()) {
                paramTypesOnly.setLength(paramTypesOnly.length() - 2);
            }
            methods.add(accessModifier + " " + returnType + " " + methodName + "(" + paramTypesOnly + ")");
        }
        return methods;
    }

    private List<String> extractFields(String content) {
        List<String> fields = new ArrayList<>();
        String fieldPattern = "(private|protected|public)\\s+([\\w<>]+)\\s+(\\w+)\\s*;";
        Pattern pattern = Pattern.compile(fieldPattern);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String accessModifier = convertAccessModifier(matcher.group(1) != null ? matcher.group(1) : "default");
            String type = matcher.group(2);
            String fieldName = matcher.group(3);
            fields.add(accessModifier + " " + type + " " + fieldName);
        }
        return fields;
    }

    private String convertAccessModifier(String accessModifier) {
        return accessModifier.equals("public") ? "+" : (accessModifier.equals("private") ? "-" : "#");
    }

    public List<UMLClass> getCompiledClasses() {
        return compiledClasses;
    }
}
