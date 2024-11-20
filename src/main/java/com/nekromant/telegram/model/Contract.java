package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
    @Id
    private Long studentInfoChatId;

    @Column
    private String contractId;

    @MapsId
    @OneToOne
    private UserInfo studentInfo;

    @Column
    private LocalDate date;
}
