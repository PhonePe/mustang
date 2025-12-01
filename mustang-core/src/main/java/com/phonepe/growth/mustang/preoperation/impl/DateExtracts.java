package com.phonepe.growth.mustang.preoperation.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DateExtracts {

    ERA(DateExtracts.ERA_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitEra();
        }
    },
    YEAR(DateExtracts.YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitYear();
        }
    },
    MONTH(DateExtracts.MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMonth();
        }
    },
    WEEK_OF_YEAR(DateExtracts.WEEK_OF_YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWeekOfYear();
        }
    },
    WEEK_OF_MONTH(DateExtracts.WEEK_OF_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWeekOfMonth();
        }
    },
    DATE(DateExtracts.DATE_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDate();
        }
    },
    DAY_OF_YEAR(DateExtracts.DAY_OF_YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfYear();
        }
    },
    DAY_OF_MONTH(DateExtracts.DAY_OF_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfMonth();
        }
    },
    DAY_OF_WEEK(DateExtracts.DAY_OF_WEEK_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfWeek();
        }
    },
    DAY_OF_WEEK_IN_MONTH(DateExtracts.DAY_OF_WEEK_IN_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfWeekInMonth();
        }
    },
    AM_PM(DateExtracts.AM_PM_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAmPm();
        }
    },
    HOUR(DateExtracts.HOUR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitHour();
        }
    },
    HOUR_OF_DAY(DateExtracts.HOUR_OF_DAY_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitHourOfDay();
        }
    },
    MINUTE(DateExtracts.MINUTE_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMinute();
        }
    },
    SECOND(DateExtracts.SECOND_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitSecond();
        }
    },
    MILLISECOND(DateExtracts.MILLISECOND_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMillisecond();
        }
    },
    ZONE_OFFSET(DateExtracts.ZONE_OFFSET_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitZoneOffset();
        }
    },
    DST_OFFSET(DateExtracts.DST_OFFSET_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDstOffset();
        }
    },
    DIFF_WITH_EPOCH(DateExtracts.DIFF_WITH_EPOCH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDiffWithEpoch();
        }
    };

    public static final String ERA_TEXT = "ERA";
    public static final String YEAR_TEXT = "YEAR";
    public static final String MONTH_TEXT = "MONTH";
    public static final String WEEK_OF_YEAR_TEXT = "WEEK_OF_YEAR";
    public static final String WEEK_OF_MONTH_TEXT = "WEEK_OF_MONTH";
    public static final String DATE_TEXT = "DATE";
    public static final String DAY_OF_YEAR_TEXT = "DAY_OF_YEAR";
    public static final String DAY_OF_MONTH_TEXT = "DAY_OF_MONTH";
    public static final String DAY_OF_WEEK_TEXT = "DAY_OF_WEEK";
    public static final String DAY_OF_WEEK_IN_MONTH_TEXT = "DAY_OF_WEEK_IN_MONTH";
    public static final String AM_PM_TEXT = "AM_PM";
    public static final String HOUR_TEXT = "HOUR";
    public static final String HOUR_OF_DAY_TEXT = "HOUR_OF_DAY";
    public static final String MINUTE_TEXT = "MINUTE";
    public static final String SECOND_TEXT = "SECOND";
    public static final String MILLISECOND_TEXT = "MILLISECOND";
    public static final String ZONE_OFFSET_TEXT = "ZONE_OFFSET";
    public static final String DST_OFFSET_TEXT = "DST_OFFSET";
    public static final String DIFF_WITH_EPOCH_TEXT = "DIFF_WITH_EPOCH";

    @Getter
    private String value;

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitEra();

        T visitYear();

        T visitMonth();

        T visitWeekOfYear();

        T visitWeekOfMonth();

        T visitDate();

        T visitDayOfYear();

        T visitDayOfMonth();

        T visitDayOfWeek();

        T visitDayOfWeekInMonth();

        T visitAmPm();

        T visitHour();

        T visitHourOfDay();

        T visitMinute();

        T visitSecond();

        T visitMillisecond();

        T visitZoneOffset();

        T visitDstOffset();

        T visitDiffWithEpoch();

    }

}
