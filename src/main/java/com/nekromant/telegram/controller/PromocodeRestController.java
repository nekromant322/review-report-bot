package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.service.PromocodeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class PromocodeRestController {
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/promocode/list")
    public ResponseEntity<List<Promocode>> getPromocode() {
        List<Promocode> promocodeList = promocodeService.findAll();
        return new ResponseEntity<>(promocodeList, HttpStatus.OK);
    }

    @PostMapping("/promocode/add")
    public ResponseEntity addPromocode(@RequestBody Map promocodeDto) {
        Promocode promocode = modelMapper.map(promocodeDto, Promocode.class);
        promocode.setCreated(LocalDateTime.now().withNano(0));
        promocodeService.save(promocode);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/promocode/delete")
    public ResponseEntity deletePromocode(@RequestBody Map map) {
        promocodeService.deleteById(map);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/promocode/patch")
    public ResponseEntity updateIsActive(@RequestBody Map isActiveMap) {
        promocodeService.updateIsActive(isActiveMap);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/promocode/patch/single")
    public ResponseEntity updateSingleIsActive(@RequestBody Map map) {
        promocodeService.updateSingleIsActive(map);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/promocode/roasting/discount")
    public ResponseEntity discountPromocode(@RequestBody Map map) {
       return new ResponseEntity(promocodeService.getPromocodeByText(map), HttpStatus.OK);
    }
}
