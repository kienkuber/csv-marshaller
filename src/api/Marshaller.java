package api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface Marshaller<T> {
    void marshall(List<T> values, OutputStream os) throws IOException;
}
