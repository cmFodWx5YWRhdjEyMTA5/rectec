package com.ym.traegergill.modelBean;
// Generated 2017-10-23 17:20:01 by Hibernate Tools 5.2.5.Final

/**
 * Flavor generated by hbm2java
 */
public class Flavor implements java.io.Serializable {

	private Integer flavorid;
	private Integer ingredientid;

	public Flavor() {
	}

	public Flavor(Integer ingredientid) {
		this.ingredientid = ingredientid;
	}

	public Integer getFlavorid() {
		return this.flavorid;
	}

	public void setFlavorid(Integer flavorid) {
		this.flavorid = flavorid;
	}

	public Integer getIngredientid() {
		return this.ingredientid;
	}

	public void setIngredientid(Integer ingredientid) {
		this.ingredientid = ingredientid;
	}

}