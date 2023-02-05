package fit.wenchao.http_file_server.function;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t) throws Exception;
}
