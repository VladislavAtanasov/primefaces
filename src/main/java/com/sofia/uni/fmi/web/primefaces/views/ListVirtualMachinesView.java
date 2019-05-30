package com.sofia.uni.fmi.web.primefaces.views;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.view.ViewScoped;

import com.amazonaws.services.ec2.model.Instance;
import com.sofia.uni.fmi.web.primefaces.vms.service.AmazonVirtualMachineService;

@ManagedBean
@ViewScoped
public class ListVirtualMachinesView implements Serializable {

	private List<Instance> vms;

	private Instance selectedVmInstance;

	@ManagedProperty("#{amazonVirtualMachineService}")
	private AmazonVirtualMachineService service;

	@PostConstruct
	public void init() {
		this.vms = service.listVms();
	}

	public List<Instance> getVms() {
		return vms;
	}

	public void setService(AmazonVirtualMachineService service) {
		this.service = service;
	}

	public Instance getSelectedVmInstance() {
		return selectedVmInstance;
	}

	public void setSelectedVmInstance(Instance selectedVmInstance) {
		this.selectedVmInstance = selectedVmInstance;
	}

}