package com.example.LatestStable.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceFetcherService {
    private final OkHttpClient okHttpClient;
    private final CarbonCalculatorService carbonCalculatorService;

}
