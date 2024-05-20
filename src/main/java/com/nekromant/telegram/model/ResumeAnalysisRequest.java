package com.nekromant.telegram.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ResumeAnalysisRequest extends ClientRequest {
    @Column(name = "cv_pdf")
    @Lob
    @ToString.Exclude
    private byte[] CVPdf;

    @ToString.Include
    int CVPdfLength() {
        return this.CVPdf == null ? 0 : getCVPdf().length;
    }
}
