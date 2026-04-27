/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang.preoperation.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DateExtractionType {

    ERA(DateExtractionType.ERA_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitEra();
        }
    },
    YEAR(DateExtractionType.YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitYear();
        }
    },
    MONTH(DateExtractionType.MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMonth();
        }
    },
    WEEK_OF_YEAR(DateExtractionType.WEEK_OF_YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWeekOfYear();
        }
    },
    WEEK_OF_MONTH(DateExtractionType.WEEK_OF_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWeekOfMonth();
        }
    },
    DATE(DateExtractionType.DATE_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDate();
        }
    },
    DAY_OF_YEAR(DateExtractionType.DAY_OF_YEAR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfYear();
        }
    },
    DAY_OF_MONTH(DateExtractionType.DAY_OF_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfMonth();
        }
    },
    DAY_OF_WEEK(DateExtractionType.DAY_OF_WEEK_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfWeek();
        }
    },
    DAY_OF_WEEK_IN_MONTH(DateExtractionType.DAY_OF_WEEK_IN_MONTH_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDayOfWeekInMonth();
        }
    },
    AM_PM(DateExtractionType.AM_PM_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAmPm();
        }
    },
    HOUR(DateExtractionType.HOUR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitHour();
        }
    },
    HOUR_OF_DAY(DateExtractionType.HOUR_OF_DAY_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitHourOfDay();
        }
    },
    MINUTE(DateExtractionType.MINUTE_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMinute();
        }
    },
    SECOND(DateExtractionType.SECOND_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitSecond();
        }
    },
    MILLISECOND(DateExtractionType.MILLISECOND_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitMillisecond();
        }
    },
    ZONE_OFFSET(DateExtractionType.ZONE_OFFSET_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitZoneOffset();
        }
    },
    DST_OFFSET(DateExtractionType.DST_OFFSET_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDstOffset();
        }
    },
    DIFF_WITH_EPOCH(DateExtractionType.DIFF_WITH_EPOCH_TEXT) {
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
