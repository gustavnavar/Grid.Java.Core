package me.agno.gridcore.columns;

import me.agno.gridcore.IGridColumnCollection;

import java.util.function.Function;

public interface IColumn<T> {

    String getName();

    String getFieldName();

    Class<?> getTargetType();

    void setFieldName(String fieldName);

    IGridColumn<T> sum(boolean enabled);

    IGridColumn<T> average(boolean enabled);

    IGridColumn<T> max(boolean enabled);

    IGridColumn<T> calculate(String name, Function<IGridColumnCollection<T>, Object> calculation);

    IGridColumn<T> min(boolean enabled);

    IGridColumn<T> setPrimaryKey(boolean enabled);

    IGridColumn<T> setPrimaryKey(boolean enabled, boolean autoGenerated);
}
