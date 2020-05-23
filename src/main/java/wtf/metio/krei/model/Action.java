package wtf.metio.krei.model;

@FunctionalInterface
public interface Action {

    /**
     * @return computed result
     */
    Integer call();

}
