package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;

import java.sql.Blob;

@RestController
public class ResumeAnalysisRequestRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;

    @PostMapping("/submit_blob")
    @Modifying
    public void submitNewResumeAnalysisRequest(@RequestParam("blob") MultipartFile pdfBlob, @RequestHeader("tg_name") String tgName) throws Exception {
        Blob blob = new SerialBlob(pdfBlob.getBytes());
        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
        resumeAnalysisRequest.setCVPdf(blob.getBinaryStream().readAllBytes());
        resumeAnalysisRequest.setTgName(tgName);
        resumeAnalysisRequestService.saveNewBlob(resumeAnalysisRequest);
    }
}
