package com.ppx.cloud.storecommon.query.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
		
		String prodSql = "select * from ( " +
			"select p.PROD_ID PID, p.PROD_TITLE T, idx.PROG_ID GID, idx.POLICY ARG, if(p.REPO_ID = :storeId, 1, 0) F, min(s.PRICE) P, sum(s.STOCK_NUM) N, " +
			"(select i.PROD_IMG_SRC from product_img i where i.PROD_ID = p.PROD_ID order by i.PROD_IMG_PRIO limit 1) SRC " +
			"from product p left join sku s on p.PROD_ID = s.PROD_ID " +
			"left join (select t.PROD_ID, t.PROG_ID, t.INDEX_POLICY POLICY from (select * from program_index i where curdate() between INDEX_BEGIN and INDEX_END order by INDEX_PRIO) t group by t.PROD_ID) idx on p.PROD_ID = idx.PROD_ID " +  
			"group by p.PROD_ID) t where t.PID in (:prodIdList) order by instr('," + StringUtils.join(prodIdList, ",") + ",',CONCAT(',',t.PID,','))";
		
		List<QueryProduct> prodList = jdbc.query(prodSql, paramMap, BeanPropertyRowMapper.newInstance(QueryProduct.class));
		return prodList;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
}
