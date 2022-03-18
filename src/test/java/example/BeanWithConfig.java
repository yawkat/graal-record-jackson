package example;

public class BeanWithConfig {
    private String foo;

    public BeanWithConfig() {
    }

    public BeanWithConfig(String foo) {
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }

    public BeanWithConfig setFoo(String foo) {
        this.foo = foo;
        return this;
    }
}
