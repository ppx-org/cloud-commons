package com.ppx.cloud.storecommon.price.bean;

public class SkuIndex {
	private int skuId;
	
	private int num;
	
	private Float price;
	
	private Integer prodId;
	
	private String policy;
	
	private Integer progId;
	
	private Float itemPrice;
	
	// display
	private String prodTitle;
	
	private String skuName;
	
	private String skuImgSrc;
	
	
	public SkuIndex() {
		
	}
	
	public SkuIndex(int skuId, int num) {
		this.skuId = skuId;
		this.num = num;
	}

	public int getSkuId() {
		return skuId;
	}

	public void setSkuId(int skuId) {
		this.skuId = skuId;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Integer getProdId() {
		return prodId;
	}

	public void setProdId(Integer prodId) {
		this.prodId = prodId;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public Integer getProgId() {
		return progId;
	}

	public void setProgId(Integer progId) {
		this.progId = progId;
	}

	public Float getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(Float itemPrice) {
		this.itemPrice = itemPrice;
	}

	public String getProdTitle() {
		return prodTitle;
	}

	public void setProdTitle(String prodTitle) {
		this.prodTitle = prodTitle;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public String getSkuImgSrc() {
		return skuImgSrc;
	}

	public void setSkuImgSrc(String skuImgSrc) {
		this.skuImgSrc = skuImgSrc;
	}

	
	
	
	
	
}

























