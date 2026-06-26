package com.awpy.awpy.controller;

import com.awpy.awpy.dto.ranking.RankingResponse;
import com.awpy.awpy.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public List<RankingResponse> topCinco() {
        return rankingService.topCinco();
    }
}
