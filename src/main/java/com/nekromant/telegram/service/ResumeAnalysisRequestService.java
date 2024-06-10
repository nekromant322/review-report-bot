package com.nekromant.telegram.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.dto.LifePayResponseDTO;
import com.nekromant.telegram.commands.feign.LifePayFeign;
import com.nekromant.telegram.config.LifePayProperties;
import com.nekromant.telegram.contants.PayStatus;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.PaymentDetailsRepository;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.nekromant.telegram.contants.MessageContants.RESUME_OFFER_DESCRIPTION;
import static java.rmi.server.LogStream.log;


@Slf4j
@Service
public class ResumeAnalysisRequestService extends ClientPaymentRequestService {
    @Autowired
    private ResumeAnalysisRequestRepository resumeAnalysisRequestRepository;
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private LifePayFeign lifePayFeign;
    @Autowired
    private LifePayProperties lifePayProperties;

    public ResponseEntity save(byte[] CVPdf, String tgName, String phone, String CVPromocodeId) {
        ResumeAnalysisRequest resumeAnalysisRequest = ResumeAnalysisRequest.builder()
                .CVPdf(CVPdf)
                .tgName(tgName)
                .customerPhone(phone)
                .build();

        ChequeDTO chequeDTO = new ChequeDTO(lifePayProperties.getLogin(),
                lifePayProperties.getApikey(),
                super.calculatePriceWithOptionalDiscount(CVPromocodeId, this.getClass().getSimpleName()),
                RESUME_OFFER_DESCRIPTION,
                phone,
                lifePayProperties.getMethod());

        try {
            ResumeAnalysisRequestService.log.info("Sending request to LifePay" + chequeDTO);
            LifePayResponseDTO lifePayResponse = new Gson().fromJson(lifePayFeign.payCheque(chequeDTO).getBody(), LifePayResponseDTO.class);
            ResumeAnalysisRequestService.log.info("LifePay response: " + lifePayResponse);

            PaymentDetails paymentDetails = PaymentDetails.builder()
                    .number(lifePayResponse.getData().getNumber())
                    .status(PayStatus.UNREDEEMED)
                    .serviceType(ServiceType.RESUME)
                    .build();
            paymentDetailsRepository.save(paymentDetails);
            ResumeAnalysisRequestService.log.info("Unredeemed payment created: " + paymentDetails);

            resumeAnalysisRequest.setLifePayTransactionNumber(lifePayResponse.getData().getNumber());
            resumeAnalysisRequestRepository.save(resumeAnalysisRequest);
            ResumeAnalysisRequestService.log.info("New resume analysis request created: " + resumeAnalysisRequest);

            Promocode promocode = promocodeService.findById(CVPromocodeId);
            if (promocode != null) {
                Set<PaymentDetails> promocodePaymentDetailsSet = promocode.getPaymentDetailsSet();
                promocodePaymentDetailsSet.add(paymentDetails);
                promocode.setPaymentDetailsSet(promocodePaymentDetailsSet);
                promocodeService.save(promocode);
            }
            return ResponseEntity.ok(lifePayResponse.getData().getPaymentUrlWeb());
        } catch (JsonParseException jsonParseException) {
            log("Erorr while parsing Json: " + jsonParseException.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException dataAccessException) {
            log("Error while accessing database: " + dataAccessException.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
