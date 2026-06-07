package n.paradox.aslib.initializer.generator;

public class NotAClass implements InitializerClass{
    @Override
    public void init() {
        throw new RuntimeException("how");
    }
}
