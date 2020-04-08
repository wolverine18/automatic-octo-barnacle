package edu.gvsu.cis.convcalc.dummy;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class HistoryContent {

    public static final List<HistoryItem> ITEMS = new ArrayList<HistoryItem>();

    public static void addItem(HistoryItem item) {
        ITEMS.add(item);
    }

    public static class HistoryItem {
        public final Double fromVal;
        public final Double toVal;
        public final String mode;
        public final String fromUnits;
        public final String toUnits;
        public String _key;

        public final String timestamp;

        public HistoryItem() {
            this.fromVal = 0.0;
            this.toVal = 0.0;
            this.mode = "";
            this.fromUnits = "";
            this.toUnits = "";
            this.timestamp = new DateTime().toString();
            this._key = "";
        }

        public HistoryItem(Double fromVal, Double toVal, String mode,
                           String fromUnits, String toUnits, String timestamp) {
            this.fromVal = fromVal;
            this.toVal = toVal;
            this.mode = mode;
            this.fromUnits = fromUnits;
            this.toUnits = toUnits;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return this.fromVal + " " + this.fromUnits + " = " + this.toVal + " " + this.toUnits;
        }
    }
}
