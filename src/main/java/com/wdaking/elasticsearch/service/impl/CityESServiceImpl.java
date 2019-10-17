package com.wdaking.elasticsearch.service.impl;

import com.wdaking.elasticsearch.domain.City;
import com.wdaking.elasticsearch.repository.CityRepository;
import com.wdaking.elasticsearch.service.CityService;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * 城市 ES 业务逻辑实现类
 */
@Service
public class CityESServiceImpl implements CityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityESServiceImpl.class);

    @Autowired
    CityRepository cityRepository;

    @Override
    public Long saveCity(City city) {

        City cityResult = cityRepository.save(city);
        return cityResult.getId();
    }

    @Override
    public List<City> searchCity(Integer pageNumber,
                                 Integer pageSize,
                                 String searchContent) {
        // 分页参数
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functionBuilders = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuery("cityname", searchContent),
                        ScoreFunctionBuilders.weightFactorFunction(1)),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuery("description", searchContent),
                        ScoreFunctionBuilders.weightFactorFunction(1))
        };
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(functionBuilders)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                .setMinScore(2F);
        // 创建搜索 DSL 查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(functionScoreQueryBuilder).build();

        LOGGER.info("\n searchCity(): searchContent [" + searchContent + "] \n DSL  = \n " + searchQuery.getQuery().toString());

        Page<City> searchPageResults = cityRepository.search(searchQuery);
        return searchPageResults.getContent();
    }

}
