package com.nekromant.telegram.service;

import com.nekromant.telegram.commands.dto.ChequeDTO;
import com.nekromant.telegram.commands.feign.TelegramFeign;
import com.nekromant.telegram.config.LifePayProperties;
import com.nekromant.telegram.config.PriceProperties;
import com.nekromant.telegram.contants.ServiceType;
import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.ResumeAnalysisRequest;
import com.nekromant.telegram.repository.ResumeAnalysisRequestRepository;
import feign.form.FormData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;

import static com.nekromant.telegram.contants.MessageContants.RESPONSE_FOR_RESUME_PROJARKA;
import static com.nekromant.telegram.contants.MessageContants.RESUME_OFFER_DESCRIPTION;


@Slf4j
@Service
public class ResumeAnalysisRequestService extends ClientPaymentRequestServiceCommon implements ClientPaymentRequestService {
    @Autowired
    private ResumeAnalysisRequestRepository resumeAnalysisRequestRepository;
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private UserInfoService userInfoService;
    @Value("${owner.userName}")
    private String ownerUserName;
    @Autowired
    private TelegramFeign telegramFeign;
    @Autowired
    private LifePayProperties lifePayProperties;
    @Autowired
    private PriceProperties priceProperties;

    public ResponseEntity save(byte[] CVPdf, String tgName, String phone, String promocodeId) {
        ResumeAnalysisRequest resumeAnalysisRequest = ResumeAnalysisRequest.builder()
                .CVPdf(CVPdf)
                .tgName(tgName)
                .customerPhone(phone)
                .build();

        ChequeDTO chequeDTO = new ChequeDTO(lifePayProperties.getLogin(),
                lifePayProperties.getApikey(),
                calculatePriceWithOptionalDiscount(priceProperties.getResumeReview(), promocodeId),
                RESUME_OFFER_DESCRIPTION,
                phone,
                lifePayProperties.getMethod());

        return save(ServiceType.RESUME, chequeDTO, resumeAnalysisRequest, resumeAnalysisRequestRepository, promocodeId);
    }

    public void notifyMentor(PaymentDetails paymentDetails) {
        promocodeService.incrementCounterUsed(paymentDetails);

        String text = generateTextForMentoringBotNotification(paymentDetails,
                RESPONSE_FOR_RESUME_PROJARKA,
                resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getTgName());
        notifyMentor(paymentDetails, text);

        String receiverId = userInfoService.getUserInfo(ownerUserName).getChatId().toString();
        byte[] CV_bytes = resumeAnalysisRequestRepository.findByLifePayTransactionNumber(paymentDetails.getNumber()).getCVPdf();
        FormData formData = new FormData(MediaType.MULTIPART_FORM_DATA, "document", CV_bytes);
        telegramFeign.sendDocument(formData, receiverId);
    }
}
