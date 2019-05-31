package com.sofia.uni.fmi.web.primefaces.views;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.view.ViewScoped;

import com.amazonaws.services.ec2.model.Instance;
import com.sofia.uni.fmi.web.primefaces.dto.VmInstanceDTO;
import com.sofia.uni.fmi.web.primefaces.vms.service.AmazonVirtualMachineService;

@ManagedBean
@ViewScoped
public class ListVirtualMachinesView implements Serializable {

	private List<VmInstanceDTO> vms;

	private VmInstanceDTO selectedVmInstance;

	@ManagedProperty("#{amazonVirtualMachineService}")
	private AmazonVirtualMachineService service;

	@PostConstruct
	public void init() {
		this.vms = service.listVms().stream().map(i -> new VmInstanceDTO(i)).collect(Collectors.toList());
	}

	public List<VmInstanceDTO> getVms() {
		return vms;
	}

	public void setService(AmazonVirtualMachineService service) {
		this.service = service;
	}

	public VmInstanceDTO getSelectedVmInstance() {
		return selectedVmInstance;
	}

	public void setSelectedVmInstance(VmInstanceDTO selectedVmInstance) {
		this.selectedVmInstance = selectedVmInstance;
	}

}