package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Promocode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocodeRepository extends CrudRepository<Promocode, Long> {
    List<Promocode> findAllByOrderByIdAsc();

    Promocode findByPromocodeText(String promocodeText);

    Promocode findByPaymentDetailsSetNumber(String number);
}
