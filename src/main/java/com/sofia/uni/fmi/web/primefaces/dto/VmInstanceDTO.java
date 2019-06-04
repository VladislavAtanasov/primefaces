package com.sofia.uni.fmi.web.primefaces.dto;

import java.util.List;

import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;

public class VmInstanceDTO {

	public String instanceId;

	public String instanceType;

	public String state;

	public String imageName;

	public String keyName;

	public String vpcId;

	public String sgName;

	public VmInstanceDTO(Instance instance) {
		this.instanceId = instance.getInstanceId();
		this.instanceType = instance.getInstanceType();
		this.state = instance.getState().getName();
		this.imageName = instance.getTags().stream().filter(t -> t.getKey().equals("imageName")).findFirst()
				.map(v -> v.getValue()).get();
		this.vpcId = instance.getVpcId();
		this.keyName = instance.getKeyName();
		List<GroupIdentifier> securityGroups = instance.getSecurityGroups();
		if (!securityGroups.isEmpty()) {
			this.sgName = securityGroups.get(0).getGroupName();
		}
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

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getVpcId() {
		return vpcId;
	}

	public void setVpcId(String vpcId) {
		this.vpcId = vpcId;
	}

	public String getSgName() {
		return sgName;
	}

	public void setSgName(String sgName) {
		this.sgName = sgName;
	}

}
