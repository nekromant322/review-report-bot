package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.NewClient;
import com.nekromant.telegram.service.NewClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.Encoder;
import java.sql.Blob;

@RestController
public class SubmissionRestController {
    @Autowired
    NewClientService newClientService;

    @PostMapping("/submit_blob")
    @Modifying
    public void submitNewClientBlob(@RequestParam("blob") MultipartFile pdfBlob, @RequestHeader("tg_nick") String tg_name) throws Exception {
        Blob blob = new javax.sql.rowset.serial.SerialBlob(pdfBlob.getBytes());
//        System.out.println("*************************");
//        System.out.println(blob.length());
//        System.out.println("*************************");
        NewClient newClient = new NewClient();
        newClient.setCVPdf(blob);
        newClient.setTg_name(tg_name);
        System.out.println(tg_name);
        newClientService.saveNewBlob(newClient);
    }
//
//    @PostMapping("/submit_name")
//    public void submitNewClientName(@RequestParam("tg_nick") String tgName) throws Exception {
//
//    }



}
