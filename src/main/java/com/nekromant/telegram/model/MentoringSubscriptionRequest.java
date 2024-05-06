package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zalando.logbook.Strategy;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MentoringSubscriptionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_name")
    private String tgName;

    private String phone;

    private String lifePayNumber;

    @Override
    public String toString() {
        return "ResumeAnalysisRequest (tgName=" + this.getTgName()
                + ", phone=" + this.getPhone()
                + ", lifePayNumber=" + this.getLifePayNumber() + ")";
    }
}
