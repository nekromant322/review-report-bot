package com.nekromant.telegram.model;

import com.nekromant.telegram.converter.TimeSlotsConverter;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.*;

@Data
@Entity
@ToString
public class ReviewRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String studentChatId;
    @Column
    private String studentUserName;
    @Column
    private String mentorUserName;
    @Column
    private String title;
    @Column
    private LocalDate date;
    @Convert(converter = TimeSlotsConverter.class)
    @Column
    private Set<Integer> timeSlots;

    @Column
    private Integer bookedTimeSlot;

}
