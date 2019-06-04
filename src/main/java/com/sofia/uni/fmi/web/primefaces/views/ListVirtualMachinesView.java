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
		this.refreshData();
	}

	public void stop() {
		String instanceId = selectedVmInstance.getInstanceId();
		if (service.stopVm(instanceId)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Your instance id: " + instanceId + " is being stopped", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Your instance id: " + instanceId + " failed to stop", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void start() {
		String instanceId = selectedVmInstance.getInstanceId();
		if (service.startVm(instanceId)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Your instance id: " + instanceId + " is being started", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Your instance id: " + instanceId + " failed to start", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void terminate() {
		String instanceId = selectedVmInstance.getInstanceId();
		if (service.terminate(instanceId)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Your instance id: " + instanceId + " is being terminated", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Your instance id: " + instanceId + " failed to terminate", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void getKeyPairs() {
		service.getKeyPairs();
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

	public void refreshData() {
		this.vms = service.listVms().stream().map(i -> new VmInstanceDTO(i)).collect(Collectors.toList());
	}
}