package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Contract {

    @Id
    private String username;

    @Column
    private String contractId;

    @Column
    private LocalDate date;
}
