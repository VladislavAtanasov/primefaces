package com.sofia.uni.fmi.web.primefaces.vms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.sofia.uni.fmi.web.primefaces.mapper.Image;
import com.sofia.uni.fmi.web.primefaces.mapper.ImagesMapper;
import com.sofia.uni.fmi.web.primefaces.views.CreateVmInstance;

@ManagedBean(name = "amazonVirtualMachineService")
@ApplicationScoped
@Component
public class AmazonVirtualMachineService extends SpringBeanAutowiringSupport {

	public List<Instance> listVms() {
		AmazonEC2 client = getEc2Client();

		List<Reservation> reservations = client.describeInstances().getReservations();
		if (reservations.isEmpty()) {
			return new ArrayList<>();
		}

		return reservations.stream().flatMap(r -> r.getInstances().stream().filter(instance -> {
			return filterVMS(instance);
		})).collect(Collectors.toList());
	}

	private boolean filterVMS(Instance instance) {
		Tag tagUserName = instance.getTags().stream().filter(t -> t.getKey().equals("username")).findFirst()
				.orElse(null);
		String userName = null;
		if (tagUserName != null) {
			userName = tagUserName.getValue();
		}

		String loggedUserName = getLoggedUserName();

		return loggedUserName.equals(userName)
				&& !instance.getState().getName().equalsIgnoreCase(InstanceStateName.Terminated.toString())
				&& !instance.getState().getName().equalsIgnoreCase(InstanceStateName.ShuttingDown.toString());
	}

	public String createVm(CreateVmInstance request) {
		String sgName = request.getSgName();
		String keyPairName = request.getKeyPairName();
		String size = request.getSize();
		Image imageName = Image.valueOf(request.getImageName());
		String imageId = new ImagesMapper().getImages().get(imageName);
		if (imageId == null) {
			System.out.println("Cannot find image with name: " + imageName);
			return "Cannot find image with name: " + imageName;
		}

		AmazonEC2 ec2Client = getEc2Client();
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest().withGroupName(sgName)
				.withDescription(sgName);

		try {
			ec2Client.createSecurityGroup(createSecurityGroupRequest);
		} catch (AmazonClientException e) {
			return e.getMessage();
		}

		IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
		IpPermission ipPermission = new IpPermission().withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
				.withIpProtocol("tcp").withFromPort(80).withToPort(80);

		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest()
				.withGroupName(sgName).withIpPermissions(ipPermission);
		try {
			ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		} catch (AmazonClientException e) {
			return e.getMessage();
		}

		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName(keyPairName);
		try {
			ec2Client.createKeyPair(createKeyPairRequest);
		} catch (AmazonClientException e) {
			return e.getMessage();
		}

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(imageId).withInstanceType(size)
				.withKeyName(keyPairName).withMinCount(1).withMaxCount(1).withSecurityGroups(sgName);

		String instanceId = null;
		try {
			instanceId = ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0)
					.getInstanceId();
		} catch (AmazonClientException e) {
			return e.getMessage();
		}

		CreateTagsRequest tagsRequest = new CreateTagsRequest();

		String username = getLoggedUserName();

		Collection<Tag> tags = new ArrayList<>();
		tags.add(new Tag("imageName", imageName.getName()));
		tags.add(new Tag("username", username));
		tagsRequest.setTags(tags);
		tagsRequest.withResources(instanceId);
		ec2Client.createTags(tagsRequest);

		return instanceId;
	}

	private String getLoggedUserName() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = principal.toString();
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		}
		return username;
	}

	public boolean stopVm(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
		StopInstancesResult stopInstancesResult = ec2Client.stopInstances(request);
		return stopInstancesResult.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}

	public boolean startVm(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
		StartInstancesResult sartInstancesResult = ec2Client.startInstances(request);
		return sartInstancesResult.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}

	public boolean terminate(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);
		TerminateInstancesResult result = ec2Client.terminateInstances(request);
		return result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}

	private AmazonEC2 getEc2Client() {
		return AmazonEC2ClientBuilder.standard().withCredentials(new EnvironmentVariableCredentialsProvider())
				.withRegion(Regions.EU_WEST_1).build();
	}

}
