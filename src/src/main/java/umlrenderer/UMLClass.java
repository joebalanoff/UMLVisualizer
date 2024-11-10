package umlrenderer;

import java.util.ArrayList;
import java.util.List;

class UMLClass {
    private final String className;
    private final List<String> methods;
    private final List<String> fields;
    private UMLClass parentClass;
    private final List<UMLClass> subclasses = new ArrayList<>();

    private final boolean isAbstract;

    public UMLClass(String className, List<String> methods, List<String> fields, boolean isAbstract) {
        this.className = className;
        this.methods = methods;
        this.fields = fields;
        this.isAbstract = isAbstract;
    }

    public void addSubclass(UMLClass subclass) {
        subclasses.add(subclass);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getMethods() {
        return methods;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setParentClass(UMLClass parent) {
        this.parentClass = parent;
        parent.subclasses.add(this);
    }

    public UMLClass getParentClass() {
        return parentClass;
    }

    public List<UMLClass> getSubclasses() {
        return subclasses;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(className).append("\n");
        if (!fields.isEmpty()) {
            for (String field : fields) {
                ret.append("\t").append(field).append("\n");
            }
        }
        if(!methods.isEmpty()) {
            for (String method : methods) {
                ret.append("\t").append(method).append("\n");
            }
        }
        return ret.toString();
    }
}
