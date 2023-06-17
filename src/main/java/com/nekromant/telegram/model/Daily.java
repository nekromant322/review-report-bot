package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Daily {

    @Id
    @GeneratedValue
    private Long id;

    private String message;

    private LocalTime time;

    private String chatId;

}
