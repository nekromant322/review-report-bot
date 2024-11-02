package com.nekromant.telegram.model;

import com.nekromant.telegram.contants.Step;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    UserInfo studentInfo;

    @Column
    private String studentUserName;

    @Column
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Step step;

}
