package com.sofia.uni.fmi.web.primefaces.dto;

import com.amazonaws.services.ec2.model.Instance;

public class VmInstanceDTO {

	public String instanceId;

	public String instanceType;

	public String state;

	public String imageName;

	public VmInstanceDTO(Instance instance) {
		this.instanceId = instance.getInstanceId();
		this.instanceType = instance.getInstanceType();
		this.state = instance.getState().getName();
		System.out.println(instance.getTags());
		this.imageName = instance.getTags().stream().filter(t -> t.getKey().equals("imageName")).findFirst()
				.map(v -> v.getValue()).get();
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

}
