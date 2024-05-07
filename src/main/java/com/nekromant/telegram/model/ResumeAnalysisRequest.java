package com.nekromant.telegram.model;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResumeAnalysisRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Exclude
    private Long id;

    @Column(name = "cv_pdf")
    @Lob
    @ToString.Exclude
    private byte[] CVPdf;

    private String tgName;

    private String customerPhone;

    private String lifePayTransactionNumber;

    @ToString.Include
    int CVPdfLength() {
        return this.CVPdf == null ? 0 : getCVPdf().length;
    }
}
