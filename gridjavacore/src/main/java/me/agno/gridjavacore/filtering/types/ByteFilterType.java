package me.agno.gridjavacore.filtering.types;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import me.agno.gridjavacore.columns.GridCoreColumn;
import me.agno.gridjavacore.filtering.GridFilterType;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;

import java.nio.charset.StandardCharsets;

/**
 * A class representing a filter type for byte values.
 */
@Getter
public final class ByteFilterType<T> extends FilterTypeBase<T, Byte> {

    private final Class<Byte> targetType = Byte.class;

    /**
     * Returns a valid GridFilterType based on the provided type. If the provided type is one of
     * the valid filter types, it is returned as-is. Otherwise, the default filter type (EQUALS) is returned.
     *
     * @param type The GridFilterType to be validated.
     * @return The valid GridFilterType.
     */
    public GridFilterType getValidType(GridFilterType type) {
        return switch (type) {
            case EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS,
                    IS_DUPLICATED, IS_NOT_DUPLICATED -> type;
            default -> GridFilterType.EQUALS;
        };
    }

    /**
     * Returns a {@code Byte} value representing the first byte of the provided {@code String} value.
     * If the value cannot be converted to a byte, returns {@code null}.
     *
     * @param value The {@code String} value to convert.
     * @return The {@code Byte} value representing the first byte of the value, or {@code null} if
     *         the value cannot be converted to a byte.
     */
    public Byte getTypedValue(String value) {
        try {
            return value.getBytes(StandardCharsets.UTF_8)[0];
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a filter expression Predicate based on the provided parameters.
     *
     * @param cb              The CriteriaBuilder object.
     * @param cq              The CriteriaQuery object.
     * @param root            The Root object.
     * @param source          The SqmQuerySpec object.
     * @param column          The column.
     * @param value           The value to compare against.
     * @param filterType      The GridFilterType.
     * @param removeDiacritics The flag indicating whether to remove diacritics or not.
     * @return                The filter expression Predicate.
     */
    public Predicate getFilterExpression(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> root,
                                         SqmQuerySpec source, GridCoreColumn<T, Byte> column,
                                         String value, GridFilterType filterType, String removeDiacritics) {

        //base implementation of building filter expressions
        filterType = this.getValidType(filterType);

        Byte typedValue = this.getTypedValue(value);
        if (typedValue == null &&
                filterType != GridFilterType.IS_DUPLICATED && filterType != GridFilterType.IS_NOT_DUPLICATED)
            return null; //incorrent filter value;

        var path = getPath(root, column.getExpression());

        return switch (filterType) {
            case EQUALS -> cb.equal(path, typedValue);
            case NOT_EQUALS -> cb.notEqual(path, typedValue);
            case LESS_THAN -> cb.lt(path, typedValue);
            case LESS_THAN_OR_EQUALS -> cb.le(path, typedValue);
            case GREATER_THAN -> cb.gt(path, typedValue);
            case GREATER_THAN_OR_EQUALS -> cb.ge(path, typedValue);
            case IS_DUPLICATED -> isDuplicated(cb, cq, root, source, this.targetType, column.getExpression());
            case IS_NOT_DUPLICATED -> isNotDuplicated(cb, cq, root, source,this.targetType,
                    column.getExpression());
            default -> throw new IllegalArgumentException();
        };
    }
}