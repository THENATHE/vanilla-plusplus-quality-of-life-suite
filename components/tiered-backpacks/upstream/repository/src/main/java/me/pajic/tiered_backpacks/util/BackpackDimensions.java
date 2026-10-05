package me.pajic.tiered_backpacks.util;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Translation(prefix = "tiered_backpacks.config")
public class BackpackDimensions implements Walkable {

    public ValidatedInt rows;
    public ValidatedInt columns;

    public BackpackDimensions(int rows, int columns) {
        this.rows = new ValidatedInt(rows);
        this.columns = new ValidatedInt(columns);
    }

    @SuppressWarnings("unused")
    public BackpackDimensions() {
        this.rows = new ValidatedInt(3, 16, 1);
        this.columns = new ValidatedInt(9, 16, 1);
    }
}
