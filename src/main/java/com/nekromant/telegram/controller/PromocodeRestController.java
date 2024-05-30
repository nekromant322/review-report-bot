package com.nekromant.telegram.controller;

import com.nekromant.telegram.model.Promocode;
import com.nekromant.telegram.service.PromocodeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/promocode")
public class PromocodeRestController {
    @Autowired
    private PromocodeService promocodeService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity getAllPromocodes(@RequestParam(required = false, name = "text") String text) {
        return text == null || text.isEmpty() ?
                promocodeService.findAll() :
                promocodeService.getPromocodeByText(text);
    }

    @PostMapping
    public ResponseEntity addPromocode(@RequestBody Map promocodeDto) {
        Promocode promocode = modelMapper.map(promocodeDto, Promocode.class);
        promocode.setCreated(LocalDateTime.now().withNano(0));
        promocodeService.save(promocode);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity deletePromocode(@RequestBody Map map) {
        promocodeService.deleteById(map);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/isactive")
    public ResponseEntity updateSingleIsActive(@RequestBody Map map) {
        promocodeService.updateSingleIsActive(map);
        return ResponseEntity.ok().build();
    }

}
