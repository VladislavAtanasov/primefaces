package com.sofia.uni.fmi.web.primefaces.mapper;

public enum Image {
	UBUNTU("ubuntu", "ami-02f69d40637223896"), WINDOWS("windows", "ami-03838ccd5cfb84782"), SUSE("suse",
			"ami-050889503ddaec473");

	private String name;
	private String id;

	private Image(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}
