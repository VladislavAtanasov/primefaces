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
		String message = service.createVm(vm);

		if (message.startsWith("i-")) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success! Your instance id is:" + message,
					null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
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
