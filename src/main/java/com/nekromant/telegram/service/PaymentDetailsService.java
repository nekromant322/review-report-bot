package com.nekromant.telegram.service;

import com.nekromant.telegram.dto.UtmDTO;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.UtmTags;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import com.nekromant.telegram.repository.UtmTagsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PaymentDetailsService {
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;

    @Autowired
    private UtmTagsRepository utmTagsRepository;

    public void save(PaymentDetails paymentDetails){
        log.info(paymentDetails.toString());
        paymentDetailsRepository.save(paymentDetails);
    }

    public void addUtmTags(String utmDtoTags, PaymentDetails paymentDetails){
        if(!utmDtoTags.equals("notSet")) {
            String[] tags = UtmDTO.toStringParser(utmDtoTags);
            Optional<UtmTags> utmObj = utmTagsRepository.findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaignAndSection(tags[0], tags[1], tags[2], tags[3], tags[4]);
            utmObj.ifPresent(paymentDetails::setUtmTags);
            save(paymentDetails);
        }
    }

    public PaymentDetails findByNumber(String number) {
        return paymentDetailsRepository.findByNumber(number);
    }
}
