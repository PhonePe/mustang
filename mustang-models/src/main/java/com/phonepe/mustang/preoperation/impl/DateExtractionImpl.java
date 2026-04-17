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

import java.util.Calendar;
import java.util.Date;

import static com.phonepe.mustang.common.Utils.Constants;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateExtractionImpl implements DateExtractionType.Visitor<Object> {

    private Calendar instance;

    @Override
    public Object visitEra() {
        return instance.get(Calendar.ERA) == 1 ? Constants.ERA_AD : Constants.ERA_BC;
    }

    @Override
    public Object visitYear() {
        return instance.get(Calendar.YEAR);
    }

    @Override
    public Object visitMonth() {
        return instance.get(Calendar.MONTH) + 1; // O indexed, and hence adding 1.
    }

    @Override
    public Object visitWeekOfYear() {
        return instance.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public Object visitWeekOfMonth() {
        return instance.get(Calendar.WEEK_OF_MONTH);
    }

    @Override
    public Object visitDate() {
        return instance.get(Calendar.DATE);
    }

    @Override
    public Object visitDayOfYear() {
        return instance.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public Object visitDayOfMonth() {
        return instance.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public Object visitDayOfWeek() {
        return instance.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public Object visitDayOfWeekInMonth() {
        return instance.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    @Override
    public Object visitAmPm() {
        return instance.get(Calendar.AM_PM) == 0 ? Constants.AM : Constants.PM;
    }

    @Override
    public Object visitHour() {
        return instance.get(Calendar.HOUR);
    }

    @Override
    public Object visitHourOfDay() {
        return instance.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public Object visitMinute() {
        return instance.get(Calendar.MINUTE);
    }

    @Override
    public Object visitSecond() {
        return instance.get(Calendar.SECOND);
    }

    @Override
    public Object visitMillisecond() {
        return instance.get(Calendar.MILLISECOND);
    }

    @Override
    public Object visitZoneOffset() {
        return instance.get(Calendar.ZONE_OFFSET);
    }

    @Override
    public Object visitDstOffset() {
        return instance.get(Calendar.DST_OFFSET);
    }

    @Override
    public Object visitDiffWithEpoch() {
        return new Date().getTime() - instance.getTimeInMillis();
    }
}
