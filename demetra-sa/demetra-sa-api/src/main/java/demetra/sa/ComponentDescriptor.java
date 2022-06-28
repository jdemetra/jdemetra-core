package demetra.sa;

@lombok.Value
public class ComponentDescriptor {

    private String name;
    private int component;
    private boolean signal;
    private boolean lowFrequency;
}
