package com.ggg456.shortlink.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfiguration {


    /**
     * 布隆过滤器
     * @param redissonClient
     * @return
     */
    @Bean
    public RBloomFilter<String> shortUriCacheBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("shortUriCacheBloomFilter");// 创建布隆过滤器.name表示缓存名称
        bloomFilter.tryInit(100000000, 0.01);//expectedInsertions表示预期插入的元素数量，falseProbability表示误判率
        return bloomFilter;
    }

}