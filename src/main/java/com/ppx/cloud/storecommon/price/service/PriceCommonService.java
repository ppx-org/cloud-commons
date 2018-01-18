package com.ppx.cloud.storecommon.price.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppx.cloud.common.jdbc.MyDaoSupport;
import com.ppx.cloud.storecommon.price.bean.SkuIndex;
import com.ppx.cloud.storecommon.price.utils.SkuIndexComparator;


@Service
public class PriceCommonService extends MyDaoSupport {
	
	
	/** 找出不存在的skuId,测试price时用 */
	private Map<Integer, List<SkuIndex>> getNoExist(List<SkuIndex> dbSkuList, Map<Integer, SkuIndex> skuIndexMap) {
		Map<Integer, List<SkuIndex>> returnMap = new HashMap<Integer, List<SkuIndex>>();
		
		List<Integer> dbSkuIdList = new ArrayList<Integer>();
		for (SkuIndex skuIndex : dbSkuList) {
			dbSkuIdList.add(skuIndex.getSkuId());
		}
		
		
		List<SkuIndex> list = new ArrayList<SkuIndex>();
		for (Integer id : skuIndexMap.keySet()) {
			
			if (!dbSkuIdList.contains(id)) {
				SkuIndex index = new SkuIndex(id, 0);
				list.add(index);
			}
		}
		returnMap.put(-2, list);
		
		return returnMap;
	}

	/***
	 * 
	 * @param date
	 * @param skuIndexMap 需要skuId和num
	 * @return 1:正常 -2:有不存的skuId
	 */
	
	public Map<Integer, List<SkuIndex>> countPrice(Date date, Map<Integer, SkuIndex> skuIndexMap) {
		Map<Integer, List<SkuIndex>> returnMap = new HashMap<Integer, List<SkuIndex>>();
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	
		
		String sSql = "select s.SKU_ID, s.PROD_ID, s.PRICE, s.SKU_NAME, p.PROD_TITLE from sku s join product p on s.PROD_ID = p.PROD_ID where SKU_ID in (:skuIdList)";
	

		List<Integer> skuIdList = new ArrayList<Integer>();
		for (Integer skuId : skuIndexMap.keySet()) {
			skuIdList.add(skuId);
		}
		
		
		Map<String, Object> skuParamMap = new HashMap<String, Object>();
		skuParamMap.put("skuIdList", skuIdList);		
		
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
		List<SkuIndex> dbSkuList = jdbc.query(sSql, skuParamMap, BeanPropertyRowMapper.newInstance(SkuIndex.class));
		
		if (dbSkuList.size() != skuIndexMap.size()) {
			// skuId不存在
			return getNoExist(dbSkuList, skuIndexMap);
		}
		
		List<Integer> prodIdList = new ArrayList<Integer>();
		for (SkuIndex index : dbSkuList) {
			SkuIndex newIndex = skuIndexMap.get(index.getSkuId());
			newIndex.setProdId(index.getProdId());
			newIndex.setPrice(index.getPrice());
			newIndex.setProdTitle(index.getProdTitle());
			newIndex.setSkuName(index.getSkuName());
			newIndex.setSkuImgSrc(index.getSkuImgSrc());
			prodIdList.add(index.getProdId());
		}
		
		// index >>>>>>>>>>>>>>>>>>>>>>>
		Map<String, Object> prodParamMap = new HashMap<String, Object>();
		prodParamMap.put("prodIdList", prodIdList);
		prodParamMap.put("date", date);	
		String indexSql = "select (select SKU_ID from sku where PROD_ID = t.PROD_ID) SKU_ID, t.PROD_ID, t.PROG_ID, t.INDEX_POLICY POLICY from"
				+ " (select * from program_index where PROD_ID in (:prodIdList) and :date between INDEX_BEGIN and INDEX_END order by INDEX_PRIO desc) t group by t.PROD_ID";
		List<SkuIndex> indexList = jdbc.query(indexSql, prodParamMap, BeanPropertyRowMapper.newInstance(SkuIndex.class));
		for (SkuIndex index : indexList) {
			SkuIndex newIndex = skuIndexMap.get(index.getSkuId());
			newIndex.setProgId(index.getProgId());
			newIndex.setPolicy(index.getPolicy());
			
		}
		
		
		
		
		// valuation >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		
		Map<Integer, Integer> groupNum = new HashMap<Integer, Integer>();
		Map<Integer, Float> enoughYen = new HashMap<Integer, Float>();
		Set<Integer> prodId = new HashSet<Integer>();
		
		// 预处理数据
		skuIndexMap.values().forEach(index -> {
			if (index.getProgId() == null) {
				index.setProgId(999999);
			}
			
			
			
			groupNum.put(index.getProgId(), groupNum.get(index.getProgId()) == null ? index.getNum() : groupNum.get(index.getProgId()) +  index.getNum());
			enoughYen.put(index.getProgId(), enoughYen.get(index.getProgId()) == null ? index.getPrice() * index.getNum() : 
				enoughYen.get(index.getProgId()) + index.getPrice() * index.getNum());
			prodId.add(index.getProdId());
			
			
			
			String poli = index.getPolicy();
			String poli_1 = "";
			String poli_2 = "";
			if (poli != null) {
				String[] item = poli.split(",");
				poli_1 = item[0];
				if (item.length == 2) {
					poli_2 = item[1];
				}
			}
			
			if (poli_1.startsWith("D")) {
				index.setProgId(800000);
			}
			else if (poli_1.startsWith("E") && poli_2.startsWith("C")) {
				index.setProgId(900000);
			}
		});
		
		try {
			String s = new ObjectMapper().writeValueAsString(skuIndexMap);
			System.out.println("xxxxxxskuIndexMap:" + s);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		// 排序 按progId分组，同级价格低在下面，为了价低者折，价低者减
		List<SkuIndex> skuIndexList = new ArrayList<SkuIndex>();
		skuIndexList.addAll(skuIndexMap.values());
		skuIndexList.sort(new SkuIndexComparator());
		
		int count2discount = 0;
		int countMoreAdd = 0;
		int countBuyFree = 0;
		
		for (SkuIndex index : skuIndexList) {
			String poli = index.getPolicy();
			String poli_1 = "";
			String poli_2 = "";
			int gN = groupNum.get(index.getProgId());
			float eYen = enoughYen.get(index.getProgId());
			if (poli != null) {
				String[] item = poli.split(",");
				poli_1 = item[0];
				poli_2 = item.length == 2 ? item[1] : "";
			}
			
			if (poli == null) {
				index.setItemPrice(index.getPrice() * index.getNum());
			}
			else if (poli.indexOf("S") == 0) {
				float p = Float.parseFloat(poli_1.split(":")[1]);
				index.setItemPrice(p * index.getNum());
			}
			else if (poli.indexOf("A") == 0) {
				float p = Float.parseFloat(poli_1.split(":")[1]);
				index.setItemPrice(p * index.getNum());
			}
			else if (poli.startsWith("%")) {
				if ("".equals(poli_2) || gN == 1) {
					float d = Float.parseFloat(poli_1.split(":")[1]);
					
					index.setItemPrice(index.getPrice() * index.getNum() * d);
				}
				else if (poli_2.startsWith("2+")) {
					float d = Float.parseFloat(poli_2.split(":")[1]);
					index.setItemPrice(index.getPrice() * index.getNum() * d);
				}
				else if (poli_2.startsWith("2")) {
					float batch1D = Float.parseFloat(poli_1.split(":")[1]);
					float batch2D = Float.parseFloat(poli_2.split(":")[1]);
					int batch1N = (int)Math.floor(gN/2) - count2discount;
					count2discount += index.getNum();
					if (batch1N >= index.getNum()) {
						index.setItemPrice(index.getPrice() * index.getNum());
					}
					else if (batch1N <= 0) {
						index.setItemPrice(index.getPrice() * batch2D);
					}
					else {
						index.setItemPrice(index.getPrice() * batch1N * batch1D + index.getPrice() * (index.getNum() - batch1N) * batch2D);
					}
				}
			}
			else if (poli.startsWith("+")) {
				if (gN == 1) {
					index.setItemPrice(index.getPrice());
				}
				else {
					float batch1Y = Float.parseFloat(poli_1.split(":")[1]);
					int batch1N = (int)Math.floor(gN/2) - countMoreAdd;
					countMoreAdd += index.getNum();
					if (batch1N >= index.getNum()) {
						index.setItemPrice(index.getPrice() * index.getNum());
					}
					else if (batch1N <= 0) {
						index.setItemPrice(batch1Y * index.getNum());
					}
					else {
						index.setItemPrice(index.getPrice() * batch1N + batch1Y * (index.getNum() - batch1N));
					}
				}
			}
			else if (poli_1.endsWith("Y")) {
				float y = Float.parseFloat(poli_1.split(":")[0]);
				int n = Integer.parseInt(poli_2.split(":")[0]);
				if (n <= gN) {
					float avgP = y / n;
					index.setItemPrice(avgP * index.getNum());
				}
				else {
					index.setItemPrice(index.getPrice() * index.getNum());
				}
			}
			else if (poli_1.startsWith("E") && poli_2.startsWith("-")) {
				float e = Float.parseFloat(poli_1.split(":")[1]);
				int y = Integer.parseInt(poli_2.split(":")[1]);
				
				if (eYen >= e) {
					float avgMinus = (float)Math.floor(eYen/e) * y / gN;
					index.setItemPrice((index.getPrice() - avgMinus) * index.getNum());
				}
			}
			else if (poli_1.startsWith("B")) {
				int buyN = Integer.parseInt(poli_1.split(":")[1]);
				int freeN = Integer.parseInt(poli_2.split(":")[1]);
				
				int gFreeN = (int)Math.floor(gN/buyN) * freeN;
				
				int batch1N = gN - gFreeN - countBuyFree;
				countBuyFree += index.getNum();
				if (buyN > gN) {
					index.setItemPrice(index.getPrice() * index.getNum());
				}
				else if (batch1N <= 0) {
					index.setItemPrice(0f);
				}
				else {
					if (batch1N >= index.getNum()) {
						index.setItemPrice(index.getPrice() * index.getNum());
					}
					else {
						index.setItemPrice(index.getPrice() * batch1N);
					}
				}
			}
			else if (poli_1.startsWith("D")) {
				int dependProdId = Integer.parseInt(poli_1.split(":")[1]);
				float dependPrice = Float.parseFloat(poli_2.split(":")[1]);
				if (prodId.contains(dependProdId)) {
					index.setItemPrice(dependPrice * index.getNum());
				}
				else {
					index.setItemPrice(index.getPrice() * index.getNum());
				}
			}
		};
		
		
		
		try {
			String s = new ObjectMapper().writeValueAsString(skuIndexList);
			System.out.println("xxxxxx:" + s);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		float excludeChangeTotalPrice = 0;
		for (SkuIndex index : skuIndexList) {
			excludeChangeTotalPrice += index.getItemPrice();
		}
		for (SkuIndex index : skuIndexList) {
			String poli = index.getPolicy();
			String poli_1 = "";
			String poli_2 = "";
			if (poli != null) {
				String[] item = poli.split(",");
				poli_1 = item[0];
				poli_2 = item.length == 2 ? item[1] : "";
			}
			if (poli_1.startsWith("E") && poli_2.startsWith("C")) {
				float e = Float.parseFloat(poli_1.split(":")[1]);
				float c = Float.parseFloat(poli_2.split(":")[1]);
				if (excludeChangeTotalPrice >= e) {
					index.setItemPrice(c * index.getNum());
				}
				else {
					index.setItemPrice(index.getPrice() * index.getNum());
				}
			}
		}
		
		
		
		
		

		
		
		
		
		
		
		
		
		
		
		// -2表示有不存在的skuId
		
		
		returnMap.put(1, skuIndexList);
		
		
		return returnMap;
	}
	
	
	
	
	
	
	
	
	
	
	

	
	
	
}
