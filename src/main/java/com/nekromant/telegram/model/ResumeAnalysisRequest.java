package com.nekromant.telegram.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ResumeAnalysisRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_pdf")
    @Lob
    private byte[] CVPdf;

    @Column(name = "tg_name")
    private String tgName;

    public byte[] getCVPdf() {
        return CVPdf;
    }

    public void setCVPdf(byte[] CVPdf) {
        this.CVPdf = CVPdf;
    }

    public String getTgName() {
        return tgName;
    }

    public void setTgName(String tgName) {
        this.tgName = tgName;
    }
}
