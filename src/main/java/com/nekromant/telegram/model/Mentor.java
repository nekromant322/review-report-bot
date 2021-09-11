package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Mentor {

    @Id
    private String userName;

    @Column
    private Boolean isActive = true;

    public Mentor(String userName) {
        this.userName = userName;
    }
}
