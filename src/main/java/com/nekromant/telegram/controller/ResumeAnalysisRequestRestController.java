package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.PublicOfferRepository;
import com.nekromant.telegram.service.ResumeAnalysisRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ResumeAnalysisRequestRestController {
    @Autowired
    private ResumeAnalysisRequestService resumeAnalysisRequestService;
    @Autowired
    private PublicOfferRepository offerRepository;

    @PostMapping("/pricing")
    @Modifying
    public void submitNewResumeAnalysisRequest(@RequestParam("form_data") MultipartFile formData, @RequestHeader("tg_name") String tgName) throws Exception {
//        Blob blob = new SerialBlob(pdfBlob.getBytes());
//        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
//        resumeAnalysisRequest.setCVPdf(blob.getBinaryStream().readAllBytes());

        ResumeAnalysisRequest resumeAnalysisRequest = new ResumeAnalysisRequest();
        resumeAnalysisRequest.setCVPdf(formData.getBytes());

        resumeAnalysisRequest.setTgName(tgName);
        resumeAnalysisRequestService.save(resumeAnalysisRequest);
    }

    @GetMapping("/getoffer")
    public ResponseEntity<byte[]> getOffer() {
        byte[] bytes = offerRepository.findById(1L).get().getOfferPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
