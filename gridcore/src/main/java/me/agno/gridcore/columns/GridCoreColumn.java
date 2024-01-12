package me.agno.gridcore.columns;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.agno.gridcore.IGrid;
import me.agno.gridcore.IGridColumnCollection;
import me.agno.gridcore.filtering.ColumnFilterValue;
import me.agno.gridcore.filtering.DefaultColumnFilter;
import me.agno.gridcore.filtering.GridFilterType;
import me.agno.gridcore.filtering.IColumnFilter;
import me.agno.gridcore.searching.DefaultColumnSearch;
import me.agno.gridcore.searching.IColumnSearch;
import me.agno.gridcore.sorting.GridSortDirection;
import me.agno.gridcore.sorting.GridSortMode;
import me.agno.gridcore.sorting.IColumnOrderer;
import me.agno.gridcore.sorting.OrderByGridOrderer;
import me.agno.gridcore.sorting.ThenByColumnOrderer;
import me.agno.gridcore.totals.DefaultColumnTotals;
import me.agno.gridcore.totals.IColumnTotals;
import me.agno.gridcore.totals.Total;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GridCoreColumn<T, TData> implements IGridColumn<T> {

    @Getter
    private String expression;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String fieldName;

    @Getter
    private Class<TData> targetType;

    @Getter
    private IGrid<T> parentGrid;

    @Getter
    @Setter
    private boolean hidden;

    @Getter
    private boolean primaryKey = false;

    public IGridColumn<T> setPrimaryKey(boolean enabled) {
        return setPrimaryKey(enabled, true);
    }

    public IGridColumn<T> setPrimaryKey(boolean enabled, boolean autoGenerated) {
        this.primaryKey = enabled;
        this.autoGeneratedKey = autoGenerated;
        return this;
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private boolean autoGeneratedKey = false;

    @Getter
    @Setter
    private boolean filterEnabled;

    @Getter
    @Setter
    private ColumnFilterValue initialFilterSettings;

    @Getter
    private IColumnFilter<T> filter;

    @Getter
    private IColumnSearch<T> search;

    @Getter
    @Setter
    private boolean sorted;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private boolean columnSortDefined = false;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private boolean sortEnabled;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private GridSortMode sortMode = GridSortMode.THREE_STATE;

    @Getter
    private Optional<GridSortDirection> direction;

    public void setDirection(GridSortDirection direction) {
        this.direction = Optional.ofNullable(direction);
    }

    @Getter
    private Optional<GridSortDirection> initialDirection;

    public void setInitialDirection(GridSortDirection initialDirection) {
        this.initialDirection = Optional.ofNullable(initialDirection);
    }

    @Getter
    private List<IColumnOrderer<T>> orderers = new ArrayList<IColumnOrderer<T>>();

    @Getter
    private IColumnTotals totals;

    @Getter
    @Setter
    private boolean sumEnabled = false;

    @Getter
    @Setter
    private boolean averageEnabled = false;

    @Getter
    @Setter
    private boolean maxEnabled = false;

    @Getter
    @Setter
    private boolean minEnabled = false;

    @Getter
    @Setter
    private boolean calculationEnabled = false;

    @Getter
    @Setter
    private LinkedHashMap<String, Function<IGridColumnCollection<T>, Object>> calculations;

    @Getter
    @Setter
    private LinkedHashMap<String, Total> calculationValues;

    @Getter
    @Setter
    private Total sumValue;

    @Getter
    @Setter
    private Total averageValue;

    @Getter
    @Setter
    private Total maxValue;

    @Getter
    @Setter
    private Total minValue;


    public GridCoreColumn(String expression, Class<TData> targetType, IGrid<T> grid)  {

        setSortEnabled(false);
        setHidden(false);

        this.targetType = targetType;
        this.parentGrid = grid;

        if (expression == null) {
            this.expression = expression;
            this.orderers.add(0, new OrderByGridOrderer<T, TData>(expression));
            this.filter = new DefaultColumnFilter<T, TData>(expression, targetType);
            this.search = new DefaultColumnSearch<T, TData>(expression, targetType);
            this.totals = new DefaultColumnTotals(expression);

            //Generate unique column name:
            setFieldName(expression);
            setName(getFieldName());
        }

        setCalculations(new LinkedHashMap<String, Function<IGridColumnCollection<T>, Object>>());
        setCalculationValues(new LinkedHashMap<String, Total>());
    }


    public IGridColumn<T> sortable(boolean sort) {
        return sortable(sort, null);
    }

    public IGridColumn<T> sortable(boolean sort, GridSortMode gridSortMode) {

        if (gridSortMode == null)
            gridSortMode= GridSortMode.THREE_STATE;

        if (sort && this.expression == null) {
            return this; //cannot enable sorting for column without expression
        }
        setColumnSortDefined(true);
        setSortEnabled(sort);
        setSortMode(gridSortMode);
        return this;
    }

    IGridColumn<T> internalSortable(boolean sort) {
        return internalSortable(sort, null);
    }

    public IGridColumn<T> internalSortable(boolean sort, GridSortMode gridSortMode) {

        if (gridSortMode == null)
            gridSortMode= GridSortMode.THREE_STATE;

        if (sort && this.expression == null) {
            return this; //cannot enable sorting for column without expression
        }

        if (!isColumnSortDefined()) {
            setSortEnabled(sort);
            setSortMode(gridSortMode);
        }
        return this;
    }

    public IGridColumn<T> sortInitialDirection(GridSortDirection direction) {

        setInitialDirection(direction);

        var sortSettings = this.parentGrid.getSettings().getSortSettings();
        if (sortSettings.getColumnName() == null || sortSettings.getColumnName().trim().isEmpty()) {

            setSorted(true);
            setDirection(direction);

            // added to enable initial sorting
            sortSettings.setColumnName(getName());
            sortSettings.setDirection(direction);
        }

        return this;
    }

    public IGridColumn<T> thenSortBy(String expression) {
        this.orderers.add(new ThenByColumnOrderer<T, TData>(expression, GridSortDirection.ASCENDING));
        return this;
    }

    public  IGridColumn<T> thenSortByDescending(String expression) {
        this.orderers.add(new ThenByColumnOrderer<T, TData>(expression, GridSortDirection.DESCENDING));
        return this;
    }

    public IGridColumn<T> filterable(boolean enable) {
        if (enable && this.expression == null) {
            return this; //cannot enable filtering for column without expression
        }

        setFilterEnabled(enable);
        return this;
    }

    public IGridColumn<T> setInitialFilter(GridFilterType type, String value) {
        var filter = new ColumnFilterValue(getName(), type, value);
        setInitialFilterSettings(filter);
        return this;
    }

    public IGridColumn<T> sum(boolean enabled) {
        this.sumEnabled = enabled;
        return this;
    }

    public IGridColumn<T> average(boolean enabled) {
        this.averageEnabled = enabled;
        return this;
    }

    public IGridColumn<T> max(boolean enabled) {
        this.maxEnabled = enabled;
        return this;
    }

    public IGridColumn<T> min(boolean enabled) {
        this.minEnabled = enabled;
        return this;
    }

    public IGridColumn<T> calculate(String name, Function<IGridColumnCollection<T>, Object> calculation) {
        this.calculationEnabled = true;
        this.calculations.put(name, calculation);
        return this;
    }
}
