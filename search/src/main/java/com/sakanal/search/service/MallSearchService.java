package com.sakanal.search.service;

import com.sakanal.search.vo.SearchParam;
import com.sakanal.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
