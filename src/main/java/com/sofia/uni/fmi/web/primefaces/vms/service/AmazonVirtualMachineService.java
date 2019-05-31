package com.sofia.uni.fmi.web.primefaces.vms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;

@ManagedBean(name = "amazonVirtualMachineService")
@ApplicationScoped
public class AmazonVirtualMachineService {

	public List<Instance> listVms() {
		AmazonEC2 client = getEc2Client();

		List<Reservation> reservations = client.describeInstances().getReservations();
		if (reservations.isEmpty()) {
			return new ArrayList<>();
		}
		return reservations.stream()
				.flatMap(r-> r.getInstances().stream()).collect(Collectors.toList());
	}

	public void createVm() {
		AmazonEC2 ec2Client = getEc2Client();
//		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
//
//				.withGroupName("BaeldungSecurityGroup").withDescription("Baeldung Security Group");
//		CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);

		IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
		IpPermission ipPermission = new IpPermission().withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
				.withIpProtocol("tcp").withFromPort(80).withToPort(80);

//		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest()
//				.withGroupName("BaeldungSecurityGroup").withIpPermissions(ipPermission);
//		ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

//		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName("baeldung-key-pair");
//		CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId("ami-02f69d40637223896")
				.withInstanceType("t2.micro").withKeyName("baeldung-key-pair").withMinCount(1).withMaxCount(1)
				.withSecurityGroups("BaeldungSecurityGroup");
		String yourInstanceId = ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0)
				.getInstanceId();

		System.out.println("VMS ID " + yourInstanceId);

	}

	private AmazonEC2 getEc2Client() {
		AWSCredentials credentials = new BasicAWSCredentials("AKIASCHJDNXPAUDWFIXS", "EXjRtLherEEl3kUWsx3AiNNjO2/cn0tKgViL9jVf");

		return AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.EU_WEST_1).build();
	}

	public static void main(String[] args) {
//		System.out.println(new AmazonVirtualMachineService().listVms());
		new AmazonVirtualMachineService().createVm();
		
	}
}
