package com.sofia.uni.fmi.web.primefaces.views;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

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

	public void stop() {
		System.out.println("Vlqzohhh");
		String instanceId = selectedVmInstance.getInstanceId();
		boolean stopVm = service.stopVm(instanceId);
		if (stopVm) {
			FacesMessage msg = new FacesMessage("Success", "Your instance id: " + instanceId + " is being stopped");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage("Fail", "Your instance id: " + instanceId + " failed to stop");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
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