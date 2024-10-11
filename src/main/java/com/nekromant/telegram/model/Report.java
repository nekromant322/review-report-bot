package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String studentUserName;

    @Column
    private Integer hours;

    @Column
    private LocalDate date;

    @Column
    private String title;
}
