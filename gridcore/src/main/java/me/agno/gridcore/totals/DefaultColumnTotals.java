package me.agno.gridcore.totals;

import lombok.Getter;

@Getter
public class DefaultColumnTotals implements IColumnTotals {

    private final String expression;

    public DefaultColumnTotals(String expression) {
        this.expression = expression;
    }
}
