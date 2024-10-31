package com.nekromant.telegram.model;

import com.nekromant.telegram.converter.TimeSlotsConverter;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@ToString
public class ReviewRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserInfo studentInfo;

    @ManyToOne
    private UserInfo mentorInfo;

    @Column
    private String title;

    @Column
    private LocalDate date;

    @Convert(converter = TimeSlotsConverter.class)
    @Column
    private Set<Integer> timeSlots;

    @Column
    private LocalDateTime bookedDateTime;
}
