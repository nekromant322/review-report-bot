package com.nekromant.telegram.service;

import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.repository.PromocodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PromocodeService {
    @Autowired
    PromocodeRepository promocodeRepository;

    public List<Promocode> findAll() {
        return promocodeRepository.findAllByOrderByIdAsc();
    }

    public void save(Promocode promocode) {
        promocodeRepository.save(promocode);
    }

    public void deleteById(Map map) {
        promocodeRepository.deleteById(Long.parseLong(map.get("promocode_id").toString()));
    }

    public void updateIsActive(Map map) {
        map.forEach((key, value) -> {
            Promocode promocode = promocodeRepository.findById(Long.parseLong(key.toString())).get();
            promocode.setActive(Boolean.parseBoolean(value.toString()));
            promocodeRepository.save(promocode);
        });
    }

    public void updateSingleIsActive(Map map) {
        Promocode promocode = promocodeRepository.findById(Long.parseLong(map.get("promocode_id").toString())).get();
        promocode.setActive(Boolean.parseBoolean(map.get("isActive").toString()));
        promocodeRepository.save(promocode);
    }

    public Promocode getPromocodeByText(Map map) {
        String promocodeText = map.get("promocodeText").toString();
        return promocodeRepository.findByPromocodeText(promocodeText);
    }

    public Promocode findById(String cvPromocodeId) {
       return promocodeRepository.findById(Long.parseLong(cvPromocodeId)).get();
    }

    public Promocode findByPaymentDetailsSetNumber(String number) {
        return promocodeRepository.findByPaymentDetailsSetNumber(number);
    }
}
