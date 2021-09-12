package com.nekromant.telegram.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class UserStatistic {

    private String userName;
    private int totalDays;
    private int totalHours;
    private int studyDays;
    private float averagePerWeek;
}
