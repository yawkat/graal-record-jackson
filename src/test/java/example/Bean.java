package example;

public class Bean {
    private String foo;

    public Bean() {
    }

    public Bean(String foo) {
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }

    public Bean setFoo(String foo) {
        this.foo = foo;
        return this;
    }
}
