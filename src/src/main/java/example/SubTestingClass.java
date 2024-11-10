package example;

import java.util.ArrayList;
import java.util.List;

public class SubTestingClass extends TestingClass {
    private final List<String> strings = new ArrayList<>();

    protected SubTestingClass() {
        super("test");
    }
}
