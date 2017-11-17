package com.ym.traegergill.modelBean;

import java.util.List;

public class FilterGroupByFilterTypeModel {
	private Integer filterTypeid;
	private String filterTypeName;
	private String describe;
	private List<Filter> filterList;
	
	public FilterGroupByFilterTypeModel(Integer filterTypeid, String filterTypeName, String describe) {
		super();
		this.filterTypeid = filterTypeid;
		this.filterTypeName = filterTypeName;
		this.describe = describe;
	}

	public FilterGroupByFilterTypeModel(Integer filterTypeid, String filterTypeName, String describe,
			List<Filter> filterList) {
		super();
		this.filterTypeid = filterTypeid;
		this.filterTypeName = filterTypeName;
		this.describe = describe;
		this.filterList = filterList;
	}

	public Integer getFilterTypeid() {
		return filterTypeid;
	}

	public void setFilterTypeid(Integer filterTypeid) {
		this.filterTypeid = filterTypeid;
	}

	public String getFilterTypeName() {
		return filterTypeName;
	}

	public void setFilterTypeName(String filterTypeName) {
		this.filterTypeName = filterTypeName;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public List<Filter> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<Filter> filterList) {
		this.filterList = filterList;
	}
	
}
