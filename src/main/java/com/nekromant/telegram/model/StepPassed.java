package com.nekromant.telegram.model;

import com.nekromant.telegram.contants.Step;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StepPassed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String studentUserName;

    @Column
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Step step;

}
