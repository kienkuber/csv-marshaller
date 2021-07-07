package impl;

import api.CsvColumn;
import api.Marshaller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvMarshaller<T> implements Marshaller<T> {

    @Override
    public void marshall(List<T> values, OutputStream os) throws IOException {
        if (values.isEmpty()) return;
        List<Field> fields = extractCsvFields(values.get(0));
        List<String> headers = fields.stream().map(this::getFieldName)
                .collect(Collectors.toList());
        List<List<String>> cellValues = getAllCellValues(values, fields);
        writeToFile(os, headers, cellValues);
    }

    private void writeToFile(OutputStream os, List<String> headers, List<List<String>> cellValues) throws IOException {
        try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                writer.write(header);
                if (i < headers.size() - 1) writer.write(",");
            }
            writer.write('\n');
            for (int i = 0; i < cellValues.size(); i++) {
                List<String> row = cellValues.get(i);
                for (int j = 0; j < row.size(); j++) {
                    writer.write(row.get(j));
                    if (j < row.size() - 1) writer.write(",");
                }
                if (i < cellValues.size() - 1) writer.write('\n');
            }
        }
    }

    private List<Field> extractCsvFields(T value) {
        return Arrays.stream(value.getClass().getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(CsvColumn.class))
                .collect(Collectors.toList());
    }

    private String getFieldName(Field field) {
        return field.getAnnotation(CsvColumn.class).name().equals("") ?
                field.getName() :
                field.getAnnotation(CsvColumn.class).name();

    }

    private List<List<String>> getAllCellValues(List<T> values, List<Field> fields) {
        List<String> fieldNames = fields.stream().map(Field::getName)
                .collect(Collectors.toList());
        return values.stream()
                .map(e -> getCellValuesForOneRow(e, fieldNames))
                .collect(Collectors.toList());
    }

    private List<String> getCellValuesForOneRow(T value, List<String> fieldNames) {
        return Arrays.stream(value.getClass().getDeclaredFields())
                .filter(e -> fieldNames.contains(e.getName()))
                .map(e -> {
                    try {
                        e.setAccessible(true);
                        Object object = e.get(value);
                        return object == null ? "" : object.toString();
                    } catch (IllegalAccessException illegalAccessException) {
                        return "";
                    }
                })
                .collect(Collectors.toList());
    }
}
