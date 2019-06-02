package com.sofia.uni.fmi.web.primefaces.views;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FlowEvent;

import com.sofia.uni.fmi.web.primefaces.vms.service.AmazonVirtualMachineService;

@ManagedBean
@ViewScoped
public class CreateVmInstanceWizard implements Serializable {

	private CreateVmInstance vm = new CreateVmInstance();

	@ManagedProperty("#{amazonVirtualMachineService}")
	private AmazonVirtualMachineService service;

	public void create() {
		String instanceId = service.createVm(vm);
		if (instanceId != null) {
			FacesMessage msg = new FacesMessage("Success", "Your instance id is:" + instanceId);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage("Fail", "Invalid Input");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void setService(AmazonVirtualMachineService service) {
		this.service = service;
	}

	public CreateVmInstance getVm() {
		return vm;
	}

	public void setVm(CreateVmInstance vm) {
		this.vm = vm;
	}

	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}
}
