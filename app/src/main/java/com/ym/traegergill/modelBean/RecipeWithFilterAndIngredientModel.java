package com.ym.traegergill.modelBean;

import java.util.Date;
import java.util.List;
import java.util.Map;



public class RecipeWithFilterAndIngredientModel {
	
	private Integer recipeid;
	private String mainPic;
	private String title;
	private String describe;
	private String difficulty;
	private String prepareTime;
	private String cookTime;
	private Integer uid;
	private String serves;
	private String hardwood;
	private String cookDurationDesc;
	private Date updateTime;
	private List<Filter> recipeFilterList;
	private Map<String, Object> filterListMap;
	private List<RecipeIngredientFullModel> recipeIngredientFullModelList;

	public RecipeWithFilterAndIngredientModel() {
		super();
	}

	public RecipeWithFilterAndIngredientModel(Integer recipeid, String mainPic, String title, String describe,
			String difficulty, String prepareTime, String cookTime, Integer uid, String serves, String hardwood,
			String cookDurationDesc, Date updateTime, List<Filter> recipeFilterList, Map<String, Object> filterListMap,
			List<RecipeIngredientFullModel> recipeIngredientFullModelList) {
		super();
		this.recipeid = recipeid;
		this.mainPic = mainPic;
		this.title = title;
		this.describe = describe;
		this.difficulty = difficulty;
		this.prepareTime = prepareTime;
		this.cookTime = cookTime;
		this.uid = uid;
		this.serves = serves;
		this.hardwood = hardwood;
		this.cookDurationDesc = cookDurationDesc;
		this.updateTime = updateTime;
		this.recipeFilterList = recipeFilterList;
		this.filterListMap = filterListMap;
		this.recipeIngredientFullModelList = recipeIngredientFullModelList;
	}

	public Integer getRecipeid() {
		return recipeid;
	}

	public void setRecipeid(Integer recipeid) {
		this.recipeid = recipeid;
	}

	public String getMainPic() {
		return mainPic;
	}

	public void setMainPic(String mainPic) {
		this.mainPic = mainPic;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getPrepareTime() {
		return prepareTime;
	}

	public void setPrepareTime(String prepareTime) {
		this.prepareTime = prepareTime;
	}

	public String getCookTime() {
		return cookTime;
	}

	public void setCookTime(String cookTime) {
		this.cookTime = cookTime;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getServes() {
		return serves;
	}

	public void setServes(String serves) {
		this.serves = serves;
	}

	public String getHardwood() {
		return hardwood;
	}

	public void setHardwood(String hardwood) {
		this.hardwood = hardwood;
	}

	public String getCookDurationDesc() {
		return cookDurationDesc;
	}

	public void setCookDurationDesc(String cookDurationDesc) {
		this.cookDurationDesc = cookDurationDesc;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public List<Filter> getRecipeFilterList() {
		return recipeFilterList;
	}

	public void setRecipeFilterList(List<Filter> recipeFilterList) {
		this.recipeFilterList = recipeFilterList;
	}

	public Map<String, Object> getFilterListMap() {
		return filterListMap;
	}

	public void setFilterListMap(Map<String, Object> filterListMap) {
		this.filterListMap = filterListMap;
	}

	public List<RecipeIngredientFullModel> getRecipeIngredientFullModelList() {
		return recipeIngredientFullModelList;
	}

	public void setRecipeIngredientFullModelList(List<RecipeIngredientFullModel> recipeIngredientFullModelList) {
		this.recipeIngredientFullModelList = recipeIngredientFullModelList;
	}

}
