package com.nekromant.telegram.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class Contract {

    @Id
    private String username;

    @Column
    private String contractId;

    @Column
    private LocalDate date;
}
