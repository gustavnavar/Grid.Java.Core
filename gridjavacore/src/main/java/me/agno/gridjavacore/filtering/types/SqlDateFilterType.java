package me.agno.gridjavacore.filtering.types;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import me.agno.gridjavacore.columns.GridCoreColumn;
import me.agno.gridjavacore.filtering.GridFilterType;
import me.agno.gridjavacore.utils.DateTimeUtils;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;

import java.sql.Date;

/**
 * SqlDateFilterType is a class that represents a custom filter type for filtering dates in SQL queries.
 */
@Getter
public class SqlDateFilterType<T> extends FilterTypeBase<T, Date> {

    private final Class<Date> targetType = Date.class;

    /**
     * Returns the valid GridFilterType based on the input GridFilterType.
     *
     * @param type The input GridFilterType.
     * @return The valid GridFilterType. If the input type is a valid GridFilterType, it is returned as is. Otherwise, GridFilterType.EQUALS is returned.
     */
    public GridFilterType getValidType(GridFilterType type) {
        return switch (type) {
            case EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS,
                    IS_DUPLICATED, IS_NOT_DUPLICATED -> type;
            default -> GridFilterType.EQUALS;
        };
    }

    /**
     * Retrieves a typed value from a string value.
     *
     * @param value The string value to parse into a typed value.
     * @return The typed value retrieved from the string value, or null if parsing fails.
     */
    public Date getTypedValue(String value) {
        return DateTimeUtils.getSqlDate(value);
    }

    /**
     * Retrieves the filter expression based on the given criteria objects and parameters.
     *
     * @param cb               The CriteriaBuilder object.
     * @param cq               The CriteriaQuery object.
     * @param root             The Root object.
     * @param source           The SqmQuerySpec object.
     * @param column           The column.
     * @param value            The filter value.
     * @param filterType The GridFilterType.
     * @param removeDiacritics Whether to remove diacritics from the filter value.
     * @return The Predicate representing the filter expression.
     */
    public Predicate getFilterExpression(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> root,
                                         SqmQuerySpec source, GridCoreColumn<T, Date> column, String value,
                                         GridFilterType filterType, String removeDiacritics) {

        //base implementation of building filter expressions
        filterType = getValidType(filterType);

        Date typedValue = getTypedValue(value);
        if (typedValue == null &&
                filterType != GridFilterType.IS_DUPLICATED && filterType != GridFilterType.IS_NOT_DUPLICATED)
            return null; //incorrent filter value;

        var path = getPath(root, column.getExpression());

        return switch (filterType) {
            case EQUALS -> cb.equal(path, typedValue);
            case NOT_EQUALS -> cb.notEqual(path, typedValue);
            case LESS_THAN -> cb.lessThan(path, typedValue);
            case LESS_THAN_OR_EQUALS -> cb.lessThanOrEqualTo(path, typedValue);
            case GREATER_THAN -> cb.greaterThan(path, typedValue);
            case GREATER_THAN_OR_EQUALS -> cb.greaterThanOrEqualTo(path, typedValue);
            case IS_DUPLICATED -> isDuplicated(cb, cq, root, source, this.targetType, column.getExpression());
            case IS_NOT_DUPLICATED -> isNotDuplicated(cb, cq, root, source, this.targetType,
                    column.getExpression());
            default -> throw new IllegalArgumentException();
        };
    }
}