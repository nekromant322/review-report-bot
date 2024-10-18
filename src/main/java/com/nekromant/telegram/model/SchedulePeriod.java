package com.nekromant.telegram.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class SchedulePeriod {
    @Id
    private Long id;

    @Column
    private Long startTime;

    @Column
    private Long endTime;
}
