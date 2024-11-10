package example;

public abstract class TestingClass {
    private int health;
    private int speed;

    public TestingClass(String input) {
        System.out.println(input);
    }

    public int getHealth(int test) {
        return health;
    }

    public int getSpeed() {
        return speed;
    }
}
