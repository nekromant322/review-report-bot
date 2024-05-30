package com.nekromant.telegram.service;

import com.nekromant.telegram.model.PaymentDetails;
import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.repository.PromocodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromocodeService {
    @Autowired
    PromocodeRepository promocodeRepository;

    public ResponseEntity findAll() {
        return ResponseEntity.ok(promocodeRepository.findAllByOrderByIdAsc());
    }

    public ResponseEntity getPromocodeByText(String text) {
        return promocodeRepository.findByPromocodeText(text).isEmpty() ?
                ResponseEntity.notFound().build() :
                ResponseEntity.ok(promocodeRepository.findByPromocodeText(text).get());
    }

    public void save(Promocode promocode) {
        promocodeRepository.save(promocode);
    }

    public void deleteById(Map map) {
        promocodeRepository.deleteById(Long.parseLong(map.get("promocode_id").toString()));
    }

    public void updateSingleIsActive(Map map) {
        Promocode promocode = promocodeRepository.findById(Long.parseLong(map.get("promocode_id").toString())).get();
        promocode.setActive(Boolean.parseBoolean(map.get("isActive").toString()));
        promocodeRepository.save(promocode);
    }

    public Promocode findById(String cvPromocodeId) {
        return !cvPromocodeId.equals("null") ? promocodeRepository.findById(Long.parseLong(cvPromocodeId)).get() : null;
    }

    public Promocode findByPaymentDetailsSetNumber(String number) {
        return promocodeRepository.findByPaymentDetailsSetNumber(number);
    }

    public void incrementCounterUsed(PaymentDetails paymentDetails) {
        Promocode promocode = findByPaymentDetailsSetNumber(paymentDetails.getNumber());
        if (promocode != null) {
            promocode.setCounterUsed(promocode.getCounterUsed() + 1);
            if (promocode.getMaxUsesNumber() <= promocode.getCounterUsed()) {
                promocode.setActive(false);
            }
            save(promocode);
        }
    }
}
