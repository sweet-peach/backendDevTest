package com.backend.solution.feature.product.service;

import org.springframework.cache.Cache;
import com.backend.solution.feature.product.dto.ProductDetail;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductServiceImpl implements ProductService {

    private final WebClient webClient;
    private final Cache productCache;
    private final Map<String, Mono<ProductDetail>> inFlightRequests = new ConcurrentHashMap<>();

    public ProductServiceImpl(WebClient webClient, CacheManager cacheManager) {
        this.webClient = webClient;
        this.productCache = cacheManager.getCache("products");
    }

    @Cacheable(value = "similarIds", key = "#productId")
    public List<String> getSimilarProductIds(String productId) {
        return webClient.get()
                .uri("/product/{id}/similarids", productId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .onErrorReturn(Collections.emptyList())
                .block();
    }

    private Mono<ProductDetail> getProductDetailDeduplicated(String productId) {
        return inFlightRequests.computeIfAbsent(productId, id -> getProductDetail(id)
                .doFinally(signal -> inFlightRequests.remove(id))
                .cache());
    }

    private Mono<ProductDetail> getProductDetail(String productId) {
        return webClient.get()
                .uri("/product/{id}", productId)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.empty();
                    }
                    return response.bodyToMono(ProductDetail.class);
                });
    }


    @Override
    public List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarProductIds = getSimilarProductIds(productId);

        return Flux.fromIterable(similarProductIds)
                .flatMap(id -> {
                    ProductDetail cachedProduct = productCache != null ? productCache.get(id, ProductDetail.class) : null;

                    if (cachedProduct != null) {
                        return Mono.just(cachedProduct);
                    }

                    Mono<ProductDetail> originalCall = getProductDetailDeduplicated(id)
                            .doOnNext(product -> {
                                if (productCache != null && product != null) productCache.put(id, product);
                            });

                    originalCall
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();

                    return originalCall
                            .timeout(Duration.ofSeconds(2))
                            .onErrorResume(e -> Mono.empty());
                })
                .filter(Objects::nonNull)
                .collectList()
                .block();
    }
}