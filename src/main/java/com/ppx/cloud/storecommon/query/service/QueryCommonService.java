package com.ppx.cloud.storecommon.query.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.ppx.cloud.common.jdbc.MyDaoSupport;
import com.ppx.cloud.storecommon.query.bean.QueryProduct;


@Service
public class QueryCommonService extends MyDaoSupport {
	
	
	public List<QueryProduct> listProduct(List<Integer> prodIdList, Integer storeId) {
		if (prodIdList.size() == 0) {
			return new ArrayList<QueryProduct>();
		}
		
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("storeId", storeId);
		paramMap.put("prodIdList", prodIdList);	
		
		String prodSql = "select p.PROD_ID PID, p.PROD_TITLE T, s.PRICE P, img.SKU_IMG_SRC SRC, idx.PROG_ID GID, idx.POLICY ARG, if(p.REPO_ID = :storeId, 1, 0) F from product p join sku s on p.PROD_ID = s.PROD_ID left join " +
			"(select t.SKU_ID, t.SKU_IMG_SRC from (select * from sku_img order by SKU_IMG_PRIO desc) t group by t.SKU_ID) img on s.SKU_ID = img.SKU_ID left join " +
			"(select t.PROD_ID, t.PROG_ID, t.INDEX_POLICY POLICY from (select * from program_index i where curdate() between INDEX_BEGIN and INDEX_END order by INDEX_PRIO desc) t group by t.PROD_ID) idx on p.PROD_ID = idx.PROD_ID " + 
			"where p.PROD_ID in (:prodIdList)";
		
		List<QueryProduct> prodList = jdbc.query(prodSql, paramMap, BeanPropertyRowMapper.newInstance(QueryProduct.class));
		return prodList;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
}
