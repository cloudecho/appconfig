package com.github.cloudecho.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlbean.annotation.ElementTag;
import org.xmlbean.util.PubUtils;

public class AppConfigBean {
	@ElementTag(name = "param")
	List<Param> params;

	public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

	public Map<String, Param> asMap() {
		Map<String, Param> map = new HashMap<String, Param>();
		for (Param p : this.getParams()) {
			p.setParamVal(stripEnv(p.getParamVal()).trim());
			map.put(p.getParamAttr().getKey(), p);
		}
		return map;
	}

	private String stripEnv(String val) {
		if (PubUtils.isEmpty(val)) {
			return "";
		}
		return AppUtils.stripEnv(val);
	}

	public static class Param {
		String paramVal;
		ParamAttr paramAttr;

		public String getParamVal() {
			return paramVal;
		}

		public void setParamVal(String paramVal) {
			this.paramVal = paramVal;
		}

		public ParamAttr getParamAttr() {
			return paramAttr;
		}

		public void setParamAttr(ParamAttr paramAttr) {
			this.paramAttr = paramAttr;
		}
	}

	public static class ParamAttr {
		String key;
		String name;
		String type;
		String description;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
}
