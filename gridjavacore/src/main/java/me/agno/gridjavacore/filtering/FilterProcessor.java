package me.agno.gridjavacore.filtering;

import jakarta.persistence.criteria.Predicate;
import lombok.Setter;
import me.agno.gridjavacore.IGrid;
import me.agno.gridjavacore.columns.IGridColumn;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The `FilterProcessor` class is responsible for processing filters based on the provided grid and filter settings.
 */
public class FilterProcessor<T> {

    private final IGrid<T> grid;
    private IGridFilterSettings settings;

    @Setter
    private Function<Predicate, Predicate> process;

    /**
     * The `FilterProcessor` class is responsible for processing filters based on the provided grid and filter settings.
     */
    public FilterProcessor(IGrid<T> grid, IGridFilterSettings settings) {
        if (settings == null)
            throw new IllegalArgumentException("settings");
        this.grid = grid;
        this.settings = settings;
    }

    /**
     * Updates the settings for the grid filter.
     *
     * @param settings the new filter settings to update
     * @throws IllegalArgumentException if settings is null
     */
    public void updateSettings(IGridFilterSettings settings) {
        if (settings == null)
            throw new IllegalArgumentException("settings");
        this.settings = settings;
    }


    public Predicate process(Predicate predicate) {

        if (this.process != null)
            return this.process.apply(predicate);

        var gridQuery = (SqmSelectStatement) grid.getCriteriaQuery();
        var gridQuerySpec = gridQuery.getQuerySpec();
        SqmQuerySpec source = gridQuerySpec.copy(SqmCopyContext.simpleContext());

        for (IGridColumn<T> gridColumn : this.grid.getColumns().values()) {
            if (gridColumn == null) continue;
            if (gridColumn.getFilter() == null) continue;

            List<ColumnFilterValue> options;
            if(this.settings.isInitState(gridColumn)) {
                options = new ArrayList<ColumnFilterValue>();
                options.add(gridColumn.getInitialFilterSettings());
            }
            else {
                options = this.settings.getFilteredColumns().getByColumn(gridColumn);
            }

            var newPredicate = gridColumn.getFilter().applyFilter(this.grid.getCriteriaBuilder(), this.grid.getCriteriaQuery(),
                    this.grid.getRoot(), source, options, this.grid.getRemoveDiacritics());

            if(predicate == null)
                predicate = newPredicate;
            else if(newPredicate != null)
                predicate = this.grid.getCriteriaBuilder().and(predicate, newPredicate);
        }

        return predicate;
    }
}