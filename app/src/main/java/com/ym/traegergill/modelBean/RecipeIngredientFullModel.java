package com.ym.traegergill.modelBean;

public class RecipeIngredientFullModel {

	private Integer recipeIngredientid;
	private Integer recipeid;
	private Integer ingredientid;
	private Integer type;
	private Integer quantity;
	private String ingredientName;
	private String unit;
	
	public RecipeIngredientFullModel() {
		super();
	}

	public RecipeIngredientFullModel(Integer recipeIngredientid, Integer recipeid, Integer ingredientid, Integer type,
			Integer quantity, String ingredientName, String unit) {
		super();
		this.recipeIngredientid = recipeIngredientid;
		this.recipeid = recipeid;
		this.ingredientid = ingredientid;
		this.type = type;
		this.quantity = quantity;
		this.ingredientName = ingredientName;
		this.unit = unit;
	}

	public Integer getRecipeIngredientid() {
		return recipeIngredientid;
	}

	public void setRecipeIngredientid(Integer recipeIngredientid) {
		this.recipeIngredientid = recipeIngredientid;
	}

	public Integer getRecipeid() {
		return recipeid;
	}

	public void setRecipeid(Integer recipeid) {
		this.recipeid = recipeid;
	}

	public Integer getIngredientid() {
		return ingredientid;
	}

	public void setIngredientid(Integer ingredientid) {
		this.ingredientid = ingredientid;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getIngredientName() {
		return ingredientName;
	}

	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
}
